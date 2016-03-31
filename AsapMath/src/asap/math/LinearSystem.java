/*******************************************************************************
 *******************************************************************************/
package asap.math;

import hmi.math.Vec3f;
import hmi.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

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
 * Utilities for LinearSystem solving
 * 
 * @author hvanwelbergen
 */
public final class LinearSystem
{
    private LinearSystem()
    {
    }

    /**
     * Input:
     * A[iSize][iSize] coefficient matrix, entries are A[row][col]
     * B[iSize] vector, entries are B[row]
     * Output:
     * return value is TRUE if successful, FALSE if pivoting failed
     * A[iSize][iSize] is inverse matrix
     * B[iSize] is solution x to Ax = B
     */
    public static boolean solve(List<List<Float>> aafA, List<float[]> afB)
    {
        int iSize = aafA.size();
        List<Integer> aiColIndex = new ArrayList<>();
        CollectionUtils.ensureSize(aiColIndex, iSize);
        List<Integer> aiRowIndex = new ArrayList<>();
        CollectionUtils.ensureSize(aiRowIndex, iSize);
        List<Boolean> abPivoted = new ArrayList<>();
        CollectionUtils.ensureSize(abPivoted, iSize);
        
        for (int i = 0; i < iSize; i++)
            abPivoted.set(i, false);

        int i1, i2, iRow = 0, iCol = 0;
        float vSave[] = Vec3f.getVec3f(0,0,0);
        double fSave;        
        float tmp[] = Vec3f.getVec3f();

        // elimination by full pivoting
        for (int i0 = 0; i0 < iSize; i0++)
        {

            // search matrix (excluding pivoted rows) for maximum absolute entry
            double fMax = 0.0;
            for (i1 = 0; i1 < iSize; i1++)
            {
                if (!abPivoted.get(i1))
                {
                    for (i2 = 0; i2 < iSize; i2++)
                    {
                        if (!abPivoted.get(i2))
                        {
                            double fAbs = Math.abs(aafA.get(i1).get(i2));
                            if (fAbs > fMax)
                            {
                                fMax = fAbs;
                                iRow = i1;
                                iCol = i2;
                            }
                        }
                    }
                }
            }

            if (fMax == 0.0)
            {
                return false;
                // matrix is not invertible
            }
            

            abPivoted.set(iCol, true);

            // swap rows so that A[iCol][iCol] contains the pivot entry
            if (iRow != iCol)
            {
                List<Float> afRowPtr = aafA.get(iRow);
                aafA.set(iRow, aafA.get(iCol));
                aafA.set(iCol, afRowPtr);

                vSave = afB.get(iRow);
                afB.set(iRow, afB.get(iCol));
                afB.set(iCol, vSave);
            }

            // keep track of the permutations of the rows
            aiRowIndex.set(i0, iRow);
            aiColIndex.set(i0, iCol);

            // scale the row so that the pivot entry is 1
            double fInv = 1.0 / aafA.get(iCol).get(iCol);
            aafA.get(iCol).set(iCol, 1.0f);
            for (i2 = 0; i2 < iSize; i2++)
            {
                //aafA[iCol][i2] *= fInv;
                float tmpF = aafA.get(iCol).get(i2);
                aafA.get(iCol).set(i2, (float) fInv * tmpF);
            }

            // afB[iCol] *= fInv;
            tmp = Vec3f.getVec3f(afB.get(iCol));            
            Vec3f.scale((float) fInv, tmp);
            afB.set(iCol, tmp);

            // zero out the pivot column locations in the other rows
            for (i1 = 0; i1 < iSize; i1++)
            {
                if (i1 != iCol)
                {
                    fSave = aafA.get(i1).get(iCol);
                    aafA.get(i1).set(iCol, 0.0f);
                    for (i2 = 0; i2 < iSize; i2++)
                    {
                        // aafA[i1][i2] -= aafA[iCol][i2] * fSave;
                        float tmpF =aafA.get(i1).get(i2)-(float) (aafA.get(iCol).get(i2)*fSave);
                        aafA.get(i1).set(i2,tmpF);
                    }

                    // afB[i1] -= afB[iCol] * fSave;
                    tmp = Vec3f.getVec3f(afB.get(iCol));
                    Vec3f.scale((float) fSave, tmp);
                    Vec3f.sub(tmp, afB.get(i1), tmp);
                    afB.set(i1, tmp);
                }
            }
        }

        // reorder rows so that A[][] stores the inverse of the original matrix
        for (i1 = iSize - 1; i1 >= 0; i1--)
        {
            if (aiRowIndex.get(i1) != aiColIndex.get(i1))
            {
                for (i2 = 0; i2 < iSize; i2++)
                {
                    fSave = aafA.get(i2).get(aiRowIndex.get(i1));
                    aafA.get(i2).set(aiRowIndex.get(i1), aafA.get(i2).get(aiColIndex.get(i1)));
                    aafA.get(i2).set(aiColIndex.get(i1), (float) fSave);
                }
            }
        }

        return true;
    }

    /**
     * Input:
     * Matrix is tridiagonal.
     * Lower diagonal A[iSize-1]
     * Main diagonal B[iSize]
     * Upper diagonal C[iSize-1]
     * Right-hand side R[iSize]
     * Output:
     * return value is TRUE if successful, FALSE if pivoting failed
     * U[iSize] is solution
     */
    public static boolean solveTri2(List<Float> afA, List<Float> afB, List<Float> afC, List<float[]> afR, List<float[]> afU)
    {
        float tmp[] = Vec3f.getVec3f();
        if (afB.get(0) == 0.0) return false;
        int size = afR.size();
        List<Float> afD = new ArrayList<>();
        CollectionUtils.ensureSize(afD, size);

        // cout << "==============================================" << endl;
        // cout << "input:" << endl;
        // cout << afB[0] << "*p_0 + " << afC[0] << "*p_1 = " << afR[0] << endl;
        // for (int i=1; i<=size-2; i++)
        // cout << afA[i-1] << "*p_" << i-1 << " + "
        // << afB[i] << "*p_" << i << " + "
        // << afC[i] << "*p_" << i+1 << " = " << afR[i] << endl;
        // cout << afA[size-2] << "*p_" << size-2 << "+ "
        // << afB[size-1] << "*p_" << size-1 << " = " << afR[size-1] << endl;

        double fE = afB.get(0);
        double fInvE = 1.0 / fE;
        tmp = Vec3f.getVec3f(afR.get(0));
        Vec3f.scale((float) fInvE, tmp);
        afU.set(0, tmp);

        int i0, i1;
        for (i0 = 0, i1 = 1; i1 < size; i0++, i1++)
        {

            afD.set(i0, (float) (afC.get(i0) * fInvE));

            fE = afB.get(i1) - afA.get(i0) * afD.get(i0);
            if (fE == 0.0) return false;
            fInvE = 1.0 / fE;
            tmp = Vec3f.getVec3f(afU.get(i0));
            Vec3f.scale((float) (fInvE * afA.get(i0)), tmp);
            Vec3f.sub(tmp, afR.get(i1), tmp);

            // afU.set(i1,(afR.get(i1) - afA.get(i0)*afU.get(i0))*fInvE);
            afU.set(i1, tmp);
        }

        for (i0 = size - 1, i1 = size - 2; i1 >= 0; i0--, i1--)
        {
            // afU.get[i1] -= afD[i1] * afU[i0];
            tmp = Vec3f.getVec3f(afU.get(i0));
            Vec3f.scale(afD.get(i1), tmp);
            Vec3f.sub(tmp, afU.get(i1), tmp);
        }

        // cout << "PROBE:" << endl;
        // MgcVector3 v;
        // v = afB[0]*afU[0]+afC[0]*afU[1];
        // cout << v << "=?" << afR[0] << endl;
        // for (int i=1; i<=size-2; i++) {
        // v=afA[i-1]*afU[i-1] +
        // afB[i]*afU[i] +
        // afC[i]*afU[i+1];
        // cout << v << "=?" << afR[i] << endl;
        // }
        // v = afA[size-2]*afU[size-2]+afB[size-1]*afU[size-1];
        // cout << v << "=?" << afR[size-1] << endl;
        // cout << "==============================================" << endl;

        return true;
    }
}
