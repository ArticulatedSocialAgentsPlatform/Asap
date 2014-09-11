package asap.bmlflowvisualizer.utils;

import hmi.xml.XMLScanException;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import saiba.bml.core.Behaviour;

import javax.swing.DefaultListModel;

import asap.bml.ext.bmla.feedback.BMLABlockPredictionFeedback;
import asap.bml.ext.bmla.feedback.BMLABlockProgressFeedback;
import asap.bml.ext.bmla.feedback.BMLAFeedbackParser;
import asap.bml.ext.bmla.feedback.BMLAPredictionFeedback;
import asap.bml.ext.bmla.feedback.BMLASyncPointProgressFeedback;
import asap.bmlflowvisualizer.gui.VisualisationField;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.feedback.BMLFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import saiba.bml.parser.SyncPoint;

public class BMLBlock {

	private Point[] position;
	private int[] width;
	private Color borderColor;
	private int boxHeight = 20; // TODO Have only one global variable determine
								// this value for all classes
	private String id;
	private int borderThickness;
	private BehaviourBlock bb;
	private List<BMLInformation> feedback = Collections
			.synchronizedList(new ArrayList<BMLInformation>());

	private long planStart = -1;
	private long planEnd = -1;
	private long schedStart = -1;
	private long schedEnd = -1;
	private long playStart = -1;
	private long playEnd = -1;
	private boolean isInterruptBlock;
	private HashMap<BMLBlockStatus, Long> statusTimes = new HashMap<BMLBlockStatus, Long>();
	private BMLBlockStatus curStatus;

	public BMLBlock(String id, BehaviourBlock bb, long time) {
		this.id = id;
		this.position = new Point[5];
		this.width = new int[5];
		this.borderColor = Color.BLACK;
		this.bb = bb;
		this.borderThickness = 1;
		this.planStart = time;
		this.isInterruptBlock = false;
		if (bb != null) {
			curStatus = BMLBlockStatus.SUBMITTED;
			statusTimes.put(BMLBlockStatus.SUBMITTED, time);
		}
	}

	public BMLBlock(String id) {
		this(id, null, 0);
	}

	public boolean intersects(int x, int y, int field) {
		if (field >= 0 && field < 5) {
			if (position[field] != null) {
				if (x - position[field].x <= width[field]
						&& x - position[field].x >= 0
						&& y - position[field].y >= 0
						&& y - position[field].y <= boxHeight) {
					return true;
				}

			}
		}
		return false;
	}

	public DefaultListModel<String> getMessageList() {
		DefaultListModel<String> list = new DefaultListModel<String>();
		list.addElement(BehaviourBlock.class.getSimpleName() + " " + planStart);
		synchronized (feedback) {
			for (BMLInformation info : feedback) {
				String s = "";
				BMLFeedback fb;
				try {
					fb = BMLAFeedbackParser
							.parseFeedback(info.getInformation());
					if (fb instanceof BMLABlockProgressFeedback) {
						s = BMLABlockProgressFeedback.class.getSimpleName()
								+ ", " + info.getTimestamp();
					} else if (fb instanceof BMLSyncPointProgressFeedback) {
						s = BMLASyncPointProgressFeedback.class.getSimpleName()
								+ ", " + info.getTimestamp();
					} else if (fb instanceof BMLPredictionFeedback) {
						s = BMLAPredictionFeedback.class.getSimpleName() + ", "
								+ info.getTimestamp();
					} else if (fb instanceof BMLWarningFeedback) {
						s = BMLWarningFeedback.class.getSimpleName() + ", "
								+ info.getTimestamp();
					}
				} catch (IOException e) {
					// shouldn't happen since we parse strings
					throw new AssertionError(e);
				} catch (XMLScanException e) {
					System.out.println("XMLScanException: " + e.toString());
				}
				list.addElement(s);
			}
		}
		return list;
	}

	public DefaultListModel<String> getBehaviourList() {
		DefaultListModel<String> list = new DefaultListModel<String>();
		for (Behaviour b : bb.behaviours) {
			String s = b.getClass().getSimpleName() + " " + b.id + " ";
			for (SyncPoint point : b.getSyncPoints()) {
				if (point.getName().equals("start")) {
					s += point.getRefString() + ", ";
				}
				if (point.getName().equals("end")) {
					s += point.getRefString();
				}
			}
			list.addElement(s);
		}
		return list;
	}

	public DefaultListModel<String> getStateList() {
		DefaultListModel<String> list = new DefaultListModel<String>();
		String s = "";
		ArrayList<Entry<BMLBlockStatus, Long>> orderedList = new ArrayList<Entry<BMLBlockStatus, Long>>();
		for (Entry<BMLBlockStatus, Long> e : statusTimes.entrySet()) {
			int index;
			for (index = 0; index < orderedList.size(); index++) {
				if (orderedList.get(index).getValue() > e.getValue()) {
					break;
				}
			}
			orderedList.add(index, e);
		}
		for (Entry<BMLBlockStatus, Long> e : orderedList) {

			s = e.getKey().name() + " " + e.getValue();
			list.addElement(s);
		}

		return list;
	}

	/**
	 * Overwrites the SyncPoints of a behaviour when a new PredictionFeedback
	 * comes in
	 * 
	 * @param behaviour
	 *            to be updated
	 */
	public void updateBehaviourSyncPoints(Behaviour behaviour) {
		for (Behaviour b : bb.behaviours) {
			if (b.id.equals(behaviour.id)) {
				b.getSyncPoints().clear();
				b.addSyncPoints(behaviour.getSyncPoints());
			}
		}

	}

	/**
	 * 
	 * @param status
	 *            The status of interest
	 * @return Returns the time the given status was first encountered, or -1 if
	 *         the status was not encountered
	 */
	public long getStatusTime(BMLBlockStatus status) {
		if (statusTimes.containsKey(status)) {
			return statusTimes.get(status);
		}
		return -1;
	}

	public void addFeedback(BMLInformation info) {
		synchronized (feedback) {
			feedback.add(info);
		}
		// TODO Update this Block according to the information. This is
		// currently done in the BMLFlowVisualizerPort
	}

	public void start(long time) {
		curStatus = BMLBlockStatus.IN_EXEC;
		if (!statusTimes.containsKey(BMLBlockStatus.IN_EXEC)) {
			statusTimes.put(BMLBlockStatus.IN_EXEC, time);
		}

		if (playStart < 0) {
			playStart = time;
		}
		if (schedEnd < 0) {
			schedEnd = time;
		}
	}

	public void end(long time) {
		if (statusTimes.containsKey(BMLBlockStatus.INTERRUPT_REQUESTED)) {
			switch (curStatus) {
			case IN_EXEC:
				curStatus = BMLBlockStatus.INTERRUPTED;
				break;
			default:
				curStatus = BMLBlockStatus.REVOKED;
				break;
			}
		} else {
			curStatus = BMLBlockStatus.DONE;
		}

		if (!statusTimes.containsKey(curStatus)) {
			statusTimes.put(curStatus, time);
		}
		if (playEnd < 0) {
			playEnd = time;
		}

	}

	public void update(BMLABlockPredictionFeedback feedback, long time) {

		switch (feedback.getStatus()) {
		case IN_PREP:
			curStatus = BMLBlockStatus.IN_PREP;
			break;
		case PENDING:
			curStatus = BMLBlockStatus.PENDING;
			break;
		case LURKING:
			curStatus = BMLBlockStatus.LURKING;
		default:
			break;
		}

		if (!statusTimes.containsKey(curStatus)) {
			statusTimes.put(curStatus, time);
		}

		if (planEnd < 0) {
			planEnd = time;
		}
		if (schedStart < 0) {
			schedStart = time;
		}
	}

	public String getId() {
		return id;
	}

	public List<BMLInformation> getFeedbacks() {
		return feedback;
	}

	public Point getPosition(int field) {
		if (field >= 0 && field < 5) {
			return position[field];
		}
		return null;
	}

	public void setPosition(Point position, int width, VisualisationField field) {
		this.position[field.ordinal()] = position;
		this.width[field.ordinal()] = width;
	}

	public Color getColor() {
		switch (curStatus) {
		case PENDING:
			return Color.ORANGE;
		case LURKING:
			return Color.MAGENTA;
		case IN_EXEC:
			return Color.GREEN;
		case DONE:
			return Color.GREEN;
		case REVOKED:
			return Color.LIGHT_GRAY;
		case INTERRUPTED:
			return Color.ORANGE;
		case FAILED:
			return Color.RED;
		default:
			return Color.GREEN;
		}
	}

	public BehaviourBlock getBb() {
		return bb;
	}

	public void setBb(BehaviourBlock bb, long time) {
		this.bb = bb;
		curStatus = BMLBlockStatus.SUBMITTED;
		statusTimes.put(BMLBlockStatus.SUBMITTED, time);
	}

	public int getBoxHeight() {
		return boxHeight;
	}

	public void setBoxHeight(int boxHeight) {
		this.boxHeight = boxHeight;
	}

	public Color getBorderColor() {
		return borderColor;
	}

	public void setBorderColor(Color borderColor) {
		this.borderColor = borderColor;
	}

	public void setBorderThickness(int thickness) {
		this.borderThickness = thickness;
	}

	public int getBorderThickness() {
		return borderThickness;
	}

	public long getPlanStart() {
		return planStart;
	}

	public void setPlanStart(long planStart) {
		this.planStart = planStart;
	}

	public long getPlanEnd() {
		return planEnd;
	}

	public void setPlanEnd(long planEnd) {
		this.planEnd = planEnd;
	}

	public long getSchedStart() {
		return schedStart;
	}

	public void setSchedStart(long schedStart) {
		this.schedStart = schedStart;
	}

	public long getSchedEnd() {
		return schedEnd;
	}

	public void setSchedEnd(long schedEnd) {
		this.schedEnd = schedEnd;
	}

	public long getPlayStart() {
		return playStart;
	}

	public void setPlayStart(long playStart) {
		this.playStart = playStart;
	}

	public long getPlayEnd() {
		return playEnd;
	}

	public void setPlayEnd(long playEnd) {
		this.playEnd = playEnd;
	}

	/**
	 * Used to request an interrupt for this block
	 * 
	 * @param time
	 *            The time the request came in
	 */
	public void interrupt(long time) {
		curStatus = BMLBlockStatus.INTERRUPT_REQUESTED;
		statusTimes.put(BMLBlockStatus.INTERRUPT_REQUESTED, time);
	}

	/**
	 * Returns the index of the item in the messageList that was last activated
	 * after the given time
	 * 
	 * @param time
	 *            The time from which to search
	 * @return Index of the item in question
	 */
	public int getCurMessageListIndex(long time) {
		int index = 0;

		for (BMLInformation info : feedback) {
			if (info.getTimestamp() <= time) {
				index++;
			} else {
				break;
			}
		}
		return index;
	}

	public BMLBlockStatus getCurStatus() {
		return curStatus;
	}

	/**
	 * Returns the index of the item in the stateList that was last activated
	 * after the given time
	 * 
	 * @param time
	 *            The time from which to search
	 * @return Index of the item in question
	 */
	public int getCurStateListIndex(long time) {
		int index = -1;
		for (BMLBlockStatus status : BMLBlockStatus.values()) {
			if (statusTimes.get(status) != null
					&& statusTimes.get(status) <= time) {
				index++;
			}
		}
		return index;
	}

	/**
	 * Method to mark this bmlBlock as an interrupt block that interrupts other
	 * blocks.
	 */
	public void isInterruptBlock() {
		isInterruptBlock = true;
		borderColor = Color.RED;
	}

	public String getCurStatus(long timestamp) {
		BMLBlockStatus lastStatus = BMLBlockStatus.NONE;
		long lastTime = 0;
		for (Entry<BMLBlockStatus, Long> e : statusTimes.entrySet()) {
			if (e.getValue() <= timestamp && e.getValue() > lastTime) {
				lastStatus = e.getKey();
				lastTime = e.getValue();
			}
		}
		return lastStatus.name();
	}

}
