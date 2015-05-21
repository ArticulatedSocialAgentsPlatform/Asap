/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.ace;

import hmi.math.Mat4f;
import hmi.math.Vec3f;
import lombok.Getter;
import asap.hns.ShapeSymbols;

/**
 * A curved stroke is denoted by three vector parameters, denoting consecutive control points. However, one can
 * look at them as defining the direction and length, as well as the roundness and skewedness of the stroke.
 * @author hvanwelbergen
 * @author Stefan Kopp (original C++ version)
 */
public class CurvedGStroke extends GuidingStroke
{
    private float n[] = Vec3f.getVec3f(); // normal
    private ShapeSymbols a; // Form/Wölbungsart (-2:rechtes S, -1:rechtes C, +1:linkes C, +2:linkes S)
    private double w; // Wölbungsgrad (0...1)
    private double r; // Rundung (-1:zweieckig, 0: rund, +1:eineckig/spitz)
    private double s; // Schiefe (-1:hinten flach ... 0:rund ... +1:vorne flach

    private float n1[] = Vec3f.getVec3f(), n2[] = Vec3f.getVec3f(); // interior data points

    // interior break points
    @Getter private double fT1;
    @Getter private double fT2;

    public CurvedGStroke(GStrokePhaseID phase, float[] p, float n[], ShapeSymbols a, double w, double r, double s)
    {
        this(phase, p, n, a, w, r, s, Vec3f.getZero(), Vec3f.getZero(), 0, 0);
    }

    public CurvedGStroke(GStrokePhaseID phase, float[] p, float n[], ShapeSymbols a, double w, double r, double s,
            float[] n1, float[] n2, double fT1, double fT2)
    {
        super(phase, p, Vec3f.getZero());
        Vec3f.normalize(this.n, n);
        this.a = a;
        this.w = w;
        this.r = r;
        this.s = s;

        this.fT1 = fT1;
        this.fT2 = fT2;
        Vec3f.set(this.n1, n1);
        Vec3f.set(this.n2, n2);
    }

    /**
     * Copy n1 to result
     */
    public void getN1(float result[])
    {
        Vec3f.set(result, n1);
    }

    /**
     * Create a new float[] containing the values of n1
     */
    public float[] getN1()
    {
        return Vec3f.getVec3f(n1);
    }

    /**
     * Copy n2 to result
     */
    public void getN2(float result[])
    {
        Vec3f.set(result, n2);
    }

    /**
     * Create a new float[] containing the values of n2
     */
    public float[] getN2()
    {
        return Vec3f.getVec3f(n2);
    }

    @Override
    public void transform(float m[])
    {
        super.transform(m);
        Mat4f.transformPoint(m, n1);
        Mat4f.transformPoint(m, n2);
        Mat4f.transformVector(m, n);
    }

    @Override
    public double getArcLengthFrom(float[] sPos)
    {
        float[] v1 = Vec3f.getVec3f();
        float[] v2 = Vec3f.getVec3f();
        float[] v3 = Vec3f.getVec3f();
        Vec3f.sub(v1, n1, sPos);
        Vec3f.sub(v2, n2, n1);
        Vec3f.sub(v3, endPos, n2);
        return Vec3f.length(v1) + Vec3f.length(v2) + Vec3f.length(v3);
    }

    public void formAt(float sP[], double sT)
    {
        float p[] = Vec3f.getVec3f();
        Vec3f.sub(p, endPos, sP);
        double fA = Vec3f.length(p);

        double fN1 = 0;
        double fN2 = 0;

        switch (a)
        {
        case RightS:
            fN1 = 1;
            fN2 = -1;
            break;
        case RightC:
            fN1 = 1;
            fN2 = 1;
            break;
        case LeftC:
            fN1 = -1;
            fN2 = -1;
            break;
        case LeftS:
            fN1 = -1;
            fN2 = 1;
            break;
        }

        // -- W�lbungsrad w (0...1) => fB1,fB2
        double fB1 = w * fA;
        double fB2 = fB1;

        // -- Rundung r => fA1,fA2,fA3
        double fA1, fA2, fA3;
        if (r >= 0)
        {
            // 0..1: rund -> eineckig/spitz
            if (r >= 1) r = .995;
            fA1 = fA / (3 - r);
        }
        else
        {
            if (r < -1) r = -.995;
            r *= 27;
            fA1 = fA / (3 - r);
        }
        fA3 = fA1;
        fA2 = fA - fA1 - fA3;

        /* unused??
        double fS1 = Math.sqrt(fA1 * fA1 + fB1 * fB1);
        double fS2 = fA2;
        double fS3 = Math.sqrt(fA3 * fA3 + fB2 * fB2);
        double fS = fS1 + fS2 + fS3;
        */
        
        // -- Schiefe s
        if (s > 0)
        {
            double fB1Min = fA1 * fB2 / (fA2 + fA1);
            fB1 = fB1Min + (1 - s) * (fB2 - fB1Min);
        }
        else if (s < 0)
        {
            double fB2Min = fA3 * fB1 / (fA2 + fA3);
            fB2 = fB2Min + (1 + s) * (fB1 - fB2Min);
        }

        // -- create interior data points => vN1, vN2
        float d[] = Vec3f.getVec3f();
        Vec3f.normalize(p);
        Vec3f.cross(d, p, n);

        // n1 = sP + (fA1 * p + fN1 * fB1 * d);
        float tempP[] = Vec3f.getVec3f();
        float tempD[] = Vec3f.getVec3f();
        Vec3f.scale((float) fA1, tempP, p);
        Vec3f.scale((float) (fN1 * fB1), tempD, d);
        Vec3f.set(n1, sP);
        Vec3f.add(n1, tempP);
        Vec3f.add(n1, tempD);

        // n2 = sP + ((fA1 + fA2) * p + fN2 * fB2 * d);
        Vec3f.scale((float) (fA1 + fA2), tempP, p);
        Vec3f.scale((float) (fN2 * fB2), tempD, d);
        Vec3f.set(n2, sP);
        Vec3f.add(n2, tempP);
        Vec3f.add(n2, tempD);

        // -- set chordal parametrization => fT1,fT2
        double fDur = this.getEDt();
        fT1 = sT + 2 * fDur / 5; // (fDur/fS)*fS1;
        fT2 = sT + 3 * fDur / 5; // (fDur/fS)*(fS2+fS1);
    }
}
