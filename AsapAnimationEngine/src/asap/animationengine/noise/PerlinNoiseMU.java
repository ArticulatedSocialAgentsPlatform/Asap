/*******************************************************************************
 *******************************************************************************/
package asap.animationengine.noise;

import hmi.animation.Hanim;
import hmi.math.PerlinNoise;
import hmi.math.Quat4f;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;
import asap.animationengine.AnimationPlayer;
import asap.animationengine.motionunit.AnimationUnit;
import asap.animationengine.motionunit.TimedAnimationMotionUnit;
import asap.motionunit.MUPlayException;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.pegboard.PegBoard;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterNotFoundException;

import com.google.common.collect.ImmutableSet;

/**
 * Motion unit for applying perlin noise to a set of joints.
 * parameters:
 * joint,
 * offsetx, offsety, offsetz, (default -0.5, -0.5, -0.5)
 * basefreqx, basefreqz, basefreqz, (default 1,1,1)
 * baseamplitudex, baseamplitudey, baseamplitudez, (default 1,1,1)
 * persistencex, persistencey, persistencez, (default 0.5,0.5,0.5)
 * 
 * //basefreq geeft iets aan over de snelheid waarmee de noise loopt.
 * //baseamplitude geeft de amplitude in radialen
 * //offset geeft de offsetangle in radialen
 * //persistence geeft iets aan over de verhouding hoge en lage frequenties. we gebruiken overigens maar 4 octaven
 * //(zie ook uitleg hugo elias http://freespace.virgin.net/hugo.elias/models/m_perlin.htm )
 * 
 * @author Dennis Reidsma
 * 
 */
@Slf4j
public class PerlinNoiseMU implements NoiseMU
{
    private HashMap<String, String> parameters = new HashMap<String, String>(); // name => value set
    private KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();
    private AnimationPlayer aniPlayer;
    private PerlinNoise pnx1 = new PerlinNoise(1024, 0, 1);
    /*
     * private PerlinNoise pnx2 = new PerlinNoise(1024,0,1);
     * private PerlinNoise pnx3 = new PerlinNoise(1024,0,1);
     * private PerlinNoise pnx4 = new PerlinNoise(1024,0,1);
     */
    private PerlinNoise pny1 = new PerlinNoise(1024, 0, 1);
    /*
     * private PerlinNoise pny2 = new PerlinNoise(1024,0,1);
     * private PerlinNoise pny3 = new PerlinNoise(1024,0,1);
     * private PerlinNoise pny4 = new PerlinNoise(1024,0,1);
     */
    private PerlinNoise pnz1 = new PerlinNoise(1024, 0, 1);
    /*
     * private PerlinNoise pnz2 = new PerlinNoise(1024,0,1);
     * private PerlinNoise pnz3 = new PerlinNoise(1024,0,1);
     * private PerlinNoise pnz4 = new PerlinNoise(1024,0,1);
     */

    protected AnimationPlayer player;
    float[] q = new float[4];

    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
    }

    @Override
    public List<KeyPosition> getKeyPositions()
    {
        return keyPositionManager.getKeyPositions();
    }

    @Override
    public void setKeyPositions(List<KeyPosition> p)
    {
        keyPositionManager.setKeyPositions(p);
    }

    @Override
    public void removeKeyPosition(String id)
    {
        keyPositionManager.removeKeyPosition(id);
    }

    @Override
    public KeyPosition getKeyPosition(String name)
    {
        return keyPositionManager.getKeyPosition(name);
    }

    public PerlinNoiseMU()
    {
        setParameterValue("joint", Hanim.skullbase);
        setFloatParameterValue("offsetx", -0.1f);
        setFloatParameterValue("offsety", 0f);
        setFloatParameterValue("offsetz", 0f);
        setFloatParameterValue("basefreqx", 1f);
        setFloatParameterValue("basefreqy", 1f);
        setFloatParameterValue("basefreqz", 1f);
        setFloatParameterValue("baseamplitudex", 0.5f);
        setFloatParameterValue("baseamplitudey", 0f);
        setFloatParameterValue("baseamplitudez", 0f);
        setFloatParameterValue("persistencex", 0.5f);
        setFloatParameterValue("persistencey", 0.5f);
        setFloatParameterValue("persistencez", 0.5f);
    }

    @Override
    public double getPreferedDuration()
    {
        return 1;
    }

    @Override
    public void play(double t) throws MUPlayException
    {
        try
        {

            float rotxRad = getFloatParameterValue("offsetx") + getFloatParameterValue("baseamplitudex")
                    * pnx1.noise((float) t * getFloatParameterValue("basefreqx"));
            // + baseamplitudex*persistence*pnx2.noise((float)currentTime*basefreqx*2)
            // + baseamplitudex*persistence*persistence*pnx3.noise((float)currentTime*basefreqx*4)
            // + baseamplitudex*persistence*persistence*persistence*pnx4.noise((float)currentTime*basefreqx*8)
            float rotyRad = getFloatParameterValue("offsety") + getFloatParameterValue("baseamplitudey")
                    * pny1.noise((float) t * getFloatParameterValue("basefreqy"));
            float rotzRad = getFloatParameterValue("offsetz") + getFloatParameterValue("baseamplitudez")
                    * pnz1.noise((float) t * getFloatParameterValue("basefreqz"));
            Quat4f.setFromRollPitchYaw(q, rotzRad, rotxRad, rotyRad);
            player.getVNextPartBySid(getParameterValue("joint")).setRotation(q);
        }
        catch (Exception ex)
        {
            throw new MUPlayException(ex.getMessage(), this);
        }
    }

    @Override
    public void setFloatParameterValue(String name, float value)
    {
        parameters.put(name, "" + value);
        // System.out.println("param" +name+","+value);
    }

    @Override
    public void setParameterValue(String name, String value)
    {
        parameters.put(name, value);
        // System.out.println("param" +name+","+value);
    }

    @Override
    public String getParameterValue(String name) throws ParameterNotFoundException
    {
        if (parameters.get(name) == null)
        {
            throw new ParameterNotFoundException(name);
        }
        else return parameters.get(name);

    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterNotFoundException
    {
        if (parameters.get(name) == null)
        {
            throw new ParameterNotFoundException(name);
        }
        float value = 0;
        try
        {
            value = Float.parseFloat(parameters.get(name));
        }
        catch (NumberFormatException ex)
        {
            throw new ParameterNotFoundException(name);
        }
        return value;
    }

    @Override
    public TimedAnimationMotionUnit createTMU(FeedbackManager bbm, BMLBlockPeg bbPeg, String bmlId, String id, PegBoard pb)
    {
        return new NoiseTMU(bbm, bbPeg, bmlId, id, this, pb, aniPlayer);
    }

    @Override
    public AnimationUnit copy(AnimationPlayer p)
    {
        this.aniPlayer = p;
        HashMap<String, String> newparam = new HashMap<String, String>();
        newparam.putAll(parameters);
        PerlinNoiseMU pmu = new PerlinNoiseMU();
        pmu.parameters = newparam;
        pmu.player = p;
        return pmu;
    }

    private static final Set<String> PHJOINTS = ImmutableSet.of();

    @Override
    public Set<String> getPhysicalJoints()
    {
        return PHJOINTS;
    }

    @Override
    public Set<String> getKinematicJoints()
    {
        try
        {
            String jointName = getParameterValue("joint");
            return ImmutableSet.of(jointName);
        }
        catch (ParameterNotFoundException e)
        {
            log.warn("No joint set for PerlinNoiseMU, ParameterNotFoundException", e);
        }
        return ImmutableSet.of();

    }
    
    @Override
    public Set<String> getAdditiveJoints()
    {
        return ImmutableSet.of();
    }

    @Override
    public void startUnit(double t) throws MUPlayException
    {
                
    }
}
