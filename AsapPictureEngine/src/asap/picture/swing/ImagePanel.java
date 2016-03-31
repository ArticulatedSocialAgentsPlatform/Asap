/*******************************************************************************
 *******************************************************************************/
package asap.picture.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Collection;

import javax.swing.JComponent;

public class ImagePanel extends JComponent
{
    private static final long serialVersionUID = 1L;

    private Collection<Image> images;

    public ImagePanel()
    {
        super();        
    }

    public void drawPicture(Collection<Image> images)
    {
        this.images = images;
        repaint();
    }

    @Override
    public void paintComponent(Graphics g)
    {
    	if (images != null && images.size() > 0)
        {
            // System.out.println("nrimg: " +images.size());
            // iterate over all images in the collection, drawing each one on top of the other
            for (Image img : images)
            {
                g.drawImage(img, 0, 0, null);
            }
        }
        else
        {
            // images set is currently empty, so clear the display
            g.clearRect(0, 0, WIDTH, HEIGHT);
        }
    }
}
