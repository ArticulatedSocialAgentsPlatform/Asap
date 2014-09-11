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
import javax.swing.plaf.basic.BasicBorders;

public class InfoScreen extends JFrame {
	
	private int blockHeight = 20;
	private int blockWidth = 100;
	private InfoScreen ref;
	private BMLFlowVisualization visualization;
	
	public InfoScreen(BMLFlowVisualization vis) {
		this.ref = this;
		this.visualization = vis;
		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		
		c.weightx = 0.5;
		c.weighty = 0.0;
		c.insets = new Insets(10,0,0,0);
		JLabel label = new JLabel("");
		label.setBackground(Color.GREEN);
		label.setOpaque(true);
		label.setPreferredSize(new Dimension(blockWidth,blockHeight));
		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		c.gridx = 0;
		c.gridy = 0;
		this.add(label, c);
		label = new JLabel("Currently playing or successfully played block.");
		c.gridx = 1;
		this.add(label,c);
		label = new JLabel("");
		label.setBackground(Color.YELLOW);
		label.setOpaque(true);
		label.setPreferredSize(new Dimension(blockWidth,blockHeight));
		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		c.gridx = 0;
		c.gridy = 1;
		this.add(label, c);
		label = new JLabel("Block is planned.");
		c.gridx = 1;
		this.add(label,c);
		label = new JLabel("");
		label.setBackground(Color.ORANGE);
		label.setOpaque(true);
		label.setPreferredSize(new Dimension(blockWidth,blockHeight));
		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		c.gridx = 0;
		c.gridy = 2;
		this.add(label, c);
		label = new JLabel("Block is in prending");
		c.gridx = 1;
		this.add(label,c);
		label = new JLabel("");
		label.setBackground(new Color(153, 76, 0));
		label.setOpaque(true);
		label.setPreferredSize(new Dimension(blockWidth,blockHeight));
		label.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		c.gridx = 0;
		c.gridy = 3;
		this.add(label, c);
		label = new JLabel("Block is lurking");
		c.gridx = 1;
		this.add(label,c);
		label = new JLabel("");
		label.setOpaque(true);
		label.setPreferredSize(new Dimension(blockWidth,blockHeight));
		label.setBorder(BorderFactory.createLineBorder(Color.RED));
		c.gridx = 0;
		c.gridy = 4;
		this.add(label, c);
		label = new JLabel("Red borders symbolize BML blocks that interrupt others.");
		c.gridx = 1;
		this.add(label,c);
		JButton closeB = new JButton("Close");
		closeB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				visualization.notifyInfoClose();
				ref.dispose();
			}
		});
		
		c.gridx= 1;
		c.gridy= 5;
		this.add(closeB,c);
		
		this.setPreferredSize(new Dimension(600,300));
		this.pack();
		this.setVisible(true);
	}

}
