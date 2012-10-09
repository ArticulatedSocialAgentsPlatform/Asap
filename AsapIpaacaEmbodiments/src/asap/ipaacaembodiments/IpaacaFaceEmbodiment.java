package asap.ipaacaembodiments;

import hmi.faceembodiments.FaceEmbodiment;

/**
 * FaceEmbodiment that makes use of an IpaacaFaceController
 * @author hvanwelbergen
 *
 */
public class IpaacaFaceEmbodiment implements FaceEmbodiment
{
    private final IpaacaFaceController fc;
    private String id;
    
    public IpaacaFaceEmbodiment(IpaacaFaceController fc)
    {
        this.fc = fc;
    }
    
    @Override
    public void copy()
    {
        fc.copy();        
    }
    
    public void setId(String id)
    {
        this.id = id;
    }

    @Override
    public String getId()
    {
        return id;
    }   

    @Override
    public IpaacaFaceController getFaceController()
    {
        return fc;
    }

}
