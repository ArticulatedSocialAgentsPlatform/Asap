/*******************************************************************************
 *******************************************************************************/
package asap.math.splines;

import hmi.math.Vecf;
import hmi.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Data;

//Source material:
//Magic Software, Inc.
//http://www.magic-software.com
//Copyright (c) 2000, All Rights Reserved
//
//Source code from Magic Software is supplied under the terms of a license
//agreement and may not be copied or disclosed except in accordance with the
//terms of that agreement.  The various license agreements may be found at
//the Magic Software web site.  This file is subject to the license
//
//FREE SOURCE CODE
//http://www.magic-software.com/License.html/free.pdf

/**
 * TCBSpline
 * 
 * @author hvanwelbergen
 */
public class TCBSplineN
{
    private double m_fTMin, m_fTMax;
    private int m_iSegments;
    private List<Double> m_afTime = new ArrayList<>();;
    private List<Double> m_afLength = new ArrayList<>();
    private List<Double> m_afAccumLength = new ArrayList<>();
    private int dim;
    private List<float[]> m_akPoint = new ArrayList<>();

    private List<float[]> m_akA = new ArrayList<>();
    private List<float[]> m_akB = new ArrayList<>();
    private List<float[]> m_akC = new ArrayList<>();
    private List<float[]> m_akD = new ArrayList<>();

    private List<Double> m_afTension = new ArrayList<>();
    private List<Double> m_afContinuity = new ArrayList<>();
    private List<Double> m_afBias = new ArrayList<>();

    public TCBSplineN(int iSegments, List<Double> afTime, List<float[]> akPoint, List<Double> afTension, List<Double> afContinuity,
            List<Double> afBias)
    {

        m_fTMin = afTime.get(0);
        m_fTMax = afTime.get(iSegments);
        /*
        m_fTotalLength = -1.0;

        m_iIterations = 32;
        m_fTolerance = 1e-06;
        m_uiMaxLevel = Integer.MAX_VALUE;
        */
        m_iSegments = iSegments;
        m_afTime.addAll(afTime);

        CollectionUtils.ensureSize(m_afLength, m_iSegments);
        CollectionUtils.ensureSize(m_afAccumLength, m_iSegments);

        // TO DO. Add 'boundary type' just as in natural splines.
        assert (m_iSegments >= 3);

        // all four of these arrays have m_iSegments+1 elements
        dim = akPoint.get(0).length;
        m_akPoint = akPoint;
        m_afTension = afTension;
        m_afContinuity = afContinuity;
        m_afBias = afBias;

        allocArray(m_akA, m_iSegments, dim); // = new MgcVector3[m_iSegments];
        allocArray(m_akB, m_iSegments, dim); // = new MgcVector3[m_iSegments];
        allocArray(m_akC, m_iSegments, dim); // = new MgcVector3[m_iSegments];
        allocArray(m_akD, m_iSegments, dim); // = new MgcVector3[m_iSegments];

        // For now, treat the first point as if it occurred twice.
        ComputePolyKB(0, 0, 1, 2);

        for (int i = 1; i < m_iSegments - 1; i++)
            ComputePolyKB(i - 1, i, i + 1, i + 2);

        // For now, treat the last point as if it occurred twice.
        ComputePolyKB(m_iSegments - 2, m_iSegments - 1, m_iSegments, m_iSegments);

        // arc lengths of the polynomial segments
        int iKey;
        for (iKey = 0; iKey < m_iSegments; iKey++)
        {
            m_afLength.set(iKey, GetLengthInt(iKey, 0.0, m_afTime.get(iKey + 1) - m_afTime.get(iKey)));
        }

        // accumulative arc length
        m_afAccumLength.set(0, m_afLength.get(0));
        for (iKey = 1; iKey < m_iSegments; iKey++)
        {
            m_afAccumLength.set(iKey, m_afAccumLength.get(iKey - 1) + m_afLength.get(iKey));
        }
    }

    private void ComputePolyKB(int i0, int i1, int i2, int i3)
    {
        float[] kDiff = Vecf.getVecf(dim);
        Vecf.sub(kDiff, m_akPoint.get(i2), m_akPoint.get(i1));

        double fDt = m_afTime.get(i2) - m_afTime.get(i1);

        // build multipliers at P1
        double fOmt0 = 1.0 - m_afTension.get(i1);
        double fOmc0 = 1.0 - m_afContinuity.get(i1);
        double fOpc0 = 1.0 + m_afContinuity.get(i1);
        double fOmb0 = 1.0 - m_afBias.get(i1);
        double fOpb0 = 1.0 + m_afBias.get(i1);
        double fAdj0 = 2.0 * fDt / (m_afTime.get(i2) - m_afTime.get(i0));
        double fOut0 = 0.5 * fAdj0 * fOmt0 * fOpc0 * fOpb0;
        double fOut1 = 0.5 * fAdj0 * fOmt0 * fOmc0 * fOmb0;

        // build outgoing tangent at P1
        // MgcVectorN kTOut = fOut1*kDiff + fOut0*(m_akPoint[i1] - m_akPoint[i0]);
        float[] kTOut = Vecf.getVecf(dim);
        float[] tmp = Vecf.getVecf(dim);
        Vecf.sub(tmp, m_akPoint.get(i1), m_akPoint.get(i0));
        Vecf.scale((float) fOut0, tmp);
        Vecf.scale((float) fOut1, kTOut, kDiff);
        Vecf.add(kTOut, tmp);

        // build multipliers at point P2
        double fOmt1 = 1.0 - m_afTension.get(i2);
        double fOmc1 = 1.0 - m_afContinuity.get(i2);
        double fOpc1 = 1.0 + m_afContinuity.get(i2);
        double fOmb1 = 1.0 - m_afBias.get(i2);
        double fOpb1 = 1.0 + m_afBias.get(i2);
        double fAdj1 = 2.0 * fDt / (m_afTime.get(i3) - m_afTime.get(i1));
        double fIn0 = 0.5 * fAdj1 * fOmt1 * fOmc1 * fOpb1;
        double fIn1 = 0.5 * fAdj1 * fOmt1 * fOpc1 * fOmb1;

        // build incoming tangent at P2
        // MgcVectorN kTIn = fIn1*(m_akPoint[i3] - m_akPoint[i2]) + fIn0*kDiff;
        float kTIn[] = Vecf.getVecf(dim);
        tmp = Vecf.getVecf(dim);
        Vecf.sub(tmp, m_akPoint.get(i3), m_akPoint.get(i2));
        Vecf.scale((float) fIn1, tmp);
        Vecf.scale((float) fIn0, kTIn, kDiff);
        Vecf.add(kTIn,tmp);

        m_akA.set(i1, m_akPoint.get(i1));
        m_akB.set(i1, kTOut);
        // m_akC.set(i1, 3.0*kDiff - 2.0*kTOut - kTIn);
        float res[] = Vecf.getVecf(dim);
        tmp = Vecf.getVecf(dim);
        Vecf.scale(-2, tmp,kTOut);
        Vecf.sub(tmp, kTIn);
        Vecf.scale(3, res, kDiff);
        Vecf.add(res, tmp);
        m_akC.set(i1, res);

        // m_akD.set(i1, -2.0 * kDiff + kTOut + kTIn);
        res = Vecf.getVecf(dim);
        Vecf.scale(-2, res, kDiff);
        Vecf.add(res, kTOut);
        Vecf.add(res, kTIn);
        m_akD.set(i1, res);
    }

    

    public double GetSpeedInt(int iKey, double fTime)
    {
        float kVelocity[] = Vecf.getVecf(dim);
        // kVelocity = m_akB[iKey] + fTime*(2.0*m_akC[iKey] + 3.0*fTime*m_akD[iKey]);
        float tmp[] = Vecf.getVecf(dim);
        Vecf.scale((float) fTime * 3, tmp, m_akD.get(iKey));
        Vecf.scale(2, kVelocity, m_akC.get(iKey));
        Vecf.add(tmp, kVelocity);
        Vecf.scale((float) fTime, tmp);
        Vecf.add(kVelocity, m_akB.get(iKey), tmp);
        return Vecf.length(kVelocity);
    }

    public double GetLengthInt(int iKey, double fT0, double fT1)
    {
        // TO DO. implement (also not implemented in ACE)
        return 0.0;
    }

    public float[] GetPosition(double fTime)
    {
        KeyInfo kInfo = GetKeyInfo(fTime);
        int iKey = kInfo.getRiKey();
        double fDt = kInfo.getRfDt();

        fDt /= (m_afTime.get(iKey + 1) - m_afTime.get(iKey));

        // kResult = m_akA[iKey] + fDt * (m_akB[iKey] + fDt * (m_akC[iKey] + fDt * m_akD[iKey]));
        float[] kResult = Vecf.getVecf(dim);
        Vecf.scale((float) fDt, kResult, m_akD.get(iKey));
        Vecf.add(kResult, m_akC.get(iKey));
        Vecf.scale((float) fDt, kResult);
        Vecf.add(kResult, m_akB.get(iKey));
        Vecf.scale((float) fDt, kResult);
        Vecf.add(kResult, m_akA.get(iKey));
        // cout << "getPosition at " << fTime << "(" << fDt << ")"
        // << ": Pn=" << m_akA[iKey]
        // << ", Pn+1=" << m_akB[iKey]
        // << ", Tn=" << m_akC[iKey]
        // << ", Tn+1=" << m_akD[iKey] << " -> " << kResult << endl;

        return kResult;
    }

    public float[] GetFirstDerivative(double fTime)
    {
        KeyInfo kInfo = GetKeyInfo(fTime);
        int iKey = kInfo.getRiKey();
        double fDt = kInfo.getRfDt();

        fDt /= (m_afTime.get(iKey + 1) - m_afTime.get(iKey));
        // kResult = m_akB[iKey] + fDt*(2.0*m_akC[iKey] + 3.0*fDt*m_akD[iKey]);
        float kResult[] = Vecf.getVecf(dim);
        Vecf.scale(3 * (float) fDt, kResult, m_akD.get(iKey));
        Vecf.add(kResult, m_akC.get(iKey));
        Vecf.add(kResult, m_akC.get(iKey));
        Vecf.scale((float) fDt, kResult);
        Vecf.add(kResult, m_akB.get(iKey));
        return kResult;
    }

    public float[] GetSecondDerivative(double fTime)
    {
        KeyInfo kInfo = GetKeyInfo(fTime);
        int iKey = kInfo.getRiKey();
        double fDt = kInfo.getRfDt();

        fDt /= (m_afTime.get(iKey + 1) - m_afTime.get(iKey));

        // MgcVectorN kResult = 2.0*m_akC[iKey] + 6.0*fDt*m_akD[iKey];
        float kResult[] = Vecf.getVecf(dim);
        Vecf.scale(6 * (float) fDt, kResult, m_akD.get(iKey));
        Vecf.add(kResult, m_akC.get(iKey));
        Vecf.add(kResult, m_akC.get(iKey));

        return kResult;
    }

    public float[] GetThirdDerivative(double fTime)
    {
        KeyInfo kInfo = GetKeyInfo(fTime);
        int iKey = kInfo.getRiKey();
        float[] kResult = Vecf.getVecf(dim);
        Vecf.scale(6, kResult, m_akD.get(iKey));
        return kResult;
    }

    double GetVariation(double fT0, float[] rkP0, double fT1, float rkP1)
    {
        // TO DO. implement (also not implemented in ACE)
        return 0.0;
    }

    public String getSplineString()
    {
        StringBuffer buf = new StringBuffer();
        for (double u = GetMinTime(); u <= GetMaxTime(); u += 0.1)
        {
            buf.append(Arrays.toString(GetPosition(u)));
        }
        return buf.toString();
    }    

    private void allocArray(List<float[]> array, int size, int dim)
    {
        array.clear();
        for (int i = 0; i < size; i++)
        {
            array.add(Vecf.getVecf(dim));
        }
    }

    @Data
    private static class KeyInfo
    {
        private final int riKey;
        private final double rfDt;
    }

    // from MGcMultipleCurve.inl
    KeyInfo GetKeyInfo(double fTime)
    {
        int riKey = 0;
        double rfDt = 0;
        // cout << "GetKeyInfo for " << fTime << endl;
        if (fTime <= m_afTime.get(0))
        {
            riKey = 0;
            rfDt = 0.0;
        }
        else if (fTime >= m_afTime.get(m_iSegments))
        {
            riKey = m_iSegments - 1;
            rfDt = m_afTime.get(m_iSegments) - m_afTime.get(m_iSegments - 1);
        }
        else
        {
            for (int i = 0; i < m_iSegments; i++)
            {
                if (fTime < m_afTime.get(i + 1))
                {
                    riKey = i;
                    rfDt = fTime - m_afTime.get(i);
                    break;
                }
            }
        }
        return new KeyInfo(riKey, rfDt);
    }

    public double GetMinTime()
    {
        return m_fTMin;
    }

    public double GetMaxTime()
    {
        return m_fTMax;
    }
}
