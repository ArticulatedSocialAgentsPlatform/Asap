/*******************************************************************************
 *******************************************************************************/
package asap.hns;

import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;

import com.google.common.primitives.Floats;

/**
 * Parses a symbol inside symbolMatrices
 * @author hvanwelbergen
 *
 */
public class MatrixSymbol extends XMLStructureAdapter
{
    @Getter
    private String className;
    
    @Getter
    private String name;
    
    private float values[];
    public float[] getValues()
    {
        return Arrays.copyOf(values, values.length);
    }
    
    @Override
    public void decodeAttributes(HashMap<String, String> attrMap, XMLTokenizer tokenizer)
    {
        className = getRequiredAttribute("class", attrMap, tokenizer);
        name = getRequiredAttribute("name", attrMap, tokenizer);
        String str = getRequiredAttribute("value", attrMap, tokenizer);
        String strValues[] = str.split("\\s+");         
        List<Float>valList = new ArrayList<>();
        for(String val:strValues)
        {
            valList.add(Float.parseFloat(val));
        }
        values = Floats.toArray(valList);
    }
    
    static final String XMLTAG = "symbol";

    public static String xmlTag()
    {
        return XMLTAG;
    }

    @Override
    public String getXMLTag()
    {
        return XMLTAG;
    }
}
