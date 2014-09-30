package asap.srnao.swing;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JPanel;

import lombok.Delegate;
import asap.srnao.display.PictureDisplay;

public class PictureJFrame implements PictureDisplay {

    @Delegate
    private PictureJComponent pictureComponent;
    
    public PictureJFrame() {
        init();
    }

    private void init() {
        final JFrame frame = new JFrame();
        JPanel jPanel = new JPanel();
        frame.getContentPane().add(jPanel);
        pictureComponent = new PictureJComponent(jPanel);
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
