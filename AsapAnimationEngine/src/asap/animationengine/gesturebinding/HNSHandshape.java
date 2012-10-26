package asap.animationengine.gesturebinding;

import lombok.extern.slf4j.Slf4j;
import asap.hns.Hns;
import hmi.animation.SkeletonPose;

/**
 * Utility class for HNS handshapes
 * @author hvanwelbergen
 * 
 */
@Slf4j
public class HNSHandshape
{
    private final Hns hns;

    public HNSHandshape(Hns hns)
    {
        this.hns = hns;
    }

    public SkeletonPose getHNSHandShape(String handshape)
    {
        String basicSymbol, poseString;
        String HNSStr[] = handshape.split("\\s+");
        basicSymbol = HNSStr[0];

        // parse basic hand shape and retrieve posture
        if (hns.getBasicHandShapes().contains(basicSymbol) || hns.getSpecificHandShapes().contains(basicSymbol))
        {
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
        return null;
    }
}
