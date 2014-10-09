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
import asap.bml.ext.bmla.feedback.BMLABlockStatus;
import asap.bml.ext.bmla.feedback.BMLAFeedbackParser;
import asap.bml.ext.bmla.feedback.BMLAPredictionFeedback;
import asap.bml.ext.bmla.feedback.BMLASyncPointProgressFeedback;
import asap.bmlflowvisualizer.gui.BMLFlowVisualization;
import asap.bmlflowvisualizer.gui.VisualisationField;
import saiba.bml.core.BehaviourBlock;
import saiba.bml.feedback.BMLFeedback;
import saiba.bml.feedback.BMLPredictionFeedback;
import saiba.bml.feedback.BMLSyncPointProgressFeedback;
import saiba.bml.feedback.BMLWarningFeedback;
import saiba.bml.parser.SyncPoint;

/**
 * Wrapper class for a BMLBlock. Stores all received feedback and status times
 * of the bml block as well as some gui related information such as positions on
 * the panels.
 * 
 * @author jpoeppel
 *
 */
public class BMLBlock {

	private Point[] position;
	private int[] width;
	private Color borderColor;

	private String id;
	private int borderThickness;
	private BehaviourBlock bb;
	private List<BMLInformation> feedback = Collections
			.synchronizedList(new ArrayList<BMLInformation>());

	private HashMap<BMLBlockStatus, Long> statusTimes = new HashMap<BMLBlockStatus, Long>();
	private BMLBlockStatus curStatus;

	public BMLBlock(String id, BehaviourBlock bb, long time) {
		this.id = id;
		this.position = new Point[5];
		this.width = new int[5];
		this.borderColor = Color.BLACK;
		this.bb = bb;
		this.borderThickness = 1;
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
						&& y - position[field].y <= BMLFlowVisualization.BLOCK_HEIGHT) {
					return true;
				}

			}
		}
		return false;
	}

	public DefaultListModel<String> getMessageList() {
		DefaultListModel<String> list = new DefaultListModel<String>();
		list.addElement(BehaviourBlock.class.getSimpleName() + " "
				+ getPlanStart() + " ms");
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
					s += " ms";
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
					s += point.getRefString() + " ms, ";
				}
				if (point.getName().equals("end")) {
					s += point.getRefString() + " ms";
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

			s = e.getKey().name() + " " + e.getValue() + " ms";
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
	}

	public void end(BMLABlockProgressFeedback progFeedback, long time) {
		if (progFeedback.getStatus() == BMLABlockStatus.REVOKED) {
			curStatus = BMLBlockStatus.REVOKED;
		} else if (statusTimes.containsKey(BMLBlockStatus.INTERRUPT_REQUESTED)) {
			switch (curStatus) {
			case IN_EXEC:
				curStatus = BMLBlockStatus.INTERRUPTED;
				break;
			default:
				// curStatus = BMLBlockStatus.REVOKED;
				break;
			}
		} else {
			curStatus = BMLBlockStatus.DONE;
		}

		if (!statusTimes.containsKey(curStatus)) {
			statusTimes.put(curStatus, time);
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

	/**
	 * Returns the color of the block according to the status this block was in
	 * at the given timestamp. If timestamp < 0 the current status is used.
	 * 
	 * @param timestamp
	 *            Determines the status that is used for the colour
	 * @return
	 */
	public Color getColor(long timestamp) {
		if (getStatusAt(timestamp) == BMLBlockStatus.INTERRUPT_REQUESTED) {
			return getColorFor(getStatusAt(statusTimes
					.get(BMLBlockStatus.INTERRUPT_REQUESTED) - 1));
		} else {
			return getColorFor(getStatusAt(timestamp));
		}

	}

	public BehaviourBlock getBb() {
		return bb;
	}

	public void setBb(BehaviourBlock bb, long time) {
		this.bb = bb;
		if (!statusTimes.containsKey(BMLBlockStatus.SUBMITTED)) {
			curStatus = BMLBlockStatus.SUBMITTED;
			statusTimes.put(BMLBlockStatus.SUBMITTED, time);
		}
	}

	public Color getBorderColor(VisualisationField field) {
		switch (field) {
		case HistPlanned:
			if (statusTimes.containsKey(BMLBlockStatus.REVOKED)) {
				return Color.RED;
			}
			break;
		case HistPlayed:
			if (statusTimes.containsKey(BMLBlockStatus.INTERRUPTED)) {
				return Color.RED;
			}
			break;
		case HistScheduled:
			if (statusTimes.containsKey(BMLBlockStatus.REVOKED)) {
				return Color.RED;
			}
			break;
		case DetailPlanned:
			if (statusTimes.containsKey(BMLBlockStatus.REVOKED)) {
				return Color.RED;
			}
			break;
		case DetailScheduled:
			if (statusTimes.containsKey(BMLBlockStatus.REVOKED)) {
				return Color.RED;
			}
			break;
		default:
			break;
		}
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
		if (statusTimes.containsKey(BMLBlockStatus.SUBMITTED)) {
			return statusTimes.get(BMLBlockStatus.SUBMITTED);
		} else {
			return -1;
		}
	}

	public long getPlanEnd() {
		long tmpPen = Long.MAX_VALUE;
		long tmpLurk = Long.MAX_VALUE;
		if (statusTimes.containsKey(BMLBlockStatus.PENDING)) {
			tmpPen = statusTimes.get(BMLBlockStatus.PENDING);
		}
		if (statusTimes.containsKey(BMLBlockStatus.LURKING)) {
			tmpLurk = statusTimes.get(BMLBlockStatus.LURKING);
		}
		long min = Math.min(tmpPen, tmpLurk);
		return (min < Long.MAX_VALUE ? min : -1);
	}

	/**
	 * Returns the start of the scheduling phase. This is always the same as the
	 * end of the planning phase.
	 * 
	 * @return Time in ms of the start of the scheduling phase.
	 */
	public long getSchedStart() {
		return getPlanEnd();
	}

	public long getSchedEnd() {
		if (statusTimes.containsKey(BMLBlockStatus.REVOKED)) {
			return statusTimes.get(BMLBlockStatus.REVOKED);
		}
		if (statusTimes.containsKey(BMLBlockStatus.IN_EXEC)) {
			return statusTimes.get(BMLBlockStatus.IN_EXEC);
		} else {
			return -1;
		}
	}

	/**
	 * Returns the start of the playing phase.
	 * 
	 * @return Time in ms of the start of the playing phase.
	 */
	public long getPlayStart() {
		if (statusTimes.containsKey(BMLBlockStatus.IN_EXEC)) {
			return statusTimes.get(BMLBlockStatus.IN_EXEC);
		} else {
			return -1;
		}
	}

	public long getPlayEnd() {
		if (statusTimes.containsKey(BMLBlockStatus.DONE)) {
			return statusTimes.get(BMLBlockStatus.DONE);
		} else if (statusTimes.containsKey(BMLBlockStatus.INTERRUPTED)) {
			return statusTimes.get(BMLBlockStatus.INTERRUPTED);
		} else {
			return -1;
		}
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
	 * blocks. So far only the border color is changed in this case. Update:
	 * This is not desired anymore.
	 */
	public void isInterruptBlock() {
		// borderColor = Color.RED;
	}

	/**
	 * Return the status this block was in at the given time. If timestamp is
	 * negative, the current status of the block is returned.
	 * 
	 * @param timestamp
	 *            The time since the program started running in ms.
	 * @return The status the block was in at the given time.
	 */
	private BMLBlockStatus getStatusAt(long timestamp) {
		if (timestamp < 0) {
			return curStatus;
		}
		BMLBlockStatus lastStatus = BMLBlockStatus.NONE;
		long lastTime = 0;
		for (Entry<BMLBlockStatus, Long> e : statusTimes.entrySet()) {
			if (e.getValue() <= timestamp && e.getValue() > lastTime) {
				lastStatus = e.getKey();
				lastTime = e.getValue();
			}
		}
		return lastStatus;
	}

	/**
	 * Return the status string this block was in at the given time
	 * 
	 * @param timestamp
	 *            The time since the program started running in ms.
	 * @return The name of the status the block was in at the given time.
	 */
	public String getStatusStringAt(long timestamp) {
		return getStatusAt(timestamp).name();
	}

	public static Color getColorFor(BMLBlockStatus status) {
		switch (status) {
		case DONE:
			return Color.GREEN;
		case FAILED:
			return Color.RED;
		case IN_EXEC:
			return Color.GREEN;
		case IN_PREP:
			return Color.YELLOW;
		case INTERRUPTED:
			return Color.GREEN;
		case LURKING:
			return new Color(153, 76, 0);
		case PENDING:
			return Color.ORANGE;
		case REVOKED:
			return Color.LIGHT_GRAY;
		case SUBMITTED:
			return new Color(255, 251, 223);
		case NONE:
			return null;
		case INTERRUPT_REQUESTED:
			// Interrupt_Requested does not have a color on it's own since it
			// can happen at any moment.
			return null;
		//Not used yet
		case SUBSIDING:
			return Color.WHITE;
		default:
			return Color.WHITE;
		}
		
	}
}
