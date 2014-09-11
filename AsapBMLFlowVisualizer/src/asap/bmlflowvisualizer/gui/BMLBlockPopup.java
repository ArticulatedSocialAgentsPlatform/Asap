package asap.bmlflowvisualizer.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import asap.bmlflowvisualizer.BMLFlowVisualizerPort;
import asap.bmlflowvisualizer.utils.BMLBlock;
import asap.bmlflowvisualizer.utils.BMLBlockStatus;
import asap.bmlflowvisualizer.utils.ClickListener;
import asap.bmlflowvisualizer.utils.ListHighlightRenderer;

public class BMLBlockPopup extends JFrame {

	private BMLBlock bmlBlock;
	private JList<String> behaviourList;
	private JList<String> messageList;
	private JList<String> stateList;
	private BMLBlockPopup ref;
	private BMLFlowVisualization port;
	private ListHighlightRenderer<String> messageListRenderer;
	private ListHighlightRenderer<String> stateListRenderer;

	public BMLBlockPopup(BMLFlowVisualization bmlFlowVisualization,
			BMLBlock block, long time) {
		super();
		this.bmlBlock = block;
		this.port = bmlFlowVisualization;
		this.setTitle(block.getId());
		ref = this;
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.weightx = 0.5;
		c.weighty = 0.0;
		JPanel behaviourPanel = new JPanel();
		behaviourPanel.setLayout(new BoxLayout(behaviourPanel,
				BoxLayout.PAGE_AXIS));
		c.gridx = 0;
		c.gridy = 0;
		this.add(new JLabel("Behaviours"), c);
		c.gridx = 2;
		c.gridy = 0;
		this.add(new JLabel("States"), c);
		c.gridx = 1;
		c.gridy = 0;
		this.add(new JLabel("Messages"), c);
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 0.5;

		behaviourList = new JList<String>(block.getBehaviourList());
		behaviourPanel.add(behaviourList);
		c.gridx = 0;
		c.gridy = 1;
		this.add(new JScrollPane(behaviourPanel), c);
		JPanel messagePanel = new JPanel();
		messagePanel
				.setLayout(new BoxLayout(messagePanel, BoxLayout.PAGE_AXIS));

		messageList = new JList<String>(block.getMessageList());
		messageListRenderer = new ListHighlightRenderer<String>();
		messageList.setCellRenderer(messageListRenderer);
		messageList.addMouseListener(new ClickListener(200) {

			@Override
			public void singleClick(MouseEvent e) {

			}

			@Override
			public void doubleClick(MouseEvent e) {
				int index = messageList.getSelectedIndex();
				String info;
				if (index == 0) {
					info = bmlBlock.getBb().toString();
				} else {
					info = bmlBlock.getFeedbacks().get(index - 1)
							.getInformation();
				}
				new DetailPopup(info);
			}
		});
		messagePanel.add(messageList);
		c.gridx = 1;
		c.gridy = 1;
		this.add(new JScrollPane(messagePanel), c);
		JPanel statePanel = new JPanel();

		statePanel.setLayout(new BoxLayout(statePanel, BoxLayout.PAGE_AXIS));
		stateList = new JList<String>(block.getStateList());
		stateListRenderer = new ListHighlightRenderer<String>();
		stateList.setCellRenderer(stateListRenderer);
		statePanel.add(stateList);
		c.gridx = 2;
		c.gridy = 1;
		this.add(new JScrollPane(statePanel), c);

		JButton goToTime = new JButton("Jump to selected time");
		goToTime.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int index = messageList.getSelectedIndex();
				if (index != -1) {
					long time;
					if (index == 0) {
						time = bmlBlock.getPlanStart();
					} else {
						time = bmlBlock.getFeedbacks().get(index - 1)
								.getTimestamp();
					}
					port.jumpToTime(time);
				}
			}
		});

		c.gridx = 1;
		c.gridy = 2;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		this.add(goToTime, c);
		goToTime = new JButton("Jump to selected time");
		goToTime.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String value = stateList.getSelectedValue();
				if (value != null) {
					value = value.substring(0, value.indexOf(" "));
					BMLBlockStatus status = Enum.valueOf(BMLBlockStatus.class,
							value);
					port.jumpToTime(bmlBlock.getStatusTime(status));
				}
			}
		});

		c.gridx = 2;
		c.gridy = 2;
		c.weighty = 0;
		c.fill = GridBagConstraints.NONE;
		this.add(goToTime, c);

		JButton closeB = new JButton("Close");
		closeB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				port.notifyPopupClose(ref);
				ref.dispose();
			}
		});
		c.gridx = 2;
		c.gridy = 3;
		messageListRenderer.setIndexToHighlight(bmlBlock
				.getCurMessageListIndex(time));
		stateListRenderer.setIndexToHighlight(bmlBlock
				.getCurStateListIndex(time));
		this.add(closeB, c);
		this.setPreferredSize(new Dimension(600, 600));
		this.pack();
		this.setVisible(true);

	}

	public void update(long time) {
		int index = behaviourList.getSelectedIndex();
		behaviourList.setModel(bmlBlock.getBehaviourList());
		behaviourList.setSelectedIndex(index);
		index = messageList.getSelectedIndex();
		messageList.setModel(bmlBlock.getMessageList());
		messageList.setSelectedIndex(index);
		index = stateList.getSelectedIndex();
		stateList.setModel(bmlBlock.getStateList());
		stateList.setSelectedIndex(index);

		messageListRenderer.setIndexToHighlight(bmlBlock
				.getCurMessageListIndex(time));
		stateListRenderer.setIndexToHighlight(bmlBlock
				.getCurStateListIndex(time));
		this.repaint();
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);

		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			port.notifyPopupClose(this);
			this.dispose();
		}
	}

	public BMLBlock getBMLBlock() {
		return bmlBlock;
	}
}
