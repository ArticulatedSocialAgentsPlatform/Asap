package asap.bmlflowvisualizer.gui;

import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.Scrollable;

public class ScrollPanel extends JPanel implements Scrollable {
	
	private static final long serialVersionUID = 4377045349264326581L;

	@Override
	public int getHeight() {
		if (super.getHeight() <= 0) {
			return 1;
		} else {
			return super.getHeight();
		}
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
	public Dimension getPreferredScrollableViewportSize() {
		return this.getPreferredSize();
	}

	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 0;
	}

	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return 0;
	}

	@Override
	public boolean getScrollableTracksViewportWidth() {
		return false;
	}

	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}


}
