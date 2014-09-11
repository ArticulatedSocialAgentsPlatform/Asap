package asap.bmlflowvisualizer.gui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public class ContentPanel extends JPanel {

	private BufferedImage image;

	public void setImage(BufferedImage img) {
		this.image = img;
	}

	private void doDrawing(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.drawImage(this.image, 0, 0, this);
	}
	
	@Override
	public int getWidth() {
		if (super.getWidth() <= 0) {
			return 1;
		} else {
			return super.getWidth();
		}
	}

	@Override
	public int getHeight() {
		if (super.getHeight() <= 0) {
			return 1;
		} else {
			return super.getHeight();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		doDrawing(g);
	}
}