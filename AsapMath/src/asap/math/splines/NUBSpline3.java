/*******************************************************************************
 *******************************************************************************/
package asap.math.splines;

import hmi.math.Mat3f;
import hmi.math.Vec3f;
import hmi.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import asap.math.LinearSystem;
import asap.math.Matrix;

/**
 * B-Spline with open uniform knot vector (non-periodic)
 * @author hvanwelbergen
 * @author Stefan Kopp
 */
@Slf4j
public class NUBSpline3
{
    protected int k; // = spline order (k=4 for cubics)
    protected int n; // n+1 = number of control points
    protected List<Double> t = new ArrayList<>(); // knot vector
    protected List<Double> m_afTime = new ArrayList<>(); // vector of control times
    protected List<float[]> c = new ArrayList<>(); // control points
    /*
    private double knotMax;
    private double m_fTMin, m_fTMax;
    */
    
    public NUBSpline3(int o)
    {
        reset();
        k = o;
    }

    public void reset()
    {
        n = 0;
        c.clear();
        t.clear();
        //knotMax = 0;
    }

    /**
     * Append a control point and extent knot vector automatically
     * due to current knot vector mode.
     * Note: For each b-spline of order k, k control points are
     * required!
     */
    public void appendControlPoint(float[] v, double time)
    {
        // no time assigned -> chordal parametrization
        if (time == -1.0)
        {
            if (c.size() > 1)
            {
                float[] v1 = c.get(c.size() - 2);
                float[] v2 = c.get(c.size() - 1);
                float dv1 = Vec3f.distanceBetweenPoints(v2, v1);
                float dv2 = Vec3f.distanceBetweenPoints(v, v2);
                double dt1 = m_afTime.get(m_afTime.size() - 1) - m_afTime.get(m_afTime.size() - 2);
                time = m_afTime.get(m_afTime.size() - 1) + dt1 * dv2 / dv1;
            }
            else time = m_afTime.get(m_afTime.size() - 1);
        }
        // cout << "append control point=" << v << ", "<< fTime << endl;
        c.add(v);
        m_afTime.add(time);
        n = c.size() - 1;

        // -- extend open uniform knot vector
        recomputeKnotVector();
    }

    public void appendControlPoint(float v[])
    {
        appendControlPoint(v, -1);
    }

    public void appendControlPoints(List<float[]> cv)
    {
        for (float[] cp : cv)
        {
            appendControlPoint(cp);
        }
    }

    public void setControlPoints(List<float[]> cv)
    {
        c.clear();
        m_afTime.clear();
        appendControlPoints(cv);
    }

    public void setControlPoint(int i, float[] p)
    {
        if (i <= n) c.set(i, p);
    }

    public void setKnotVector(List<Double> kv)
    {
        if (kv.size() != (n + k))
        {
            log.warn("MgcNUBSpline::setKnotVector : invalud number of knots assigned!");
        }
        else
        {
            t.clear();
            t = kv;
            //knotMax = t.get(t.size() - 1);
        }
    }

    protected void recomputeKnotVector()
    {
        if (!m_afTime.isEmpty())
        {

            // t=(t_0=...=t_k-1, t_k,........,t_n, t_n+1=...=t_n+k)
            // t=(ct[0],..,ct[0],ct[1],...,ct[n-k+1],ct[n-k+2],..,ct[n-k+2])
            // ---- k ------- -------- k -------

            // cout << "n=" << n << ";k=" << k << endl;
            CollectionUtils.ensureSize(t, n + k + 1);

            // first k equal knots
            for (int i = 0; i < k; i++)
            {
                // i=0..k-1
                t.set(i, m_afTime.get(0));// +i*0.1;
            }

            // variable knots inbetween
            if (n + 1 > k)
            {
                for (int i = k; i <= n; i++)
                {
                    // i=k..n
                    
                    t.set(i, m_afTime.get(i - k + 1));
                    // cout << "Knoten " << i << "=" << m_afTime[i-k+1] << endl;
                }
            }
            else
            {
                for (int i = n + 1; i <= k; i++)
                {
                    // i=n..k
                    //t.set(i, m_afTime.get(i - n + 1));
                    
                    //BUGFIX by Herwin
                    int tIndex = (i - n + 1);
                    if( tIndex >=m_afTime.size())
                    {
                        tIndex = m_afTime.size()-1;
                    }
                    t.set(i, m_afTime.get(tIndex));
                }
            }

            // last k equal knots
            for (int i = n + 1; i <= (n + k); i++)
            {
                // i=n+1..n+k
                //BUGFIX by Herwin
                //t.set(i, m_afTime.get(n - k + 2));
                t.set(i, m_afTime.get(m_afTime.size()-1));
            }

            //knotMax = t.get(t.size() - 1);
            // cout << "new knot vector:";
            // printKnotVector(cout);

            // set extremal times
            //m_fTMin = m_afTime.get(0);
            //m_fTMax = m_afTime.get(m_afTime.size() - 1);
        }
    }

    protected void calcBlendingFunctionsAt(double u, Matrix N, int o)
    {
        if (o == -1) o = k;
        for (int r = 1; r <= o; r++)
        {
            for (int i = 0; i <= (n + o - r); i++)
            {
                if (r == 1)
                {
                    if (t.get(i) <= u && u < t.get(i + 1)) N.set(i, r, 1.0);
                    else N.set(i, r, 0.0);
                }
                else
                {
                    double s1, s2, d1, d2;

                    d1 = t.get(i + r - 1) - t.get(i);
                    if (d1 == 0.0) s1 = 0.0f;
                    else s1 = N.get(i, r - 1) * (u - t.get(i)) / d1;

                    d2 = t.get(i + r) - t.get(i + 1);
                    if (d2 == 0.0) s2 = 0.0f;
                    else s2 = N.get(i + 1, r - 1) * (t.get(i + r) - u) / d2;

                    N.set(i, r, s1 + s2);
                }
            }
        }
    }

    protected void calcBlendingFunctionsAt(double t, Matrix N)
    {
        calcBlendingFunctionsAt(t, N, -1);
    }

    protected void calcDotBlendFunctionsAt(double t, Matrix N)
    {
        calcDotBlendFunctionsAt(t, N, -1);
    }

    private void calcDotBlendFunctionsAt(double u, Matrix Nd, int o)
    {
        if (o == -1) o = k;
        double f = o - 1;
        calcBlendingFunctionsAt(u, Nd, o);
        double s1, td1, s2, td2;
        for (int i = 0; i <= n; i++)
        {
            td1 = t.get(i + o) - t.get(i + 1);
            td2 = t.get(i + o - 1) - t.get(i);
            // NOTE: Nd(n+1,o-1) is supposed to be 0 !!!
            if (td1 == 0.) s1 = 0.;
            else s1 = Nd.get(i + 1, o - 1) / td1;
            if (td2 == 0.) s2 = 0.;
            else s2 = Nd.get(i, o - 1) / td2;
            Nd.set(i, o, f * (s2 - s1));
        }
    }

    private void calcDDotBlendFunctionsAt(double fTime, Matrix Ndd)
    {
        int o = k;
        double f = (o - 2) * (o - 1);
        calcBlendingFunctionsAt((float) fTime, Ndd);
        double di2i, di3i, di3i1, di4i1, di4i2, s1, s2, s3;
        for (int i = 0; i <= n; i++)
        {
            di2i = t.get(i + 2) - t.get(i);
            di3i = t.get(i + 3) - t.get(i);
            if (di2i != 0. && di3i != 0.) s1 = Ndd.get(i, 2) / (di2i * di3i);
            else s1 = 0.;

            di3i1 = t.get(i + 3) - t.get(i + 1);
            di4i1 = t.get(i + 4) - t.get(i + 1);
            if (di3i1 != 0. && di4i1 != 0. && di3i != 0.) s2 = Ndd.get(i + 1, 2) / di3i1 * (1 / di4i1 + 1 / di3i);
            else s2 = 0.;

            di4i2 = t.get(i + 4) - t.get(i + 2);
            if (di4i1 != 0. && di4i2 != 0.) s3 = Ndd.get(i + 2, 2) / (di4i2 * di4i1);
            else s3 = 0.;
            Ndd.set(i, k, f * (s1 - s2 + s3));
        }
    }

    private void calcNthDerivativeCoeffs(int d, List<float[]> cd, int l, int u)
    {
        double td;
        float[] tmp = Vec3f.getVec3f();
        // intialize for zero order
        for (int j = 0; j <= d; j++)
        {
            for (int i = u; i >= l; i--)
            {
                if (j == 0) cd.set(i, c.get(i));
                else
                {
                    td = t.get(i + k - j) - t.get(i);
                    if (td > 0.)
                    {
                        if (i > 0)
                        {
                            if (i > n)
                            {
                                tmp = Vec3f.getVec3f(cd.get(i - 1));
                                Vec3f.scale(-1f / (float) td, tmp);
                                cd.set(i, tmp);
                            }
                            else
                            {
                                // cd[i]=(cd[i]-cd[i-1])/td;                                
                                tmp = Vec3f.getVec3f(cd.get(i));
                                Vec3f.sub(tmp, cd.get(i - 1));
                                Vec3f.scale(1f / (float) td, tmp);
                                cd.set(i, tmp);
                            }
                        }
                        else
                        {
                            // cd.set(i,cd.get(i)/td);
                            tmp = Vec3f.getVec3f(cd.get(i));
                            Vec3f.scale(1f / (float) td, tmp);
                            cd.set(i, tmp);
                        }
                    }
                    // else-zweig spielt keine rolle, da in diesem fall
                    // N(r,k-j) verschwindet -> cd[r] egal !
                    else cd.set(i, Vec3f.getZero());
                }
            }
        }
    }

    private float[] getNthDerivative(double fTime, int d)
    {
        int kMd = k - d;
        float[] tmp = Vec3f.getVec3f();

        if (kMd > 0)
        {

            // get summation boundaries due to local support of B-Splines
            // and their derivative!
            int kIndex = getLowerKnotIndex(fTime);
            if (kIndex - kMd < 0) return Vec3f.getZero();

            int lowerBnd = 0; // de Boor: kIndex-kMd+1;
            int upperBnd = kIndex; // deBoor: kIndex

            if (upperBnd >= (n + k))
            {
                log.warn(
                        "MgcNUBSpline3::GetNthDerivative: (WARNING) Index for N is out of range: {} >= {} The (0,0,0) vector is returned.",
                        upperBnd, (n + k));
                return Vec3f.getZero();
            }

            // factor for "average mesh length"
            double c = 1.;
            if (d > 0) for (int i = 1; i <= d; i++)
                c *= (k - i);

            // calculate basis functions N(i,k-d)
            Matrix N = new Matrix(n + k, kMd + 1);
            calcBlendingFunctionsAt(fTime, N, kMd);

            // differentiate B-Spline coefficients (control points)
            // vector<MgcVector3> cd (n+1);
            List<float[]> cd = new ArrayList<>();
            CollectionUtils.ensureSize(cd, n + 1);
            calcNthDerivativeCoeffs(d, cd, lowerBnd, upperBnd);

            // compute derivative vector
            float f[] = Vec3f.getVec3f(0, 0, 0);
            for (int i = lowerBnd; i <= upperBnd; i++)
            {
                tmp = Vec3f.getVec3f(cd.get(i));
                Vec3f.scale((float) N.get(i, k - d), tmp);
                Vec3f.add(f, tmp);
            }
            Vec3f.scale((float) c, f);
            return f;

            // ----------------------------------------
            // less efficient calculation using time derivatives
            // of the blending functions
            // leda_matrix Nd (n+k,k+1);
            // calcDotBlendFunctionsAt(fTime,Nd);
            // MgcVector3 f (0.,0.,0.);
            // for (int i=0; i<=n; i++)
            // f+=(Nd(i,k)*c[i]);
            // return f;
            // ----------------------------------------
        }
        log.warn("GetNthDerivative : derivative of  order {} doesn't exist!", d);
        return Vec3f.getZero();
    }

    public void interpolate(List<float[]> data, List<Double> times)
    {
        // reset spline and construct new knot vector
        reset();
        int s = data.size(); // == times.size() !!!!

        // let's have open uniform knot vector and n+1 control points
        // each of which influences the curve over [t_i,t_i+k], i=0..n
        // -> t = [t_0=...=t_k-1,t_k,...,t_n,t_n+1=...=t_n+k]
        // -> curve defined over [t_{k-1},t_{n+1}]
        // -> (n+1)-(k-1)=n-k+2 distinct intervals
        // -> n-k+3 distinct breakpoints == s given data points
        // k=4: s=n-1
        // -> with n+1 unknown control points two additional conditions
        // required!
        n = s + 1;
        CollectionUtils.ensureSize(c, n + 1);

        m_afTime = times;
        recomputeKnotVector();

        // compute control points satisfying the interpolation conditions
        List<Float> alpha = new ArrayList<>();
        
        CollectionUtils.ensureSize(alpha, s - 1);
        List<Float> beta = new ArrayList<>();
        
        CollectionUtils.ensureSize(beta, s);
        List<Float> gamma = new ArrayList<>();
        CollectionUtils.ensureSize(gamma, s - 1);

        beta.set(0, 1f);
        gamma.set(0, 0f);

        for (int i = 1; i < s - 1; i++)
        {
            Matrix N = new Matrix(n + k, k + 1);
            calcBlendingFunctionsAt(t.get(i + k - 1), N);
            alpha.set(i - 1, (float) N.get(i, k)); // NOTE: alpha[i] multiplied with data[i+1] !!
            beta.set(i, (float) N.get(i + 1, k));
            gamma.set(i, (float) N.get(i + 2, k));
        }
        alpha.set(s - 2, 0f);
        beta.set(s - 1, 1f);

        // solve corresponding tridiaginal linear system
        if (!LinearSystem.solveTri2(alpha, beta, gamma, data, c)) log.warn("MgcNUBSpline3::interpolate : couldn't solve linear system!");

        // double boundary points: c[1]=c[0];c[s]=c[s-1];
        for (int i = s; i > 0; i--)
        {
            c.set(i, c.get(i - 1));
        }
        c.set(s + 1, c.get(s));
    }

    public void interpolate(List<float[]> data, List<Double> time, float[] vStart, float[] vEnd)
    {
        reset();

        int l = data.size() - 1; // data points: d[0]...d[s-1] = d[n-2]
        n = l + 2; // #control points = l+3

        m_afTime.clear();
        for (int i = 0; i < l; i++)
            m_afTime.add(time.get(i));
        // last breakpoint is deferred in preparation for calculating the end velocity of the spline
        m_afTime.add(time.get(time.size() - 1) + 1E-5);
        recomputeKnotVector();

        // compute control points satisfying the interpolation conditions
        // --- compose matrix of corresp. linear system:
        List<List<Float>> a = new ArrayList<>();
        CollectionUtils.ensureSize(a, n + 1);

        for (int i = 0; i <= n; i++)
        {
            // a[i].resize(n+1);
            List<Float> fl = new ArrayList<>();
            CollectionUtils.ensureSize(fl, n + 1);
            a.set(i, fl);

            for (int j = 0; j <= n; j++)
            {
                a.get(i).set(j, 0f);
            }
        }

        // --- l+1 position constraints
        // row 0: position at t_k-1 = p0 -> knot equals 1
        a.get(0).set(0, 1f);
        // row 1...l-1: positions at u_1,...,u_l-1
        Matrix N = new Matrix(n + k, k + 1);
        for (int i = 1; i < l; i++)
        {
            calcBlendingFunctionsAt(time.get(i), N);
            a.get(i).set(i, (float) N.get(i, k));
            a.get(i).set(i + 1, (float) N.get(i + 1, k));
            a.get(i).set(i + 2, (float) N.get(i + 2, k));
        }
        // row l: position at u_l (=t_n+1) = p_n
        a.get(l).set(n, 1f);

        // -- velocity constraints
        // row l+1: velocity at t_3 = vStart
        calcDotBlendFunctionsAt(t.get(3), N);
        a.get(l + 1).set(0, (float) N.get(0, k));
        a.get(l + 1).set(1, (float) N.get(1, k));
        a.get(l + 1).set(2, (float) N.get(2, k));
        // row l+2: velocity at t_n+1 = vEnd
        calcDotBlendFunctionsAt(time.get(time.size() - 1), N);
        a.get(n).set(n - 2, (float) N.get(n - 2, k));
        a.get(n).set(n - 1, (float) N.get(n - 1, k));
        a.get(n).set(n, (float) N.get(n, k));

        // for (int i=0; i<=n; i++) {
        // for (int j=0; j<=n; j++) {
        // cout << a[i][j] << " ";
        // }
        // cout << endl;
        // }

        // --- compose result vector
        List<float[]> b = new ArrayList<>();
        CollectionUtils.ensureSize(b, n + 1);
        for (int i = 0; i <= l; i++)
        {
            b.set(i, data.get(i));
        }
        b.set(l + 1, vStart);
        b.set(l + 2, vEnd);

        // --- solve linear system
        if (!LinearSystem.solve(a, b))
        {
            log.warn("MgcNUBSpline3::interpolate : couldn't solve linear system!");

        }
        // control points: p[0]...p[n]
        c = b;
    }

    /**
     * data points x[0]...x[l]
     * times t[0]...t[l]
     * velocities v[0]...v[l]
     */
    void interpolate(List<float[]> data, List<Double> times, List<float[]> vel)
    {
        // cout << "do save interpolation" << endl;
        reset();
        int l = data.size() - 1; // x[0]...x[l], times[0]...times[l]

        n = 2 * l + 1; // #control points = 2l+2
        if (l > 0)
        {

            // --- assign break points and generate knot vector
            m_afTime.clear();
            m_afTime.add(times.get(0));
            for (int i = 1; i < l; i++)
            {
                m_afTime.add(times.get(i));
                // -- determine additional inner knots by applying
                // -- average parametrization
                // m_afTime.push_back(times[i]);
                m_afTime.add(times.get(i) + 0.1f);
            }
            // last breakpoint is deferred in preparation for
            // calculating the end velocity of the spline
            m_afTime.add(times.get(times.size() - 1) + 1E-5f);
            recomputeKnotVector();

            // --- compose linear system
            // cout << "composing matrix" << endl;
            List<List<Float>> a = new ArrayList<>();
            CollectionUtils.ensureSize(a, n + 1);
            for (int i = 0; i <= n; i++)
            {

                // a[i].resize(n+1);

                List<Float> fl = new ArrayList<>();
                CollectionUtils.ensureSize(fl, n + 1);
                a.set(i, fl);
                for (int j = 0; j <= n; j++)
                    a.get(i).set(j, 0f);
            }

            // -- l+1 position constraints
            // row 0: position at t_3 = p0
            a.get(0).set(0, 1f);
            // row 1..l-1: position at t_4,t_6,...t_2l
            Matrix N = new Matrix(n + k, k + 1);
            for (int i = 1; i < l; i++)
            {
                int j = 2 * (i + 1);
                calcBlendingFunctionsAt(t.get(j), N);
                // int j = k+2*(i-1); // compute breakpoint index
                a.get(i).set(j - 3, (float) N.get(j - 3, k));
                a.get(i).set(j - 2, (float) N.get(j - 2, k));
                a.get(i).set(j - 1, (float) N.get(j - 1, k));
            }
            // row l: position at t_n+1 = p_l
            a.get(l).set(n, 1f);

            // -- l+1 velocity constraints
            // row l+1: velocity at t_3
            calcDotBlendFunctionsAt(t.get(3), N);
            a.get(l + 1).set(0, (float) N.get(0, k));
            a.get(l + 1).set(1, (float) N.get(1, k));
            a.get(l + 1).set(2, (float) N.get(2, k));

            // row l+2..l+2+(l-1)=2l+1
            for (int i = 1; i < l; i++)
            {
                int row = l + 1 + i;
                int j = 2 * (i + 1);
                calcDotBlendFunctionsAt(t.get(j), N);
                // j = GetLowerKnotIndex(t[j]);
                for (int h = 0; h < j; h++)
                    a.get(row).set(h, (float) N.get(h, k));
            }

            // row 2l+2: velocity at end time
            // calcDotBlendFunctionsAt(t[n+1],N);
            calcDotBlendFunctionsAt(times.get(times.size() - 1), N);
            a.get(n).set(n - 2, (float) N.get(n - 2, k));
            a.get(n).set(n - 1, (float) N.get(n - 1, k));
            a.get(n).set(n, (float) N.get(n, k));

            // cout << "composing vector" << endl;
            // --- compose result vector
            List<float[]> b = new ArrayList<>();
            CollectionUtils.ensureSize(b, n + 1);
            for (int i = 0; i <= l; i++)
                b.set(i, data.get(i));
            for (int i = l + 1; i <= n; i++)
                b.set(i, vel.get(i - l - 1));

            // cout << "solving system" << endl;
            // --- solve linear system
            if (!LinearSystem.solve(a, b)) log.warn("MgcNUBSpline3::interpolate : couldn't solve linear system!");

            // control points: p[0]...p[n]
            c = b;
            // printControlPoints(cout);
        }
        else log.warn("interpolate: not enough data points provided!");
    }

    // data points x[0]...x[l]
    // times t[0]...t[l]
    // velocities v[0]...v[l]
    public void interpolate2(List<float[]> data, List<Double> times, List<float[]> vel)
    {
        // cout << "do new interpolation" << endl;
        reset();
        int l = data.size() - 1; // x[0]...x[l], times[0]...times[l]
        if (l != times.size() - 1 || l != vel.size() - 1)
        {
            log.warn("invalid array sizes!!");
            return;
        }

        n = 2 * l + 2; // #control points = 2l+3
        if (l > 0)
        {

            // --- assign break points and generate knot vector
            m_afTime.clear();
            m_afTime.add(times.get(0));
            for (int i = 1; i < l; i++)
            {
                m_afTime.add((times.get(i - 1)));// +times[i])/2);
                // -- determine additional inner knots by applying
                // -- average parametrization
                m_afTime.add((times.get(i)));// +times[i])/2);
            }
            // last breakpoint is deferred in preparation for
            // calculating the end velocity of the spline
            m_afTime.add((times.get(l - 1) + times.get(l)) / 2f);
            m_afTime.add(times.get(l) + 1E-5f);
            recomputeKnotVector();

            // --- compose linear system
            // cout << "composing matrix" << endl;
            List<List<Float>> a = new ArrayList<>();
            CollectionUtils.ensureSize(a, n + 1);

            for (int i = 0; i <= n; i++)
            {

                // a[i].resize(n+1);
                List<Float> fl = new ArrayList<>();
                CollectionUtils.ensureSize(fl, n + 1);
                a.set(i, fl);
                for (int j = 0; j <= n; j++)
                    a.get(i).set(j, 0f);
            }

            // -- l+1 position constraints (row 0 ... l)
            // row 0: position at t_3 = p0
            a.get(0).set(0, 1f);
            // row 1..l-1: position at t_4,t_6,...t_2l
            Matrix N = new Matrix(n + k, k + 1);
            for (int i = 1; i < l; i++)
            {
                int j = 2 * (i + 1);
                calcBlendingFunctionsAt(t.get(j), N);
                // int j = k+2*(i-1); // compute breakpoint index
                a.get(i).set(j - 3, (float) N.get(j - 3, k));
                a.get(i).set(j - 2, (float) N.get(j - 2, k));
                a.get(i).set(j - 1, (float) N.get(j - 1, k));
            }
            // row l: position at t_n+1 = p_l (-> knot equals 1)
            a.get(l).set(n, 1f);

            // -- l+1 velocity constraints (row l+1 ... 2l+2)
            // row l+1: velocity at u_0 = t_3
            calcDotBlendFunctionsAt(t.get(3), N);
            a.get(l + 1).set(0, (float) N.get(0, k));
            a.get(l + 1).set(1, (float) N.get(1, k));
            a.get(l + 1).set(2, (float) N.get(2, k));
            // row l+2..l+2+(l-1)=2l+1
            for (int i = 1; i < l; i++)
            {
                int row = l + 1 + i;
                int j = 2 * (i + 1);
                calcDotBlendFunctionsAt(t.get(j), N);
                // j = GetLowerKnotIndex(t[j]);
                for (int h = 0; h < j; h++)
                    a.get(row).set(h, (float) N.get(h, k));
            }
            // row 2l+2: velocity at end time u_l
            calcDotBlendFunctionsAt(times.get(times.size() - 1), N);
            a.get(n - 1).set(n - 2, (float) N.get(n - 2, k));
            a.get(n - 1).set(n - 1, (float) N.get(n - 1, k));
            a.get(n - 1).set(n, (float) N.get(n, k));

            // -- 1 acceleration constraint (row 2l+3)
            // row 2l+3: acceleration at end time
            calcDDotBlendFunctionsAt(times.get(times.size() - 1), N);
            // cout << "N-DDot at " << t[n+1] << "=" << N << endl;
            // a[n][n]=N(n,k);
            for (int i = 0; i <= n; i++)
            {
                a.get(n).set(i, (float) N.get(i, k));
            }

            // --- compose result vector
            List<float[]> b = new ArrayList<>();
            CollectionUtils.ensureSize(b, n + 1);
            for (int i = 0; i <= l; i++)
                b.set(i, data.get(i));
            for (int i = l + 1; i <= n; i++)
                b.set(i, vel.get(i - l - 1));
            b.set(n, Vec3f.getVec3f(0f, 0f, 0f));

            // cout << "solving system" << endl;
            // --- solve linear system
            if (!LinearSystem.solve(a, b)) log.warn("MgcNUBSpline3::interpolate : couldn't solve linear system!");

            // control points: p[0]...p[n]
            c = b;
            // printControlPoints(cout);
        }
        else log.warn("interpolate: not enough data points provided!");
    }

    /**
     * data points x[0]...x[l], times t[0]...t[l]
     * velocities v[0]...v[l], accelerations a[0]...a[l]
     */
    public void interpolate(List<float[]> data, List<Double> times, List<float[]> vel, List<float[]> acc)
    {
        reset();
        int l = data.size() - 1; // x[0]...x[l], times[0]...times[l]

        n = 3 * l + 2; // #control points = 3l+3
        if (l > 0)
        {

            // --- assign break points and generate knot vector
            m_afTime.clear();
            // m_afTime.push_back(times[0]);
            for (int i = 0; i < l; i++)
            {
                double t = times.get(i);
                m_afTime.add(t);
                // -- determine additional inner knots by applying
                // -- average parametrization
                m_afTime.add(times.get(i) + 1.0f / 3.0f * (times.get(i + 1) - times.get(i)));
                m_afTime.add(times.get(i) + 2.0f / 3.0f * (times.get(i + 1) - times.get(i)));
            }
            // last breakpoint is deferred in preparation for
            // calculating the end velocity of the spline
            m_afTime.add(times.get(times.size() - 1) + 1E-5f);
            recomputeKnotVector();

            // --- compose linear system
            // cout << "composing matrix" << endl;
            List<List<Float>> a = new ArrayList<>();
            CollectionUtils.ensureSize(a, n + 1);

            for (int i = 0; i <= n; i++)
            {

                // a[i].resize(n+1);
                List<Float> fl = new ArrayList<>();
                CollectionUtils.ensureSize(fl, n + 1);
                a.set(i, fl);
                for (int j = 0; j <= n; j++)
                    a.get(i).set(j, 0f);
            }

            // -- l+1 position constraints
            // cout << "positions..." << endl;
            // row 0: position at t_3 = p0
            a.get(0).set(0, 1f);
            // row 1..l-1: position at t_4,t_6,...t_2l
            Matrix N = new Matrix(n + k, k + 1);
            for (int i = 1; i < l; i++)
            {
                int j = 3 * (i + 1);
                calcBlendingFunctionsAt(t.get(j), N);
                a.get(i).set(j - 3, (float) N.get(j - 3, k));
                a.get(i).set(j - 2, (float) N.get(j - 2, k));
                a.get(i).set(j - 1, (float) N.get(j - 1, k));
            }
            // row l: position at t_n+1 = p_l
            a.get(l).set(n, 1f);

            // -- l+1 velocity constraints
            // cout << "velocities..." << endl;
            // row l+1: velocity at t_3
            calcDotBlendFunctionsAt(t.get(3), N);
            a.get(l + 1).set(0, (float) N.get(0, k));
            a.get(l + 1).set(1, (float) N.get(1, k));
            a.get(l + 1).set(2, (float) N.get(2, k));
            // row l+2..l+2+(l-1)=2l+1
            for (int i = 1; i < l; i++)
            {
                int row = l + 1 + i;
                int j = 3 * (i + 1);
                calcDotBlendFunctionsAt(t.get(j), N);
                for (int h = 0; h < j; h++)
                    a.get(row).set(h, (float) N.get(h, k));
            }
            // row 2l+2: velocity at end time
            calcDotBlendFunctionsAt(times.get(times.size() - 1), N);
            a.get(2 * l + 2).set(n - 2, (float) N.get(n - 2, k));
            a.get(2 * l + 2).set(n - 1, (float) N.get(n - 1, k));
            a.get(2 * l + 2).set(n, (float) N.get(n, k));

            // -- l+1 acceleration constraints
            // cout << "accelerations..." << endl;
            for (int i = 1; i <= l + 1; i++)
            {
                //int row = 2 * l + 1 + i;
                int j = 3 * (i + 1);
                calcDDotBlendFunctionsAt(t.get(j), N);
                for (j = 0; j <= n; j++)
                    a.get(n).set(j, (float) N.get(j, k));
            }

            // --- compose result vector
            // cout << "composing vector" << endl;

            // --- compose result vector
            List<float[]> b = new ArrayList<>();
            CollectionUtils.ensureSize(b, n + 1);
            for (int i = 0; i <= l; i++)
                b.set(i, data.get(i));
            for (int i = l + 1; i <= 2 * l + 1; i++)
                b.set(i, vel.get(i - l - 1));
            for (int i = 2 * l + 2; i <= n; i++)
                b.set(i, vel.get(i - l - 1));

            // cout << "solving system" << endl;
            // --- solve linear system
            if (!LinearSystem.solve(a, b)) log.warn("MgcNUBSpline3::interpolate : couldn't solve linear system!");

            // control points: p[0]...p[n]
            c = b;
            // printControlPoints(cout);
        }
        else log.warn("interpolate: too less data points provided!");
    }

    private int insertKnotAt(double fTime)
    {
        float tmp[] = Vec3f.getVec3f();
        float tmp2[] = Vec3f.getVec3f();
        // insert data point and recompute knot vector
        // vector<MgcReal>::iterator it;
        int j = 0;

        for (double it : t)
        {
            if (it >= fTime)
            {
                n += 1;
                // t.insert(it,fTime);
                break;
            }
            ++j;
        }
        t.add(j, fTime);
        j -= 1;

        // cNew.push_back((1-a[i])*c[i-1] + a[i]*c[i]);
        // printKnotVector(cout);

        // prepare control point calculation
        List<Double> a = new ArrayList<>();
        for (int i = 0; i <= n; i++)
        {
            if (i <= (j - k)) a.add(1.0d);
            else if (i <= j) a.add((fTime - t.get(i)) / (t.get(i + k) - t.get(i)));
            else a.add(0d);
        }

        // recompute control points
        List<float[]> cNew = new ArrayList<>();
        cNew.add(c.get(0)); // c[0] remains untouched
        for (int i = 1; i < n; i++)
        {
            // cNew.push_back((1-a[i])*c[i-1] + a[i]*c[i]);
            tmp = Vec3f.getVec3f(c.get(i - 1));
            Vec3f.scale((float)(1 - a.get(i)), tmp);
            tmp2 = Vec3f.getVec3f(c.get(i));
            Vec3f.scale(a.get(i).floatValue(), tmp2);
            Vec3f.add(tmp, tmp2);
            cNew.add(tmp);
        }
        cNew.add(c.get(n - 1)); // c[n] remains untouched

        c = cNew;
        return j;
    }

    /**
     * get curve value at certain value of the independent
     * interpolation parameter u
     */
    public float[] getPosition(double fTime)
    {
        float[] f = Vec3f.getVec3f(0, 0, 0);
        float[] tmp = Vec3f.getVec3f();
        Matrix N = new Matrix(n + k, k + 1);
        calcBlendingFunctionsAt(fTime, N);
        for (int i = 0; i <= n; i++)
        {
            // f += (N(i, k) * c[i]);
            tmp = Vec3f.getVec3f(c.get(i));
            Vec3f.scale((float) N.get(i, k), tmp);
            Vec3f.add(f, tmp);
        }
        return f;
    }

    public float[] getFirstDerivative(double fTime)
    {
        return getNthDerivative(fTime, 1);
    }

    public float[] getSecondDerivative(double fTime)
    {
        return getNthDerivative(fTime, 2);
    }

    public float[] getThirdDerivative(double fTime)
    {
        return getNthDerivative(fTime, 3);
    }

    public float getVariation()
    {
        return 0;
    }

    private int getLowerKnotIndex(double fTime)
    {
        if (t.isEmpty()) return 0;
        if (fTime <= t.get(0)) return 0;
        else if (fTime > t.get(t.size() - 1)) return t.size() - 1;
        else
        {
            for (int i = 0; i < t.size() - 2; i++)
                if (t.get(i) <= fTime && fTime < t.get(i + 1)) return i;
            return t.size() - 1;
        }
    }

    public void rotate(float[] m)
    {
        float[] v = Vec3f.getVec3f();
        for (int i = 0; i < c.size(); i++)
        {
            Mat3f.transform(m, v, c.get(i));
            c.set(i, v);
        }
    }

    public void translate(float[] v)
    {
        float[] tmp = Vec3f.getVec3f();
        for (int i = 0; i < c.size(); i++)
        {
            tmp = Vec3f.getVec3f(c.get(i));
            Vec3f.add(tmp, v);
            c.set(i, tmp);
        }
    }

    public String getKnotVectorString()
    {
        return t.toString();
    }

    // from MgcCurve.inl
    private float getSpeed(double fTime)
    {
        return Vec3f.length(getFirstDerivative(fTime));
    }

    // from MgcMultipleCurve3
    private float getCurvature(double fTime)
    {
        float[] kVelocity = getFirstDerivative(fTime);
        float fSpeedSqr = Vec3f.lengthSq(kVelocity);

        float fTolerance = 1e-06f;
        if (fSpeedSqr >= fTolerance)
        {
            float[] kAcceleration = getSecondDerivative(fTime);
            float[] kCross = Vec3f.getVec3f();
            Vec3f.cross(kCross, kVelocity, kAcceleration);
            float fNumer = Vec3f.length(kCross);
            float fDenom = (float) Math.pow(fSpeedSqr, 1.5);
            return fNumer / fDenom;
        }
        else
        {
            // curvature is indeterminate, just return 0
            return 0.0f;
        }
    }

    public String getControlPointsString()
    {
        return c.toString();
    }

    public String getBlendingFunctionsString(int o)
    {
        if (o == -1) o = k;
        StringBuffer buf = new StringBuffer();
        for (double u = t.get(0); u <= t.get(t.size() - 1); u += 0.01)
        {
            Matrix N = new Matrix(n + k, k + 1);
            calcBlendingFunctionsAt(u, N);
            for (int i = 0; i <= n; i++)
            {
                buf.append(u);
                buf.append(" ");
                buf.append(N.get(i, o));
                buf.append("\n");
            }
        }
        return buf.toString();
    }

    public String getBlendingFunctionsFDString()
    {
        StringBuffer buf = new StringBuffer();
        for (double u = t.get(0); u <= t.get(t.size() - 1); u += 0.01)
        {
            Matrix N = new Matrix(n + k, k + 1);
            calcDotBlendFunctionsAt(u, N);
            for (int i = 0; i <= n; i++)
            {
                buf.append(u);
                buf.append(" ");
                buf.append(N.get(i, k));
                buf.append("\n");
            }
        }
        return buf.toString();
    }

    public String getSplineString()
    {
        float[] v = Vec3f.getVec3f();
        StringBuffer buf = new StringBuffer();
        for (double u = t.get(0); u <= t.get(t.size() - 1); u += 0.01)
        {
            v = getPosition(u);
            buf.append(Vec3f.toString(v));
            buf.append("\n");
        }
        return buf.toString();
    }

    public String getSplineVelocity()
    {
        StringBuffer buf = new StringBuffer();
        for (double u = t.get(0); u <= t.get(t.size() - 1); u += 0.005)
        {
            buf.append("u");
            buf.append(" ");
            buf.append(getSpeed(u));
            buf.append("\n");
        }
        return buf.toString();
    }

    public String getSplineAcc()
    {
        StringBuffer buf = new StringBuffer();
        for (double u = t.get(0); u <= t.get(t.size() - 1); u += 0.01)
        {
            buf.append(u);
            buf.append(" ");
            buf.append(Vec3f.length(getSecondDerivative(u)));
            buf.append("\n");
        }
        return buf.toString();
    }

    public String getSplineCurvature()
    {
        StringBuffer buf = new StringBuffer();
        for (double u = t.get(0); u <= t.get(t.size() - 1); u += 0.01)
        {
            buf.append(u);
            buf.append(" ");
            buf.append(getCurvature(u));
            buf.append("\n");
        }
        return buf.toString();
    }

    /**
     * data points x[0]...x[l]
     * times t[0]...t[l]
     * velocities v[0]...v[l]
     * zero acceleration at tZeroAcc
     */
    public void interpolate(List<float[]> data, List<Double> times, List<float[]> vel, double tZeroAcc)
    {
        reset();
        int l = data.size() - 1; // x[0]...x[l], times[0]...times[l]

        n = 2 * l + 2; // #control points = 2l+3
        if (l > 0)
        {

            if (tZeroAcc > times.get(0) && tZeroAcc < times.get(l))
            {

                // --- assign break points and generate knot vector
                m_afTime.clear();
                m_afTime.add(times.get(0));

                for (int i = 1; i < l; i++)
                {
                    if (times.get(i) >= tZeroAcc) m_afTime.add(tZeroAcc);
                    m_afTime.add((times.get(i)));// -1]+times[i])/2);
                    m_afTime.add(times.get(i));
                }
                if (m_afTime.get(m_afTime.size() - 1) < tZeroAcc) m_afTime.add(tZeroAcc);
                m_afTime.add(times.get(l) + 1E-5f);
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
                    for (int j = 0; j <= n; j++)
                        a.get(i).set(j, 0f);
                }

                // -- l+1 position constraints
                // row 0: position at t_3 = p0
                a.get(0).set(0, 1f);
                // row 1..l-1: position at t_4,t_6,...t_2l
                Matrix N = new Matrix(n + k, k + 1);
                for (int i = 1; i < l; i++)
                {
                    int j = 2 * (i + 1);
                    calcBlendingFunctionsAt(t.get(j), N);
                    // int j = k+2*(i-1); // compute breakpoint index
                    a.get(i).set(j - 3, (float) N.get(j - 3, k));
                    a.get(i).set(j - 2, (float) N.get(j - 2, k));
                    a.get(i).set(j - 1, (float) N.get(j - 1, k));
                }
                // row l: position at t_n+1 = p_l
                a.get(l).set(n, 1f);

                // -- l+1 velocity constraints
                // row l+1: velocity at t_3
                calcDotBlendFunctionsAt(t.get(3), N);
                a.get(l + 1).set(0, (float) N.get(0, k));
                a.get(l + 1).set(1, (float) N.get(1, k));
                a.get(l + 1).set(2, (float) N.get(2, k));

                // row l+2..l+2+(l-1)=2l+1
                for (int i = 1; i < l; i++)
                {
                    int row = l + 1 + i;
                    int j = 2 * (i + 1);
                    calcDotBlendFunctionsAt(t.get(j), N);
                    // j = GetLowerKnotIndex(t[j]);
                    for (int h = 0; h < j; h++)
                        a.get(row).set(h, (float) N.get(h, k));
                }
                // row 2l+2: velocity at end time
                calcDotBlendFunctionsAt(times.get(times.size() - 1), N);
                a.get(n - 1).set(n - 2, (float) N.get(n - 2, k));
                a.get(n - 1).set(n - 1, (float) N.get(n - 1, k));
                a.get(n - 1).set(n, (float) N.get(n, k));

                // -- 1 acceleration constraint
                // row 2l+3: acceleration at tZeroAcc is zero
                calcDDotBlendFunctionsAt(tZeroAcc, N);
                for (int i = 0; i <= n; i++)
                {
                    a.get(n).set(i, (float) N.get(i, k));
                }

                // --- compose result vector
                List<float[]> b = new ArrayList<>();
                CollectionUtils.ensureSize(b, n + 1);
                for (int i = 0; i <= l; i++)
                {
                    b.set(i, data.get(i));
                }
                b.set(n, Vec3f.getVec3f(0, 0, 0));
                for (int i = l + 1; i < n; i++)
                {
                    b.set(i, vel.get(i - l - 1));
                }

                // cout << "solving system" << endl;
                // --- solve linear system
                if (!LinearSystem.solve(a, b)) log.warn("MgcNUBSpline3::interpolate : couldn't solve linear system!");

                // control points: p[0]...p[n]
                c = b;
                // printControlPoints(cout);
            }
            else log.warn("interpolate: illegal time of zero acceleration!");
        }
        else log.warn("interpolate: too less data points provided!");
    }

    public void setPeakAt(float tP)
    {
        // 1. insert additional knot at tP
        int i = insertKnotAt(tP);

        // 2. solve for corresp. control point
        int j = i - 1;

        // 2.1 get coefficients
        Matrix N = new Matrix(n + k, k + 1);
        calcDDotBlendFunctionsAt(tP, N);

        float tmp[] = Vec3f.getVec3f();
        // 2.2 sum of constant control points
        float b[] = Vec3f.getVec3f(0, 0, 0);
        for (i = 0; i <= n; i++)
        {
            if (i != j)
            {
                // b += N.get(i,k)*c[i];
                tmp = Vec3f.getVec3f(c.get(i));
                Vec3f.scale((float) N.get(i, k), tmp);
                Vec3f.add(b, tmp);
            }
        }
        // 2.3 solve for control point
        // MgcVector3 p = -1.0 / N(j,k) * b;
        float p[] = Vec3f.getVec3f(b);
        Vec3f.scale(-1f / (float) N.get(j, k), p, b);
        c.set(j, p);
    }

    public void setUniformAt(float tS, float tE)
    {
        int jS = insertKnotAt(tS);
        insertKnotAt(tS);
        insertKnotAt(tE);
        int jE = insertKnotAt(tE);

        // indices of corresp. control points
        jS -= 1;
        // jE -= 1;
        // cout << "jS=" << jS << ",jE=" << jE << endl;
        // cout << "c[jS]=" << c[jS] << endl;
        // cout << "c[jE]=" << c[jE] << endl;

        // locate the corresp. control points on a straight line
        // MgcVector3 d = c[jE]-c[jS];
        float d[] = Vec3f.getVec3f();
        Vec3f.sub(d, c.get(jE), c.get(jS));
        
        float tmp[] = Vec3f.getVec3f();
        
        //c[jS + 1] = c[jS] + 0.33 * d;
        tmp = Vec3f.getVec3f(d);
        Vec3f.scale(0.33f,tmp);
        Vec3f.add(tmp,c.get(jS));
        c.set(jS+1,tmp);
        
        //c[jE - 1] = c[jS] + 0.66 * d;
        tmp = Vec3f.getVec3f(d);
        Vec3f.scale(0.66f,tmp);
        Vec3f.add(tmp,c.get(jS));
        c.set(jE-1,tmp);
    }
}
