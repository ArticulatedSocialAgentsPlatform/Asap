package asap.bmlflowvisualizer.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Timer;

/**
 * Custom mouse adapter to distinguish between single and double click. 
 * @author jpoeppel
 *
 */
public class ClickListener extends MouseAdapter implements ActionListener{
	
	private Timer timer;
	private MouseEvent lastEvent;
	
	public ClickListener(int interval) {
		timer = new Timer(interval, this);
	}
	
	@Override
	public void mouseClicked (MouseEvent e) {
		if (e.getClickCount() > 2) return;

        lastEvent = e;

        if (timer.isRunning())
        {
            timer.stop();
            doubleClick( lastEvent );
        }
        else
        {
            timer.restart();
        }
	}
	
	@Override
	public void actionPerformed(ActionEvent e)
    {
        timer.stop();
        singleClick( lastEvent );
    }
	
	public void singleClick(MouseEvent e) {}
    public void doubleClick(MouseEvent e) {}

}
