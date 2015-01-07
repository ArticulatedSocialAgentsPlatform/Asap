/*******************************************************************************
 *******************************************************************************/
package asap.picture.planunit;

import hmi.util.Resources;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import asap.picture.display.PictureDisplay;

public class AnimationDirLoader {

    private String animationDir = "";
    private String resourceDir = "";
    private ArrayList<String> imageIds;
    private PictureDisplay display = null;

    public AnimationDirLoader(String resourceDir, String animationDir, PictureDisplay display) {
        if (!resourceDir.endsWith("/")) {
            resourceDir += "/";
        }
        this.resourceDir = resourceDir;
        if (!animationDir.endsWith("/")) {
            animationDir += "/";
        }
        this.animationDir = animationDir;
        this.display = display;
        loadImages();
    }

    private void loadImages() {
        imageIds = new ArrayList<String>();

        File dir;
        try {
            dir = getDirHandle();
            File[] files = dir.listFiles();
            for (File file : files) {
                String imageId = resourceDir + animationDir + file.getName();
                System.out.println("id:" + imageId + " path:" + resourceDir + animationDir + " name" + file.getName());
                display.preloadImage(imageId, resourceDir + animationDir, file.getName());
                imageIds.add(imageId);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private File getDirHandle() throws URISyntaxException {
        Resources r = new Resources(resourceDir);
        URL u = r.getURL(animationDir);
        URI uri = u.toURI();
        File f = new File(uri);
        return f;
    }

    public int getNumberOfImages() {
        return imageIds.size();
    }

    public String getImageId(int n) {
        return imageIds.get(n);
    }
}
