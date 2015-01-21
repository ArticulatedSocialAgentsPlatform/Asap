/*******************************************************************************
 *******************************************************************************/
package asap.picture.planunit;

import hmi.util.Resources;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import asap.picture.display.PictureDisplay;
import asap.realizer.planunit.KeyPosition;

/**
 * Helper class for the AddAnimationXMLPU that loads an animation XML file.
 *
 * @author Jordi Hendrix
 */
public class AnimationXMLLoader {

    private List<TimeStringTuple> images;
    private List<KeyPosition> keyPositions;
    private PictureDisplay display = null;
    private double totalDuration = 0d;
    private InputStream xmlStream;
    private static Logger logger = LoggerFactory.getLogger(AnimationXMLLoader.class.getName());

    // Simple helper class to link a string id to a time (double)
    private static class TimeStringTuple {

        private double time;
        private String id;

        public TimeStringTuple(double time, String id) {
            this.time = time;
            this.id = id;
        }

        public double getTime() {
            return time;
        }

        public String getId() {
            return id;
        }
    }

    public AnimationXMLLoader(String xmlDir, String xmlFileName, PictureDisplay display) throws PUPrepareException {
        if (!xmlDir.endsWith("/")) {
            xmlDir += "/";
        }
        this.display = display;
        images = new ArrayList<TimeStringTuple>();
        keyPositions = new ArrayList<KeyPosition>();
        Resources r = new Resources(xmlDir);
        xmlStream = r.getInputStream(xmlFileName);
        if (xmlStream == null) {
            throw new PUPrepareException("Cannot find XML file containing animation at " + xmlDir + xmlFileName);
        } else {
            loadImages();
        }
    }

    /**
     * Parse the XML in the file and preload all referenced images. Also
     * populates the list of keyPositions.
     *
     * @throws PUPrepareException If the XML file is in an invalid format.
     */
    private void loadImages() throws PUPrepareException {
        List<TimeStringTuple> syncs = new ArrayList<TimeStringTuple>();

        //read XML
        String pictureDir = "";
        try {
            XMLTokenizer xmlHelper = new XMLTokenizer(xmlStream);
            HashMap<String, String> attribs;
            Resources r = null;
            if (xmlHelper.atSTag()) {
                
                // Opening tag must be xmlanim
                if (xmlHelper.getTagName().equals("xmlanim")) {
                    // Find picturePath attrib
                    attribs = xmlHelper.getAttributes();
                    pictureDir = attribs.get("picturePath");
                    if (pictureDir == null) {
                        throw new PUPrepareException("Invalid XML animation file format: xmlanim element does not contain picturePath attribute.");
                    }
                    r = new Resources(pictureDir);
                    logger.debug("picturePath set to {}", pictureDir);
                    xmlHelper.takeSTag("xmlanim");
                } else {
                    throw new PUPrepareException("Invalid XML animation file format: opening element should be xmlanim.");
                }
                
                // Parse frames and syncs
                while (!xmlHelper.atETag()) {
                    attribs = xmlHelper.getAttributes();
                    if (xmlHelper.getTagName().equals("frame")) {
                        String imageId = pictureDir + attribs.get("imgsrc");
                        
                        // Check validity of frame attribs
                        if (r.getInputStream(attribs.get("imgsrc")) == null) {
                            throw new PUPrepareException("Cannot find image " + imageId);
                        }
                        double curDuration = Double.parseDouble(attribs.get("duration"));
                        if (curDuration <= 0) {
                            throw new PUPrepareException("Invalid XML animation frame: duration must be above zero.");
                        }
                        
                        // Prepare and add image
                        totalDuration += curDuration;
                        display.preloadImage(imageId, pictureDir, attribs.get("imgsrc"));
                        images.add(new TimeStringTuple(totalDuration, imageId));
                        logger.debug("adding imgid: {}", imageId);
                    } else if (xmlHelper.getTagName().equals("sync")) {
                        syncs.add(new TimeStringTuple(totalDuration, attribs.get("type")));
                    } else {
                        logger.warn("Unexpected token found in xmlanimation: {}", xmlHelper.getTagName());
                    }
                    xmlHelper.skipTag();
                }
                
                xmlHelper.takeETag("xmlanim");
                if (images.isEmpty()) {
                    throw new PUPrepareException("Invalid XML animation: animation contains no frame elements.");
                }
            } else {
                throw new PUPrepareException("Invalid XML animation file format: Opening tag not found.");
            }
            xmlHelper.closeReader();
        } catch (IOException e) {
            throw new PUPrepareException("Error reading animation XML.");
        }

        // Add key positions
        keyPositions.add(new KeyPosition("start", 0d, 1d));
        keyPositions.add(new KeyPosition("end", 1d, 1d));
        for (TimeStringTuple s : syncs) {
            keyPositions.add(new KeyPosition(s.getId(), s.getTime() / totalDuration));
        }
    }

    public double getTotalDuration() {
        return totalDuration;
    }

    public List<KeyPosition> getKeyPositions() {
        return keyPositions;
    }

    /**
     * @return The imageId of the image with index n, or null if index is out of
     * bounds
     */
    public String getImageId(int n) {
        if (n > 0 || n < images.size()) {
            return images.get(n).getId();
        } else {
            return null;
        }
    }

    /**
     * @return The end time of the image with index n, or 0 is index is out of
     * bounds
     */
    public double getImageEndtime(int n) {
        if (n > 0 || n < images.size()) {
            return images.get(n).getTime();
        } else {
            return 0;
        }
    }
}
