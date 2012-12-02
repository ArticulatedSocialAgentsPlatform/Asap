package asap.nao.planunit;

import asap.nao.Nao;
import asap.realizer.feedback.FeedbackManager;
import asap.realizer.pegboard.BMLBlockPeg;
import asap.realizer.planunit.KeyPosition;
import asap.realizer.planunit.KeyPositionManager;
import asap.realizer.planunit.KeyPositionManagerImpl;
import asap.realizer.planunit.ParameterException;
import asap.realizer.planunit.TimedPlanUnitPlayException;

import java.util.List;

public class NaoSayNU implements NaoUnit
{

    private final KeyPositionManager keyPositionManager = new KeyPositionManagerImpl();

    private Nao nao;

    private String text;

    public NaoSayNU()
    {
        KeyPosition start = new KeyPosition("start", 0d, 1d);
        KeyPosition end = new KeyPosition("end", 1d, 1d);
        addKeyPosition(start);
        addKeyPosition(end);
    }

    public void setNao(Nao nao)
    {
        this.nao = nao;
    }

    @Override
    public void setFloatParameterValue(String name, float value) throws ParameterException
    {

    }

    @SuppressWarnings("static-access")
    @Override
    public void setParameterValue(String name, String value) throws ParameterException
    {
        if (name.equals("text"))
        {
            text = text.valueOf(value);
        }
    }

    @Override
    public String getParameterValue(String name) throws ParameterException
    {
        if (name.equals("text"))
        {
            return text.toString();
        }
        return "";
    }

    @Override
    public float getFloatParameterValue(String name) throws ParameterException
    {
        return 0;
    }

    @Override
    public boolean hasValidParameters()
    {
        if (text == null) return false;
        return true;
    }

    /**
     * Start the unit. this will go automatic since there is nothing required.
     */
    public void startUnit(double time) throws TimedPlanUnitPlayException
    {

    }

    /**
     * Test play to see if its working
     */
    public void play(double t) throws NUPlayException
    {
        nao.Say(text);
    }

    public void cleanup()
    {
        try
        {
            play(1d);
        }
        catch (NUPlayException ex)
        {
            // logger.warn("Error cleaning up NaoUnit",ex);
        }
    }

    /**
     * Creates the TimedNaoUnit corresponding to this face unit
     * 
     * @param bmlId
     *            BML block id
     * @param id
     *            behaviour id
     * 
     * @return the TNU
     */
    @Override
    public TimedNaoUnit createTNU(FeedbackManager bfm, BMLBlockPeg bbPeg, String bmlId, String id)
    {
        return new TimedNaoUnit(bfm, bbPeg, bmlId, id, this);
    }

    @Override
    public String getReplacementGroup()
    {
        return "getReplacementGroup";
    }

    /**
     * @return Prefered duration (in seconds) of this face unit, 0 means not determined/infinite
     */
    public double getPreferedDuration()
    {
        return 1d;
    }

    /**
     * Create a copy of this nao unit and link it to the Nao
     */
    public NaoUnit copy(Nao nao)
    {
        NaoSayNU result = new NaoSayNU();
        result.setNao(nao);
        result.text = text;
        for (KeyPosition keypos : getKeyPositions())
        {
            result.addKeyPosition(keypos.deepCopy());
        }
        return result;
    }

    @Override
    public void addKeyPosition(KeyPosition kp)
    {
        keyPositionManager.addKeyPosition(kp);
    }

    @Override
    public KeyPosition getKeyPosition(String name)
    {
        return keyPositionManager.getKeyPosition(name);
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

}
