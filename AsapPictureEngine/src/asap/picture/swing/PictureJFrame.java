/*******************************************************************************
 *******************************************************************************/
package asap.picture.swing;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import lombok.Delegate;
import asap.picture.display.PictureDisplay;

public class PictureJFrame implements PictureDisplay {

    @Delegate
    private PictureJComponent pictureComponent;
    private int width = 400; 
    private int height = 400;
    
    public PictureJFrame() {
    	this (400,400);
    }
    
    public PictureJFrame(int w, int h) {
    	width = w;
    	height = h;
        init();
    }

    private void init() {
        final JFrame frame = new JFrame();
        JPanel jPanel = new JPanel();
        jPanel.setSize(width,height);
        frame.getContentPane().add(jPanel);
        pictureComponent = new PictureJComponent(jPanel, width, height);
        frame.pack();
        frame.setVisible(true);
        //Dirty exit code
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.dispose();
                System.exit(0);
            }
        });
    }
    
    
    
}
