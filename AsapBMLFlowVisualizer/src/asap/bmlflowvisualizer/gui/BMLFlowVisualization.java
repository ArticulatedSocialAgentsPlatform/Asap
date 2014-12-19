package asap.bmlflowvisualizer.gui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JLayer;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;


import asap.bml.ext.bmla.BMLABMLBehaviorAttributes;
import asap.bmlflowvisualizer.BMLFlowVisualizerPort;
import asap.bmlflowvisualizer.graphutils.DAGUtils;
import asap.bmlflowvisualizer.graphutils.Edge;
import asap.bmlflowvisualizer.utils.BMLBlock;
import asap.bmlflowvisualizer.utils.BMLBlockComperator;
import asap.bmlflowvisualizer.utils.BMLBlockStatus;
import asap.bmlflowvisualizer.utils.ClickListener;
import asap.bmlflowvisualizer.utils.ConnectionType;

/**
 * Main visualization panel. Also handles all gui related events and controls
 * different dialogs.
 * 
 * @author jpoeppel
 *
 */
@SuppressWarnings("serial")
public class BMLFlowVisualization extends JPanel {
	/** Time between updates */
	private final int PLAY_INTERVAL = 100;
	/** Time between 2 clicks to be recognized as double click */
	public static final int DBCLICK_INTERVAL = 200;

	private Map<String, BMLBlock> bmlBlocks = Collections
			.synchronizedMap(new HashMap<String, BMLBlock>());

	// Visualization Components
	private JPanel detailPanel;
	private ContentPanel detailPlannedPanel;
	private ContentPanel detailScheduledPanel;
	private ScrollPanel histPanel;
	private HistPanelUI histPanelUI;
	private ContentPanel histPlannedPanel;
	private ContentPanel histScheduledPanel;
	private ContentPanel histPlayedPanel;
	private JScrollPane scrollPane;
	private JCheckBox feedbackOnly;
	private JSlider zoomSlider;
	private JButton playB;
	private JTextField timeField;
	// Visualization variables
	public static final int BLOCK_HEIGHT = 20;
	public static final int BLOCK_WIDTH = 100;
	private int maxHistWidth = 500;
	private int curHistPanelWidth;
	private double pixPerSec = maxHistWidth / 10;
	private boolean lockScrolling;

	// Auxiliary variables
	private long lastFeedback;
	private long curTimestamp;
	private boolean curTimeAtEnd;
	private long lastTimestamp;

	private boolean running; // Determines if the history update is running
	private boolean playing; // Determines if the curTime is running
	private Thread playThread;

	private List<BMLBlockPopup> activePopups = new ArrayList<BMLBlockPopup>();
	private SearchDialog searchDialog;
	private InfoScreen infoScreen;

	private BMLFlowVisualizerPort port;

	public BMLFlowVisualization(BMLFlowVisualizerPort port,
			Map<String, BMLBlock> bmlBlocks) {
		this.port = port;
		this.setLayout(new BorderLayout());
		this.bmlBlocks = bmlBlocks;
		curHistPanelWidth = 1;
		lockScrolling = true;
		running = false;
		playing = true;
		curTimeAtEnd = true;
		histPanel = buildHistoryPanel();
		histPanelUI = new HistPanelUI();
		JLayer<ScrollPanel> jlayer = new JLayer<ScrollPanel>(histPanel,
				histPanelUI);

		histPanel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
		scrollPane = new JScrollPane(jlayer);
		scrollPane.setAlignmentX(JScrollPane.RIGHT_ALIGNMENT);
		scrollPane.getViewport().setAlignmentX(JScrollPane.RIGHT_ALIGNMENT);
		scrollPane.setPreferredSize(new Dimension(maxHistWidth, 600));
		scrollPane.getViewport().setBackground(Color.GRAY);

		scrollPane.getHorizontalScrollBar().addAdjustmentListener(
				new AdjustmentListener() {

					BoundedRangeModel brm = scrollPane.getHorizontalScrollBar()
							.getModel();

					@Override
					public void adjustmentValueChanged(AdjustmentEvent e) {

						if (!brm.getValueIsAdjusting()) {
							if (lockScrolling)
								brm.setValue(brm.getMaximum());
						} else {
							lockScrolling = ((brm.getValue() + brm.getExtent()) == brm
									.getMaximum());
						}
					}
				});
		detailPanel = buildDetailPanel();
		// scrollPane.getViewport().setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.add(scrollPane, BorderLayout.WEST);
		this.add(buildControlPanel(), BorderLayout.SOUTH);
		this.add(detailPanel, BorderLayout.EAST);
		this.add(buildMenuBar(), BorderLayout.NORTH);

		playThread = new Thread(createPlayer());
		playThread.start();

	}

	private void performSingleClick(MouseEvent e) {
		curTimestamp = (long) (e.getX() / pixPerSec * 1000);
		curTimeAtEnd = false;
		playing = false;
		playB.setText("Play");
		lockScrolling = false;
		histPanelUI.setTimeLinePos(e.getX());
		timeField.setText(String.valueOf(curTimestamp * 0.001f));
		updateDetailVisualisation(curTimestamp);
		histPanel.repaint();
	}

	private void performDoubleClick(MouseEvent e, int field) {
		BMLBlock b = findIntersection(e.getX(), e.getY(), field);
		if (b != null) {
			if (noActivePopupFor(b)) {
				BMLBlockPopup popup = new BMLBlockPopup(this, b, curTimestamp);
				activePopups.add(popup);
			}
		}
	}

	public BMLBlock findIntersection(int x, int y, int field) {
		for (BMLBlock b : bmlBlocks.values()) {
			if (b.intersects(x, y, field)) {
				return b;
			}
		}
		return null;
	}

	/**
	 * Builds the history panel with its sup components
	 * 
	 * @return the complete history panel
	 */
	private ScrollPanel buildHistoryPanel() {
		ScrollPanel panel = new ScrollPanel();
		panel.setBackground(Color.CYAN);
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setPreferredSize(new Dimension(1, 600));
		histPlannedPanel = new ContentPanel();
		histPlannedPanel.addMouseListener(new ClickListener(DBCLICK_INTERVAL) {
			@Override
			public void singleClick(MouseEvent e) {
				performSingleClick(e);
			}

			@Override
			public void doubleClick(MouseEvent e) {
				performDoubleClick(e, 0);
			}
		});
		panel.add(histPlannedPanel);
		histScheduledPanel = new ContentPanel();
		histScheduledPanel
				.addMouseListener(new ClickListener(DBCLICK_INTERVAL) {
					@Override
					public void singleClick(MouseEvent e) {
						performSingleClick(e);
					}

					@Override
					public void doubleClick(MouseEvent e) {
						performDoubleClick(e, 1);
					}
				});
		panel.add(histScheduledPanel);
		histPlayedPanel = new ContentPanel();
		histPlayedPanel.addMouseListener(new ClickListener(DBCLICK_INTERVAL) {
			@Override
			public void singleClick(MouseEvent e) {
				performSingleClick(e);
			}

			@Override
			public void doubleClick(MouseEvent e) {
				performDoubleClick(e, 2);
			}
		});
		panel.add(histPlayedPanel);
		return panel;
	}

	/**
	 * Builds the Control panel with the all the buttons and defines button
	 * logic
	 * 
	 * @return The complete control panel
	 */
	private JPanel buildControlPanel() {
		JPanel panel = new JPanel();
		timeField = new JTextField();
		timeField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				String oldString = "";
				if (timeField.getText().equals("")) {
					oldString = "";
					return;
				} else {
					// if you cannot parse the string as an int, or float,
					// then change the text to the text before (means: ignore
					// the user input)
					try {
						Float.parseFloat(timeField.getText());
						oldString = timeField.getText();

					} catch (NumberFormatException el) {
						timeField.setText(oldString);
					}
				}
			}
		});
		timeField.setPreferredSize(new Dimension(150, 20));
		timeField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					jumpToTime((long)(Float.parseFloat(timeField.getText())*1000));	
				} catch(NumberFormatException el){
					System.out.println("Incorrect time value.");
				}
				
			}
		});
		panel.add(timeField);

		JButton jtTB = new JButton("Jump to time");
		jtTB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					jumpToTime((long)(Float.parseFloat(timeField.getText())*1000));	
				} catch(NumberFormatException el){
					System.out.println("Incorrect time value.");
				}
			}
		});
		panel.add(jtTB);
		zoomSlider = new JSlider(1, 30);
		zoomSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				pixPerSec = ((double) maxHistWidth) / zoomSlider.getValue();
				if (port.isFirstTimestampKnown()) {
					updateHistory(lastTimestamp);
				}
				updateHistoryVisualisation();
			}
		});
		zoomSlider.setValue(10);
		panel.add(zoomSlider);
		playB = new JButton("Pause");
		playB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Pause/Play Button logic
				if (playing) { // Will pause now
					playing = false;
					playB.setText("Play");
					curTimeAtEnd = false;
					lockScrolling = false;
				} else {
					playB.setText("Pause");
					playing = true;
				}
			}
		});
		panel.add(playB);
		JButton gtEB = new JButton("Go to End");
		gtEB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				curTimeAtEnd = true;
				lockScrolling = true;
				curTimestamp = (long) (curHistPanelWidth / pixPerSec * 1000);
				histPanelUI.setTimeLinePos(curHistPanelWidth);
				timeField.setText(String.valueOf(curTimestamp * 0.001));
				updateDetailVisualisation(curTimestamp);

				histPanel.repaint();
			}
		});
		panel.add(gtEB);
		feedbackOnly = new JCheckBox("Update on feedback only");
		panel.add(feedbackOnly);

		return panel;
	}

	private JPanel buildDetailPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setPreferredSize(new Dimension(maxHistWidth, 600));
		detailPlannedPanel = new ContentPanel();
		detailPlannedPanel
				.addMouseListener(new ClickListener(DBCLICK_INTERVAL) {

					@Override
					public void doubleClick(MouseEvent e) {
						performDoubleClick(e, 3);
					}
				});
		JLabel detailPlannedLabel = new JLabel("Planned Status");
		panel.add(detailPlannedLabel);
		panel.add(detailPlannedPanel);
		JLabel detailScheduledLabel = new JLabel("Scheduled status");
		panel.add(detailScheduledLabel);
		detailScheduledPanel = new ContentPanel();
		detailScheduledPanel.addMouseListener(new ClickListener(
				DBCLICK_INTERVAL) {

			@Override
			public void doubleClick(MouseEvent e) {
				performDoubleClick(e, 4);
			}
		});
		JScrollPane detailSchedPane = new JScrollPane(detailScheduledPanel);
		detailSchedPane.setBorder(BorderFactory.createEmptyBorder());
		panel.add(detailSchedPane);
		return panel;
	}

	private JMenuBar buildMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Menu");
		JMenuItem item = new JMenuItem("Search");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Search Button logic
				if (searchDialog == null) {
					openSearchDialog();
				}
			}
		});
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
				ActionEvent.CTRL_MASK));
		menu.add(item);

		item = new JMenuItem("Save");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Save Button logic
				port.saveAs();
			}
		});
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,
				ActionEvent.CTRL_MASK));
		menu.add(item);
		item = new JMenuItem("Load");
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Load Button logic
				port.load();
			}
		});
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,
				ActionEvent.CTRL_MASK));
		menu.add(item);

		item = new JMenuItem("Info");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I,
				ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Info Button logic
				if (infoScreen == null) {
					openInfoDialog();
				}
			}
		});

		menu.add(item);
		item = new JMenuItem("Exit");
		item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				ActionEvent.CTRL_MASK));
		item.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// Exit Button logic
				System.exit(0);
			}
		});
		menu.add(item);

		menuBar.add(menu);
		return menuBar;
	}

	/**
	 * Creates the runnable that handles to periodic update of the visualization
	 * 
	 * @return The created runnable
	 */
	private Runnable createPlayer() {
		return new Runnable() {

			@Override
			public void run() {
				try {
					while (true) {
						if (playing) {
							if (curTimestamp < lastTimestamp - PLAY_INTERVAL) {
								curTimestamp += PLAY_INTERVAL;
							} else {
								curTimestamp = lastTimestamp;
								playing = false;
								playB.setText("Play");
								curTimeAtEnd = true;
							}

							int posX = (int) (curTimestamp * 0.001 * pixPerSec) + 1;
							histPanelUI.setTimeLinePos(posX);
							timeField.setText(String
									.valueOf(curTimestamp * 0.001));
							updateDetailVisualisation(curTimestamp);
							// Scroll so that the timeLine is visible
							histPanel.scrollRectToVisible(new Rectangle(
									new Point(posX, 0)));
							histPanel.repaint();

						}
						if (running && !feedbackOnly.isSelected()) {

							long time = System.currentTimeMillis()
									- port.getFirstTimestamp();
							if (time - lastFeedback > 5000) {
								running = false;
								playing = false;
								playB.setText("Play");
							}

							if (curTimeAtEnd) {
								curTimestamp = time;
							}
							if (time > lastTimestamp) {
								lastTimestamp = time;
							}
							updateHistory(time);
							updateVisualisation();
						}

						Thread.sleep(PLAY_INTERVAL);
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
	}

	/**
	 * Updates the history visualization as well as the detail visualization
	 */
	public void updateVisualisation() {
		updateHistoryVisualisation();
		if (curTimeAtEnd) {
			updateDetailVisualisation(curTimestamp);
		}

	}

	/**
	 * Method to set the current time to the given time.
	 * 
	 * @param time
	 *            The time that should be jumped to.
	 */
	public void jumpToTime(long time) {
		playing = false;
		curTimestamp = time;
		curTimeAtEnd = false;
		if (time < 0) {
			curTimestamp = 0;
		}
		if (time > lastTimestamp){
			curTimestamp = lastTimestamp;
			curTimeAtEnd = true;
		}
		lockScrolling = false;
		playB.setText("Play");
		int posX = (int) (curTimestamp * 0.001 * pixPerSec) + 1;
		histPanelUI.setTimeLinePos(posX);
		timeField.setText(String.valueOf(curTimestamp * 0.001));
		histPanel.scrollRectToVisible(new Rectangle(new Point(posX, 0)));
		histPanel.repaint();
		updateDetailVisualisation(curTimestamp);
	}
	/**
	 * Updates the detail visualization to display the given timestamp
	 * 
	 * @param timestamp
	 *            The timestamp which situation is to be visualized
	 */
	private void updateDetailVisualisation(long timestamp) {
		// Update the popups.
		updatePopups();

		ArrayList<BMLBlock> plannedBlocks = new ArrayList<BMLBlock>();
		ArrayList<BMLBlock> scheduledBlocks = new ArrayList<BMLBlock>();
		ArrayList<BMLBlock> playingBlocks = new ArrayList<BMLBlock>();
		// Order blocks according to their status
		for (BMLBlock b : bmlBlocks.values()) {
			if ((b.getPlanStart() != -1 && b.getPlanStart() <= timestamp)
					&& (b.getPlanEnd() == -1 || b.getPlanEnd() >= timestamp)) {
				plannedBlocks.add(b);
			} else {
				b.setPosition(new Point(0, 0), 0,
						VisualisationField.DetailPlanned);
			}
			if ((b.getSchedStart() != -1 && b.getSchedStart() <= timestamp)
					&& (b.getSchedEnd() == -1 || b.getSchedEnd() >= timestamp)) {
				scheduledBlocks.add(b);
			} else {
				b.setPosition(new Point(0, 0), 0,
						VisualisationField.DetailScheduled);
			}
			if ((b.getPlayStart() != -1 && b.getPlayStart() <= timestamp)
					&& (b.getPlayEnd() == -1 || b.getPlayEnd() > timestamp)) {
				scheduledBlocks.add(b);
				playingBlocks.add(b);
			} else {
				b.setPosition(new Point(0, 0), 0,
						VisualisationField.DetailScheduled);
			}
		}
		drawOnDetailPlanned(plannedBlocks, timestamp);
		drawOnDetailScheduled(playingBlocks, scheduledBlocks, timestamp);
		detailScheduledPanel.revalidate();
		detailPanel.revalidate();
		detailPanel.repaint();
	}

	/**
	 * Updates all three history visualizations.
	 */
	private void updateHistoryVisualisation() {
		drawOnPlayed();
		drawOnScheduled();
		drawOnPlanned();
		histPanel.revalidate();
		histPanel.repaint();
	}

	/**
	 * Draws the blocks on the detailed planned panel
	 * 
	 * @param blocks
	 *            The blocks to be drawn.
	 */
	private void drawOnDetailPlanned(ArrayList<BMLBlock> blocks, long timestamp) {
		BufferedImage img = new BufferedImage(detailPlannedPanel.getWidth(),
				detailPlannedPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setColor(detailPlannedPanel.getBackground());
		g.fillRect(0, 0, img.getWidth(), img.getHeight());
		int x = detailPlannedPanel.getX() + detailPlannedPanel.getWidth() / 2;
		int y = 20;
		for (BMLBlock b : blocks) {
			g.setColor(b.getColor(timestamp));
			g.fillRect(x, y, BLOCK_WIDTH, BLOCK_HEIGHT);
			g.setColor(b.getBorderColor(VisualisationField.DetailPlanned));
			g.drawRect(x, y, BLOCK_WIDTH, BLOCK_HEIGHT);
			
			g.setColor(Color.BLACK);
			FontMetrics metric = g.getFontMetrics();
			g.drawString(trunkStringToFit(b.getId(), BLOCK_WIDTH, metric),
					x + 5, y + BLOCK_HEIGHT / 2 + 2);
			b.setPosition(new Point(x, y), BLOCK_WIDTH,
					VisualisationField.DetailPlanned);
			y += (int) 1.25 * BLOCK_HEIGHT;
		}
		g.dispose();

		detailPlannedPanel.setImage(img);
	}

	/**
	 * Draws the blocks on the detailed scheduled panel
	 * 
	 * @param playingBlocks
	 *            The currently playing blocks
	 * @param scheduledBlocks
	 *            The currently scheduled blocks
	 */
	private void drawOnDetailScheduled(ArrayList<BMLBlock> playingBlocks,
			ArrayList<BMLBlock> scheduledBlocks, long timestamp) {

		int wgap = 60;
		int hgap = 20;
		List<Edge<String>> edges = new ArrayList<Edge<String>>();
		List<String> vertices = new ArrayList<String>();
		Map<Edge<String>, ConnectionType> edgeTypes = new HashMap<Edge<String>, ConnectionType>();
		for (BMLBlock b : scheduledBlocks) {
			if (b.getBb() == null) {
				continue;
			}
			BMLABMLBehaviorAttributes bmlaAttr = b.getBb()
					.getBMLBehaviorAttributeExtension(
							BMLABMLBehaviorAttributes.class);
			if (bmlaAttr != null) {
				for (String id : bmlaAttr.getChunkBeforeList()) {
					if (containBlockWithId(scheduledBlocks, id)) {
						Edge<String> e = new Edge<String>(b.getId(), id);
						edges.add(e);
						edgeTypes.put(e, ConnectionType.Chunk);

					}
				}
				for (String id : bmlaAttr.getPrependBeforeList()) {
					if (containBlockWithId(scheduledBlocks, id)) {
						Edge<String> e = new Edge<String>(b.getId(), id);
						edges.add(e);
						edgeTypes.put(e, ConnectionType.Append);
					}
				}
				for (String id : bmlaAttr.getChunkAfterList()) {
					if (containBlockWithId(scheduledBlocks, id)) {
						Edge<String> e = new Edge<String>(id, b.getId());
						edges.add(e);
						edgeTypes.put(e, ConnectionType.Chunk);
					}
				}
				for (String id : bmlaAttr.getAppendAfterList()) {
					if (containBlockWithId(scheduledBlocks, id)) {
						Edge<String> e = new Edge<String>(id, b.getId());
						edges.add(e);
						edgeTypes.put(e, ConnectionType.Append);
					}
				}
			}
			vertices.add(b.getId());
		}

		final Map<String, Point> layout = DAGUtils.layout(vertices, edges);
		// Determine maxY in layout to calculate new width of panel
		int maxY = 0;
		for (Point p : layout.values()) {
			if (p.y > maxY) {
				maxY = p.y;
			}
		}
		int newWidth = (maxY + 1) * (BLOCK_WIDTH + wgap);
		detailScheduledPanel.setPreferredSize(new Dimension(newWidth, 1));

		BufferedImage img = new BufferedImage(newWidth,
				detailScheduledPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();

		// Clear old drawings
		g.setColor(detailScheduledPanel.getBackground());
		g.fillRect(0, 0, img.getWidth(), img.getHeight());

		int w, h, x, y;
		for (Entry<String, Point> entry : layout.entrySet()) {
			// Same as the one in blocks but allows faster access
			BMLBlock b = bmlBlocks.get(entry.getKey());
			g.setColor(b.getColor(timestamp));
			w = BLOCK_WIDTH;
			h = BLOCK_HEIGHT;
			x = (entry.getValue().y) * (w + wgap);
			y = (entry.getValue().x + 1) * (h + hgap);
//			if (playingBlocks.contains(b)) {
//				g.setColor(Color.GREEN);
//			} else {
//				g.setColor(Color.ORANGE);
//			}
			g.fillRect(x, y, w, h);
			if (playingBlocks.contains(b)) {
				// Playing blocks need to be handled the same as in the hist
				// played field
				g.setColor(b.getBorderColor(VisualisationField.HistPlayed));
			} else {
				g.setColor(b.getBorderColor(VisualisationField.DetailScheduled));
			}
			g.setStroke(new BasicStroke(b.getBorderThickness()));
			g.drawRect(x, y, w, h);
			g.setColor(Color.BLACK);
			FontMetrics metric = g.getFontMetrics();
			g.drawString(trunkStringToFit(entry.getKey(), w, metric), x + 5, y
					+ h / 2 + 2);
			bmlBlocks.get(entry.getKey()).setPosition(new Point(x, y), w,
					VisualisationField.DetailScheduled);
		}

		// Draw the edges
		g.setColor(Color.BLACK);
		for (Edge<String> e : edges) {
			BMLBlock b1 = bmlBlocks.get(e.getEnd());
			BMLBlock b2 = bmlBlocks.get(e.getStart());
			// Dashed line : append after
			if (edgeTypes.get(e) == ConnectionType.Append) {
				Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT,
						BasicStroke.JOIN_BEVEL, 0, new float[] { 9 }, 0);
				g.setStroke(dashed);
			}
			// complete line: chunk after
			if (edgeTypes.get(e) == ConnectionType.Chunk) {
				Stroke line = new BasicStroke();
				g.setStroke(line);
			}

			g.drawLine(b1.getPosition(4).x, b1.getPosition(4).y + BLOCK_HEIGHT
					/ 2, b2.getPosition(4).x + BLOCK_WIDTH, b2.getPosition(4).y
					+ BLOCK_HEIGHT / 2);
		}
		g.dispose();
		detailScheduledPanel.setImage(img);
	}

	/**
	 * Draws all blocks on the given image according the field
	 * 
	 * @param img
	 *            The image that should be drawn upon
	 * @param field
	 *            The visualisationField (planned, scheduled, played) that is to
	 *            be drawn upon
	 */
	private void drawHist(BufferedImage img, VisualisationField field) {
		Graphics2D g = img.createGraphics();
		Map<Integer, Long> layers = new HashMap<Integer, Long>();
		int yBase = 100;
		int y, xStart, xEnd;
		long start = 0;
		long end = 0;
		Color col = Color.WHITE;
		Color border = Color.BLACK;

		// Create ordered list.
		BMLBlockComperator comp = new BMLBlockComperator(field);
		ArrayList<BMLBlock> blocks = new ArrayList<BMLBlock>(bmlBlocks.values());
		Collections.sort(blocks, comp);

		for (BMLBlock b : blocks) {

			switch (field) {
			case HistPlanned:
				start = b.getPlanStart();
				if (b.getStatusTime(BMLBlockStatus.IN_PREP) > 0) {
					end = b.getStatusTime(BMLBlockStatus.IN_PREP);
				} else {
					end = b.getPlanEnd();
				}
				col = BMLBlock.getColorFor(BMLBlockStatus.SUBMITTED);
				break;
			case HistScheduled:
				start = b.getSchedStart();
				if (b.getStatusTime(BMLBlockStatus.LURKING) > 0) {
					end = b.getStatusTime(BMLBlockStatus.LURKING);
				} else {
					end = b.getSchedEnd();
				}
				col = BMLBlock.getColorFor(BMLBlockStatus.PENDING);
				break;
			case HistPlayed:
				start = b.getPlayStart();
				end = b.getPlayEnd();
				col = BMLBlock.getColorFor(BMLBlockStatus.IN_EXEC);
				border = b.getBorderColor(field);
				break;
			default:
				break;
			}
			if (start > 0) {
				// Determine layer
				int l = 0;
				boolean found = false;
				while (!found) {
					if (layers.get(l) == null || layers.get(l) <= start) {
						found = true;
					} else {
						l++;
					}
				}

				// Set y coordinate
				y = yBase - l * BLOCK_HEIGHT;
				// Calculate xStart, xEnd from zoom level and play duration
				xStart = (int) (start * 0.001 * pixPerSec);
				if (end < 0) {
					xEnd = curHistPanelWidth;
					end = Long.MAX_VALUE;
				} else {
					xEnd = (int) (end * 0.001 * pixPerSec);
				}

				// Draw block
				g.setColor(col);
				g.fillRect(xStart, y, xEnd - xStart, BLOCK_HEIGHT);

				if (field == VisualisationField.HistScheduled) {
					// Draw Lurking if necessary
					int tmpStart = xEnd;
					if (b.getStatusTime(BMLBlockStatus.LURKING) > 0) {
						if (b.getStatusTime(BMLBlockStatus.IN_EXEC) > 0) {
							xEnd = (int) (b.getSchedEnd() * 0.001 * pixPerSec);
						} else {
							xEnd = curHistPanelWidth;
						}
					}
					g.setColor(BMLBlock.getColorFor(BMLBlockStatus.LURKING));
					g.fillRect(tmpStart, y, xEnd - tmpStart, BLOCK_HEIGHT);
				} else if (field == VisualisationField.HistPlanned) {
					// Draw IN_PREP
					int tmpStart = xEnd;
					if (b.getStatusTime(BMLBlockStatus.IN_PREP) > 0) {
						// Test if the Block has finished planning
						if (b.getSchedStart() > 0) {
							xEnd = (int) (b.getSchedStart() * 0.001 * pixPerSec);
						} else {
							xEnd = curHistPanelWidth;
						}
					}
					g.setColor(BMLBlock.getColorFor(BMLBlockStatus.IN_PREP));
					g.fillRect(tmpStart, y, xEnd - tmpStart, BLOCK_HEIGHT);
				}
				// Set border color
				g.setColor(border);
				g.setStroke(new BasicStroke(b.getBorderThickness()));
				g.drawRect(xStart, y, xEnd - xStart, BLOCK_HEIGHT);
				g.setColor(Color.BLACK);
				FontMetrics metric = g.getFontMetrics();

				g.drawString(
						trunkStringToFit(b.getId(), xEnd - xStart, metric),
						xStart + 2, y + BLOCK_HEIGHT / 2 + 2);
				b.setPosition(new Point(xStart, y), xEnd - xStart, field);

				// Update layers
				layers.remove(l);
				layers.put(l, end);
			}
		}
		g.dispose();
	}

	/**
	 * Method to truncate a string so it fits into given width
	 * 
	 * @param s
	 *            The string to be truncated
	 * @param width
	 *            The width the string must not exceed
	 * @return The truncated string.
	 */
	private String trunkStringToFit(String s, int width, FontMetrics metric) {
		if (metric.stringWidth(s) < width) {
			return s;
		}
		while (metric.stringWidth(s + "..") > width && s.length() > 1) {
			s = s.substring(0, s.length() - 1);
		}
		return s + "..";
	}

	/**
	 * Method to draw on the played panel
	 */
	private void drawOnPlayed() {
		BufferedImage contImg = new BufferedImage(curHistPanelWidth,
				histPlayedPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
		drawHist(contImg, VisualisationField.HistPlayed);
		histPlayedPanel.setImage(contImg);
	}

	/**
	 * Method to draw on the scheduled panel
	 */
	private void drawOnScheduled() {
		BufferedImage contImg = new BufferedImage(curHistPanelWidth,
				histScheduledPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
		drawHist(contImg, VisualisationField.HistScheduled);
		histScheduledPanel.setImage(contImg);
	}

	/**
	 * Method to draw on the planned panel
	 */
	private void drawOnPlanned() {
		BufferedImage contImg = new BufferedImage(curHistPanelWidth,
				histPlannedPanel.getHeight(), BufferedImage.TYPE_INT_ARGB);
		drawHist(contImg, VisualisationField.HistPlanned);
		histPlannedPanel.setImage(contImg);
	}

	/**
	 * Helper method to check if a list contains a certain block.
	 * 
	 * @param blocks
	 *            The list to be checked
	 * @param id
	 *            The id to be looked for
	 * @return True if the list contains the block with the given id, false
	 *         otherwise
	 */
	private boolean containBlockWithId(ArrayList<BMLBlock> blocks, String id) {
		for (BMLBlock b : blocks) {
			if (b.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Update all active popups, including the search dialog
	 */
	private void updatePopups() {
		synchronized (activePopups) {
			for (BMLBlockPopup popup : activePopups) {
				popup.update(curTimestamp);
			}
		}
		if (searchDialog != null) {
			searchDialog.update(curTimestamp);
		}
	}

	/**
	 * Creates a popup for the given blockId if there is no active popup for
	 * this block.
	 * 
	 * @param blockId
	 *            The block id for the block which information is to be
	 *            displayed in the popup
	 */
	public void addPopup(String blockId) {
		BMLBlock b = bmlBlocks.get(blockId);
		if (noActivePopupFor(b)) {
			BMLBlockPopup popup = new BMLBlockPopup(this, b, curTimestamp);
			activePopups.add(popup);
		}
	}

	/**
	 * Method to check if there is already a popup for the given block active
	 * 
	 * @param block
	 *            The block to check
	 * @return True if there is no active popup for the given block, false
	 *         otherwise
	 */
	private boolean noActivePopupFor(BMLBlock block) {
		for (BMLBlockPopup popup : activePopups) {
			if (popup.getBMLBlock().equals(block)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Helper function to create the search dialog.
	 */
	private void openSearchDialog() {
		searchDialog = new SearchDialog(bmlBlocks, this);
	}

	/**
	 * Helper function to create the info dialog.
	 */
	private void openInfoDialog() {
		infoScreen = new InfoScreen(this);
	}

	/**
	 * Method to notify this class that the given popup was closed, since it
	 * does not need to be updated anymore.
	 * 
	 * @param bmlBlockPopup
	 *            The closed Popup
	 */
	public void notifyPopupClose(BMLBlockPopup bmlBlockPopup) {
		activePopups.remove(bmlBlockPopup);
	}

	/**
	 * Method to notify this class that the search dialog was closed, thus that
	 * it does not need to be updated anymore.
	 */
	public void notifySearchClose() {
		searchDialog = null;
	}

	/**
	 * Method to notify this class that the info screen was closed. So that a
	 * new one can be opened;
	 */
	public void notifyInfoClose() {
		infoScreen = null;
	}

	/**
	 * Updates the history panel's width to fit to the new time
	 * 
	 * @param time
	 *            The new maximum time the panel should display
	 */
	public void updateHistory(long time) {
		if (time > lastTimestamp) {
			lastTimestamp = time;
		}
		curHistPanelWidth = (int) (time * 0.001 * pixPerSec) + 1;
		histPanel.setPreferredSize(new Dimension(curHistPanelWidth, histPanel
				.getHeight()));
		int posX = (int) (curTimestamp * 0.001 * pixPerSec) + 1;
		histPanelUI.setTimeLinePos(posX);
		timeField.setText(String.valueOf(curTimestamp * 0.001));
	}

	/**
	 * Updates the visualization when new feedback came in.
	 * 
	 * @param time
	 *            The time of the last feedback
	 */
	public void update(long time) {
		lastFeedback = time;
		if (feedbackOnly.isSelected()) {
			updateHistory(time);
			updateVisualisation();
		}

		if (curTimeAtEnd) {
			curTimestamp = time;
		}
		running = true;
	}

	/**
	 * Resets the visualization.
	 */
	public void reset() {
		for (BMLBlockPopup popup : activePopups) {
			popup.dispose();
		}
		lastTimestamp = 0;
		curTimestamp = 0;
		lastFeedback = 0;
		lockScrolling = true;
		curTimeAtEnd = true;
		if (searchDialog != null) {
			searchDialog.reset();
		}

	}

}
