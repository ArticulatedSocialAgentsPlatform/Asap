/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.gesturebinding;

import hmi.animation.SkeletonPose;
import hmi.util.Resources;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.google.common.collect.ImmutableMap;

/**
 * Utility class for HNS handshapes
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class HnsHandshape
{
    private ImmutableMap<String, SkeletonPose> poseMap;

    public HnsHandshape()
    {
        
    }
            
    public HnsHandshape(String... handshapePaths) throws IOException
    {
        List<SkeletonPose> poses = new ArrayList<>();
        for (String path : handshapePaths)
        {
            Reflections reflections = new Reflections(new ConfigurationBuilder().addUrls(
                    ClasspathHelper.forPackage(path.replaceAll("/", "."))).setScanners(new ResourcesScanner()));

            Set<String> xmlFiles = reflections.getResources(Pattern.compile(".*\\.xml"));
            for (String file : xmlFiles)
            {
                if (file.startsWith(path))
                {
                    SkeletonPose p = new SkeletonPose(new XMLTokenizer(new Resources("").getReader(file)));
                    if (p.getId() != null)
                    {
                        poses.add(p);
                    }
                    else
                    {
                        log.warn("pose without id " + file);
                    }
                }
            }

        }
        setHandshapes(poses);
    }

    public HnsHandshape(Collection<SkeletonPose> shapes)
    {
        setHandshapes(shapes);
    }

    private void setHandshapes(Collection<SkeletonPose> shapes)
    {
        Map<String, SkeletonPose> pMap = new HashMap<>();
        for (SkeletonPose p : shapes)
        {
            pMap.put(p.getId(), p);
        }
        poseMap = ImmutableMap.copyOf(pMap);
    }

    public SkeletonPose getHNSHandShape(String handshape)
    {
        String basicSymbol, poseString;
        String HNSStr[] = handshape.split("\\s+");
        basicSymbol = HNSStr[0];

        // parse basic hand shape and retrieve posture
        // if (hns.getBasicHandShapes().contains(basicSymbol) || hns.getSpecificHandShapes().contains(basicSymbol))
        if (poseMap.containsKey(basicSymbol))
        {
            SkeletonPose p = poseMap.get(basicSymbol);
            return p.untargettedDeepCopy();

            // TODO: parse and handle stuff between ()'s

            // if (readPostureFile(basicSymbol, poseStr)) {
            // p = figure->string_to_posture(poseStr);
            // while (HNSStr.grep('(',')',basicSymbol)) {
            // // additional posture modifications
            // if (!modifyPosture(basicSymbol, p))
            // return false;
            // }
            // return true;
            // }
            // else
            // cerr << "couldn't process basic hand shape symbol: " << basicSymbol << endl;
            // }
        }
        else
        {
            log.warn("no correct HNS hand shape description: " + basicSymbol);
            return null;
        }
    }
}
