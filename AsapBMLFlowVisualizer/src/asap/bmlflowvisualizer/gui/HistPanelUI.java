package asap.bmlflowvisualizer.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.plaf.LayerUI;

public class HistPanelUI extends LayerUI<ScrollPanel> {

	private int timeLinePos;

	public void setTimeLinePos(int pos) {
		timeLinePos = pos;
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		super.paint(g, c);

		Graphics2D g2 = (Graphics2D) g.create();

		int w = c.getWidth();
		g2.setColor(Color.RED);
		g2.setStroke(new BasicStroke(2.0f));
		g2.drawLine(timeLinePos, 0, timeLinePos, c.getHeight());

		g2.dispose();
	}
}
