package asap.bmlflowvisualizer.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import asap.bmlflowvisualizer.utils.BMLBlock;
import asap.bmlflowvisualizer.utils.BMLBlockStatus;

public class InfoScreen extends JFrame {

	private static final long serialVersionUID = -3154921841767836793L;
	private InfoScreen ref;
	private BMLFlowVisualization visualization;

	/**
	 * Creates the information screen. Showing the different meaning of the
	 * different block colours.
	 * 
	 * @param vis
	 *            Referenz to the BMLFlowVisualization. Needed to notify when
	 *            info screen is closed.
	 */
	public InfoScreen(BMLFlowVisualization vis) {
		this.ref = this;
		this.visualization = vis;
		this.setLayout(new GridBagLayout());
		this.setTitle("Information");
		GridBagConstraints c = new GridBagConstraints();
		Dimension blockDim = new Dimension(BMLFlowVisualization.BLOCK_WIDTH,
				BMLFlowVisualization.BLOCK_HEIGHT);
		c.weightx = 0.5;
		c.weighty = 0.0;
		c.insets = new Insets(10, 0, 0, 0);
		JLabel label = new JLabel(
				"<HTML><U>Block colours and their meaning:</U></HTML>");
		c.gridx = 0;
		c.gridy = 0;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 2;
		this.add(label, c);
		c.gridwidth = 1;
		label = new JLabel("");
		label.setBackground(BMLBlock.getColorFor(BMLBlockStatus.IN_EXEC));
		label.setOpaque(true);
		label.setPreferredSize(blockDim);
		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 1;
		this.add(label, c);
		label = new JLabel("Currently playing or successfully played block.");
		c.insets = new Insets(10, 10, 0, 0);
		c.gridx = 1;
		this.add(label, c);
		label = new JLabel("");
		label.setBackground(BMLBlock.getColorFor(BMLBlockStatus.SUBMITTED));
		label.setOpaque(true);
		label.setPreferredSize(blockDim);
		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 2;
		this.add(label, c);
		label = new JLabel("Block is submitted.");
		c.insets = new Insets(10, 10, 0, 0);
		c.gridx = 1;
		this.add(label, c);
		label = new JLabel("");
		label.setBackground(BMLBlock.getColorFor(BMLBlockStatus.IN_PREP));
		label.setOpaque(true);
		label.setPreferredSize(blockDim);
		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 3;
		this.add(label, c);
		label = new JLabel("Block is being planned.");
		c.insets = new Insets(10, 10, 0, 0);
		c.gridx = 1;
		this.add(label, c);
		label = new JLabel("");
		label.setBackground(BMLBlock.getColorFor(BMLBlockStatus.PENDING));
		label.setOpaque(true);
		label.setPreferredSize(blockDim);
		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 4;
		this.add(label, c);
		label = new JLabel("Block is pending.");
		c.insets = new Insets(10, 10, 0, 0);
		c.gridx = 1;
		this.add(label, c);
		label = new JLabel("");
		label.setBackground(BMLBlock.getColorFor(BMLBlockStatus.LURKING));
		label.setOpaque(true);
		label.setPreferredSize(blockDim);
		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 5;
		this.add(label, c);
		label = new JLabel("Block is lurking.");
		c.insets = new Insets(10, 10, 0, 0);
		c.gridx = 1;
		this.add(label, c);
		label = new JLabel("");
		label.setOpaque(true);
		label.setPreferredSize(blockDim);
		label.setBorder(BorderFactory.createLineBorder(Color.RED));
		c.insets = new Insets(10, 0, 0, 0);
		c.gridx = 0;
		c.gridy = 6;
		this.add(label, c);
		label = new JLabel(
				"Red borders symbolize interrupted BML blocks.");
		c.insets = new Insets(10, 10, 0, 0);
		c.gridx = 1;
		this.add(label, c);
		
		label = new JLabel(
				"<HTML>The left side symbolises the time history of all blocks. "
						+ "The top part for planned blocks, the middle for scheduled blocks and the "
						+ "lowest for played blocks. The right side displayes a detailed view for the "
						+ "current time (red line). A dotted line between 2 blocks means they have an append connection"
						+ "and a complete line means that they have a chunk connection.</HTML>");
		c.insets = new Insets(10, 0, 0, 0);
		c.gridwidth = 2;
		c.gridx= 0;
		c.weightx = 0;
		c.gridy = 7;
		this.add(label,c);
		label = new JLabel(
				"<HTML>Single click on left side will move the red line (current time) to the clicked "
						+ "position. Double click on any box will open a popup with information about the bml block.</HTML>");
		c.gridy=8;
		this.add(label,c);
		JButton closeB = new JButton("Close");
		closeB.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				visualization.notifyInfoClose();
				ref.dispose();
			}
		});

		c.gridx = 1;
		c.gridy = 9;
		c.fill = GridBagConstraints.NONE;
		this.add(closeB, c);

		this.setPreferredSize(new Dimension(600, 400));
		this.pack();
		this.setVisible(true);
	}

}
