/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace;

import hmi.math.Mat4f;
import hmi.math.Vec3f;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Sequence of GuidingStrokes
 * @author hvanwelbergen
 * @author Stefan Kopp (original C++ version)
 */
@Slf4j
public class GuidingSequence
{
    private List<GuidingStroke> strokes = new ArrayList<>();
    private float sPos[] = Vec3f.getVec3f();

//    @Setter
//    private TPConstraint sT = new TPConstraint();
    
    @Getter @Setter
    private double startTime;
    
    public void clear()
    {
        strokes.clear();
        Vec3f.setZero(sPos);
        startTime = 0;
    }
    
    public int size()
    {
        return strokes.size();
    }

    public void setStartPos(float[] p)
    {
        Vec3f.set(sPos, p);
    }    
    
    /**
     * Copies startPos in p
     * @param p
     */
    public void getStartPos(float[]p)
    {
        Vec3f.set(p,sPos);
    }

    public float[] getStartPos()
    {
        return Vec3f.getVec3f(sPos);
    }
    
    public boolean isEmpty()
    {
        return strokes.isEmpty();
    }
    
    public void addGuidingStroke(GuidingStroke gs)
    {
        strokes.add(gs);
    }

    public void transform(float m[])
    {
        Mat4f.transformPoint(m, sPos);
        for (GuidingStroke gs : strokes)
        {
            gs.transform(m);
        }
    }

    public float[] getEndPos()
    {
        float[]result = Vec3f.getVec3f();
        getEndPos(result);
        return result;
    }
    
    public void getEndPos(float[] result)
    {
        if (strokes.isEmpty())
        {
            Vec3f.setZero(result);
            log.warn("GuidingSequence::getEndPos : no strokes asserted!");
            return;
        }
        strokes.get(strokes.size() - 1).getEndPos(result);
    }

    public void getStartPosOfStroke(float result[], int i)
    {
        if (i >= strokes.size())
        {
            log.warn("GuidingSequence::getStartPosOfStroke : no. of strokes exceeded!");
            Vec3f.setZero(result);
            return;
        }

        if (i == 0)
        {
            Vec3f.set(result, sPos);
        }
        else
        {
            strokes.get(i - 1).getEndPos(result);
        }
    }
    
    public float[] getStartPosOfStroke(int i)
    {
        float[]result= Vec3f.getVec3f();
        getStartPosOfStroke(result,i);
        return result;
    }



    public GuidingStroke getStroke(int i)
    {
        if (i >= strokes.size())
        {
            log.warn("GuidingSequence::operator[] : stroke no. exceeded!");
            return null;
        }
        return strokes.get(i);
    }


    public void replaceStroke(int i, List<GuidingStroke> gsSubs)
    {
        if (i >= 0 && i < strokes.size() && !gsSubs.isEmpty())
        {
            strokes.remove(i);
            strokes.addAll(i, gsSubs);
        }
    }

    public void getStartDirOfStroke(float[] result, int i)
    {
        if (i < 0 || i >= strokes.size() - 1)
        {
            Vec3f.setZero(result);
            return;
        }
        GuidingStroke gs = getStroke(i);

        float ep[] = Vec3f.getVec3f();
        float sp[] = Vec3f.getVec3f();
        getStartPosOfStroke(sp, i);

        if (gs instanceof LinearGStroke)
        {
            gs.getEndPos(ep);

        }
        else if (gs instanceof CurvedGStroke)
        {
            CurvedGStroke cgs = (CurvedGStroke) gs;
            cgs.getN1(ep);
        }
        Vec3f.sub(result, ep, sp);
    }

    public float[] getStrokeEndVelocityOf(int i)
    {
        float[] result = Vec3f.getVec3f();
        getStrokeEndVelocityOf(result, i);
        return result;
    }
    
    public void getStrokeEndVelocityOf(float[] result, int i)
    {
        if (i < 0 || i >= strokes.size() - 1)
        {
            Vec3f.setZero(result);
            return;
        }

        // --- fetch both guiding strokes
        GuidingStroke gs1 = getStroke(i);
        GuidingStroke gs2 = getStroke(i + 1);
        
        //MgcReal tDur1 = (gs1->eT.time - getStartTimeOfStroke(i));
        //MgcReal tDur2 = (gs2->eT.time - gs1->eT.time);        
        double tDur1 = gs1.getEDt();
        double tDur2 = gs2.getEDt();
        
        
        float s1[] = Vec3f.getVec3f();
        float s2[] = Vec3f.getVec3f();
        float s3[] = Vec3f.getVec3f();
        getStartPosOfStroke(s1, i);
        gs1.getEndPos(s2);
        gs2.getEndPos(s3);

        // --- determine overall direction between the strokes -> outDir = v
        double sD1 = gs1.getArcLengthFrom(s1);
        double sD2 = gs2.getArcLengthFrom(s2);
        float outDir[] = Vec3f.getVec3f();
        float inDir[] = Vec3f.getVec3f();
        float vDir[] = Vec3f.getVec3f();

        boolean curved = false;
        if (gs1 instanceof LinearGStroke)
        {
            if (gs2 instanceof LinearGStroke)
            {
                // LINEAR -> LINEAR
                Vec3f.sub(inDir, s2, s1);
                Vec3f.sub(outDir, s3, s2);
                Vec3f.sub(vDir, s3, s1);
            }
            else if (gs2 instanceof CurvedGStroke)
            {
                // LINEAR -> CURVE
                curved = true;
                CurvedGStroke cg2 = (CurvedGStroke) gs2;
                Vec3f.sub(inDir, s2, s1);
                float cg2N1[] = Vec3f.getVec3f();
                cg2.getN1(cg2N1);
                Vec3f.sub(outDir, cg2N1);
                Vec3f.sub(vDir, cg2N1, s1);
            }
        }
        else if (gs1 instanceof CurvedGStroke)
        {
            curved = true;
            CurvedGStroke cg1 = (CurvedGStroke) gs1;
            if (gs2 instanceof LinearGStroke)
            {
                // CURVE -> LINEAR
                float cg1N2[] = Vec3f.getVec3f();
                cg1.getN2(cg1N2);
                Vec3f.sub(inDir, s2, cg1N2);
                Vec3f.sub(outDir, s3, s2);
                Vec3f.sub(vDir, s3, cg1N2);
            }
            else if (gs2 instanceof CurvedGStroke)
            {
                // CURVE -> CURVE
                CurvedGStroke cg2 = (CurvedGStroke) gs2;
                float cg1N2[] = Vec3f.getVec3f();
                cg1.getN2(cg1N2);
                float cg2N1[] = Vec3f.getVec3f();
                cg2.getN1(cg2N1);
                Vec3f.sub(inDir, s2, cg1N2);
                Vec3f.sub(outDir, cg2N1, s2);
                Vec3f.sub(vDir, cg2N1, cg1N2);
            }
        }

        // -- force predefined direction with higher priority, i.e., overwrite vDir
        if (gs1.getPhaseId() == GStrokePhaseID.STP_PREP && gs2.getPhaseId() == GStrokePhaseID.STP_STROKE)
        // -> preparation finishes with stroke start direction, i.e., outDir
        {
            vDir = outDir;
        }
        else if (gs1.getPhaseId() == GStrokePhaseID.STP_STROKE && gs2.getPhaseId() == GStrokePhaseID.STP_RETRACT)
        // -> stroke finishes with end direction, i.e., inDir
        {
            vDir = inDir;
        }
        
        if(Vec3f.lengthSq(vDir)<=0.0001)
        {
            Vec3f.set(result, vDir);
            return;
        }
        
        // --- approx. speed (absolute velocity value)
        // -- determine radius or angle, resp., between these guiding strokes
        Vec3f.normalize(inDir);
        Vec3f.normalize(outDir);
        Vec3f.normalize(vDir);

        // -- normalize to overall velocity based on distance-time ratio
        double v = 0.5 * (sD1 / tDur1 + sD2 / tDur2);
        // -- determine velocity gain factor at this segmentation point
        double k = (gs1.getVGain() + gs2.getVGain()) / 2.0; // default = 1.0
        if (curved) k = 0.8; // at least one stroke is a curved one

        // -- estimate break point velocity from cuvature (radius)
        float[] axis = Vec3f.getVec3f();
        Vec3f.cross(axis, outDir, inDir);
        double vel;
        if (Vec3f.length(axis) > 1E-10)
        {
            double alpha = Math.abs(Vec3f.angleBetweenVectors(outDir,inDir));
            double r = Math.abs(1-(alpha/Math.PI));
            
            // -- apply law of 2/3
            vel = k * v * Math.pow(r,0.333);            
        }
        else
        {
            vel = 0;

        }

        Vec3f.scale((float) vel, vDir);
        Vec3f.set(result, vDir);
    }
}
