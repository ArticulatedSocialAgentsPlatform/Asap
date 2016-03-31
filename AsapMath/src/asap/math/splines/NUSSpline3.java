/*******************************************************************************
 *******************************************************************************/
package asap.math.splines;

import hmi.math.Vec3f;
import hmi.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import asap.math.LinearSystem;
import asap.math.Matrix;

/**
 * S-Spline with B-Spline blending functions
 * @author hvanwelbergen
 * @author Stefan Kopp (original C++ version)
 */
@Slf4j
public class NUSSpline3 extends NUBSpline3
{
    public NUSSpline3(int order)
    {
        super(order);
    }

    public float[] getPosition(double fTime)
    {
        float f[] = Vec3f.getVec3f(c.get(0));
        float tmp[] = Vec3f.getVec3f();
        // if (isnan(f[0]) || isnan(f[1]) || isnan(f[2]))
        // {
        // std::cerr << "MgcNUSSpline3::GetPosition: (ERROR) f=c.front() has elements, which are not numbers." << std::endl;
        // }

        float S[] = new float[n + 1];
        calcBlendingFunctionsAt(fTime, S);
        for (int i = 1; i < n; i++)
        {
            // f += S[i] * (c[i] - c[i - 1]);
            tmp = Vec3f.getVec3f(c.get(i));
            Vec3f.sub(tmp, c.get(i - 1));
            Vec3f.scale(S[i], tmp);
            Vec3f.add(f, tmp);
        }
        return f;
    }

    public void calcBlendingFunctionsAt(double u, float[] S)
    {
        calcBlendingFunctionsAt(u, S, -1);
    }

    /**
     * improved column-wise determination
     * note: we generate all blending functions at u at once, that
     * is S(1,k)...S(n,k).
     */
    public void calcBlendingFunctionsAt(double u, float[] S, int o)
    {
        // calculate b-spline blending functions
        Matrix N = new Matrix(n + k, k + 1);
        calcBlendingFunctionsAt(u, N, o);

        float si;
        for (int i = 1; i <= n; i++)
        {
            if (u >= t.get(i))
            {
                si = 0f;
                for (int j = i; j <= n; j++)
                {
                    si += N.get(j, k);
                }
                S[i] = si;
            }
            else S[i] = 0f;
        }
    }

    

    /**
     * data points x[0]...x[l]
     * times t[0]...t[l]
     * sparse (!) velocities at v[0]...v[k] (k<=l)
     */
    public void interpolate3(List<float[]> data, List<Double> times, List<SparseVelocityDef> vel)
    {
        reset();

        // cout << "(# data pnts=" << data.size()
        // << ", # time pnts=" << times.size()
        // << ", # vel pnts=" << vel.size() << ")" << endl;

        // # number of unknowns:
        int l = times.size() - 1; // == data.size()
        int m = vel.size() - 1;
        n = l + m + 1;

        // --- assign break points and generate knot vector
        m_afTime.clear();
        m_afTime.add(times.get(0)); // first knot = start time
        int j = 1;
        for (int i = 1; i <= l; i++)
        {
            // add inner breakpoint
            double fT;
            // is not last breakpoint?
            if (i < l) fT = times.get(i);
            else
            // last breakpoint is deferred in preparation for
            // calculating the end velocity of the spline
            fT = times.get(times.size() - 1) + 1E-5f; // i == l
            // cout << "appending breakpoint " << fT << endl;
            m_afTime.add(fT);

            // is velocity defined for this breakpoint?
            if (i < l && j<vel.size() && i == vel.get(j).getIndex())
            {
                // yes -> include twice!
                double dt = (times.get(i + 1) - times.get(i)) * 1E-5f;
                m_afTime.add(times.get(i) + dt);
                j++;
            }
        }
        recomputeKnotVector();

        // --- compose linear system
        List<List<Float>> a = new ArrayList<>();
        CollectionUtils.ensureSize(a, n + 1);
        for (int i = 0; i <= n; i++)
        {

            // a[i].resize(n+1);
            List<Float> fl = new ArrayList<>();
            CollectionUtils.ensureSize(fl, n + 1);
            a.set(i, fl);
            for (j = 0; j <= n; j++)
                a.get(i).set(j, 0f);
        }

        // -- l+1 position constraints
        // row 0: position at t_3 = p0
        a.get(0).set(0, 1f);
        // row 1..l-1: position at t_4,t_6,...t_2l
        Matrix N = new Matrix(n + k, k + 1);
        j = 1;
        int kIdx = k - 1;
        for (int i = 1; i < l; i++)
        {
            ++kIdx;
            // cout << "i=" << i << ",kIdx=" << kIdx << ",t[kIdx]=" << t[kIdx] << endl;
            calcBlendingFunctionsAt(t.get(kIdx), N);
            a.get(i).set(kIdx - 3, (float) N.get(kIdx - 3, k));
            a.get(i).set(kIdx - 2, (float) N.get(kIdx - 2, k));
            a.get(i).set(kIdx - 1, (float) N.get(kIdx - 1, k));

            // skip double breakpoints due to given velocity constraints
            if (j<vel.size() && i == vel.get(j).getIndex())
            {
                ++j;
                ++kIdx;
            }
        }
        // row l: position at t_n+1 = p_l
        a.get(l).set(n, 1f);

        // -- m+1 velocity constraints (row l+1... l+m)
        // row l+1: start velocity (at t_k-1 = t_3)
        // cout << "impose vel constraint at t=" << t[k-1] << endl;
        calcDotBlendFunctionsAt(t.get(3), N);
        a.get(l + 1).set(0, (float) N.get(0, k));
        a.get(l + 1).set(1, (float) N.get(1, k));
        a.get(l + 1).set(2, (float) N.get(2, k));
        // row l+2..l+m-1
        for (int i = 1; i < m; i++)
        {
            int row = l + 1 + i;
            j = vel.get(i).getIndex();
            // cout << "impose vel constraint at t=" << times[j] << endl;
            calcDotBlendFunctionsAt(times.get(j), N);
            // j = GetLowerKnotIndex(t[j]);
            for (int h = 0; h <= n; h++)
                a.get(row).set(h, (float) N.get(h, k));
        }
        // row l+m: end velocity at end time
        // cout << "impose vel constraint at t=" << times.back() << endl;
        calcDotBlendFunctionsAt(times.get(times.size() - 1), N);
        a.get(n).set(n - 2, (float) N.get(n - 2, k));
        a.get(n).set(n - 1, (float) N.get(n - 1, k));
        a.get(n).set(n, (float) N.get(n, k));

        // for (int i=0; i<=n; i++) {
        // for (int j=0; j<=n; j++) {
        // cout << a[i][j] << " ";
        // }
        // cout << endl;
        // }

        // cout << "composing vector" << endl;
        // --- compose result vector
        List<float[]> b = new ArrayList<>();
        CollectionUtils.ensureSize(b, n + 1);        
        
        for (int i = 0; i <= l; i++)
        {
            // b[i]=data[i];
            //Vec3f.set(b.get(i), data.get(i));
            b.set(i,Vec3f.getVec3f(data.get(i)));
        }
        for (int i = l + 1; i <= n; i++)
        {
            b.set(i, vel.get(i - l - 1).getVelocity());
            // cout << "b from vel: " << b[i][0] << "," << b[i][1] << "," << b[i][2] << endl;
        }

        // for (int i=0;i<b.size();i++) cout << "b[" << i << "] = " << b[i][0] << "," << b[i][1] << "," << b[i][2] << endl;
        // for (int i=0;i<a.size();i++) cout << "a[" << i << "] = " << a[i][0] << "," << a[i][1] << "," << a[i][2] << endl;

        // cout << "solving system" << endl;
        // --- solve linear system
        if (!LinearSystem.solve(a, b)) log.warn("MgcNUBSpline3::interpolate : couldn't solve linear system!");

        c = b;

    }
}
