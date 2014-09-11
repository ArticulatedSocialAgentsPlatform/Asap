package asap.bmlflowvisualizer.gui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class DetailPopup extends JFrame {

	public DetailPopup(String s) {
		super();
		JTextArea textArea = new JTextArea();
		textArea.setText(s);
		textArea.setEditable(false);
		this.add(new JScrollPane(textArea));
		this.setPreferredSize(new Dimension(500,500));
		this.pack();
		this.setVisible(true);
	}
}
