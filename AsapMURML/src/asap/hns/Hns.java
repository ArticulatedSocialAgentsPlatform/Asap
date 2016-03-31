/*******************************************************************************
 *******************************************************************************/
package asap.hns;

import hmi.math.Mat4f;
import hmi.math.Vec3f;
import hmi.util.StringUtil;
import hmi.xml.XMLScanException;
import hmi.xml.XMLStructureAdapter;
import hmi.xml.XMLTokenizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import asap.murml.Symmetry;

/**
 * HNS file parser
 * @author hvanwelbergen
 */
@Slf4j
public class Hns extends XMLStructureAdapter
{
    private Map<String, String> settings = new HashMap<>(); // name->value map
    private Map<String, Map<String, Double>> symbols = new HashMap<>(); // className->(name->value)
    
    @Getter
    private String baseJoint = "HumanoidRoot";
    
    @Getter
	private String defaultPointingHandshape;
    
    private static final String XMLTAG = "hns";
    private static final String HAND_REFERENCES = "handReferences";
    private static final String HAND_LOCATORS = "handLocators";
    private static final String HAND_DISTANCES = "handDistances";
    private static final String PALM_ORIENTATIONS = "palmOrientations";
    private static final String DISTANCES = "distances";
    private static final String HANDSHAPES = "handShapes";
    private static final String BASIC_HANDSHAPES = "basicHandShapes";
    private static final String SPECIFIC_HANDSHAPES = "specificHandShapes";

    private static final float[] UP_VEC = Vec3f.getVec3f(0, 1, 0);
    private static final float[] DOWN_VEC = Vec3f.getVec3f(0, -1, 0);
    private static final float[] LEFT_VEC = Vec3f.getVec3f(1, 0, 0);
    private static final float[] RIGHT_VEC = Vec3f.getVec3f(-1, 0, 0);
    private static final float[] A_VEC = Vec3f.getVec3f(0, 0, 1);
    private static final float[] T_VEC = Vec3f.getVec3f(0, 0, -1);

    enum ExtendSymbols
    {
        Flat, Normal, Large
    }

    enum RoundnessSymbols
    {
        Sharp, Normal, Broad
    }

    enum SkewdnessSymbols
    {
        Round, FunnelS, FunnelE
    }

    public static double getMinSwivelLeft()
    {
        return -2.5;
    }
    
    public static double getMaxSwivelLeft()
    {
        return 1;
    }
    
    public static double getMinSwivelRight()
    {
        return -1;
    }
    
    public static double getMaxSwivelRight()
    {
        return 2.5;
    }
    
    public static double getSwivelSigmaOfGaussianCostsDistribution()
    {
        return 0.4;
    }
    
    public static double getSwivelFreedomOfTheGaussianMeanLeft()
    {
        return 0;
    }
    
    public static double getSwivelFreedomOfTheGaussianMeanRight()
    {
        return 0;
    }    
     
    
    public Set<String> getBasicHandShapes()
    {
        return symbols.get(BASIC_HANDSHAPES).keySet();
    }

    public Set<String> getSpecificHandShapes()
    {
        return symbols.get(SPECIFIC_HANDSHAPES).keySet();
    }

    public Set<String> getHandShapes()
    {
        return symbols.get(HANDSHAPES).keySet();
    }

    /**
     * @param value a 3-vector, split by whitespace
     * @param vec return parameter
     * @return false if syntax error
     */
    public boolean parseVector(String value, float[] vec)
    {
        String values[] = value.split("\\s+");
        if (values.length != 3) return false;
        for(String val:values)
        {
            if(!StringUtil.isNumeric(val))return false;
        }
        Vec3f.set(vec, Float.parseFloat(values[0]), Float.parseFloat(values[1]), Float.parseFloat(values[2]));
        return true;
    }

    /**
     * Get the absolute direction of a HNS direction symbol (starting with 'Dir')
     * @param value the HNS symbol
     * @param direction output: the direction
     * @return false if a syntax error occured
     */
    public boolean getAbsoluteDirection(String value, float[] direction)
    {
        if (!value.startsWith("Dir"))
        {
            float result[] = Vec3f.getVec3f();
            if (parseVector(value, result))
            {
                Vec3f.normalize(result);
                Vec3f.set(direction, result);
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            String dirSuffix = value.substring("Dir".length());
            Vec3f.set(direction, 0, 0, 0);
            for (char c : dirSuffix.toCharArray())
            {
                switch (c)
                {
                case 'A':
                    Vec3f.add(direction, A_VEC);
                    break;
                case 'T':
                    Vec3f.add(direction, T_VEC);
                    break;
                case 'L':
                    Vec3f.add(direction, LEFT_VEC);
                    break;
                case 'R':
                    Vec3f.add(direction, RIGHT_VEC);
                    break;
                case 'U':
                    Vec3f.add(direction, UP_VEC);
                    break;
                case 'D':
                    Vec3f.add(direction, DOWN_VEC);
                    break;
                default:
                    return false;
                }
            }
        }

        if (Vec3f.length(direction) > 0)
        {
            Vec3f.normalize(direction);
            return true;
        }
        return false;
    }

    public void transFormLocation(float loc[], Symmetry sym)
    {
        float mLoc[]=Mat4f.getMat4f();
        float mDir[]=Mat4f.getMat4f();
        getSymmetryTransform(sym, mLoc, mDir);
        Mat4f.transformPoint(mLoc, loc);
    }
    
    public void transFormDirection(float dir[], Symmetry sym)
    {
        float mLoc[]=Mat4f.getMat4f();
        float mDir[]=Mat4f.getMat4f();
        getSymmetryTransform(sym, mLoc, mDir);
        Mat4f.transformPoint(mDir, dir);
    }
    
    public void getSymmetryTransform(Symmetry  sym, float mLoc[], float mDir[])
    {
        switch (sym)
        {
        case Sym:
            Mat4f.setIdentity(mLoc);
            Mat4f.setIdentity(mDir);
            break;
        case SymMS:
            // @formatter:off
            Mat4f.set(mLoc,-1,0,0,0,
                            0,1,0,0,
                            0,0,1,0,
                            0,0,0,1
                    );
            Mat4f.set(mDir, mLoc);
            // @formatter:on
            break;
        case SymMT:
            // @formatter:off
            Mat4f.set(mDir, 1,0,0,0,
                            0,-1,0,0,
                            0,0,1,0,
                            0,0,0,1
                    );
            Mat4f.set(mLoc,-1,0,0,0,
                            0,-1,0,0,
                            0,0,1,0,
                            0,0,0,1
            );
            // @formatter:on
            break;
        case SymMF:
            // @formatter:off
            Mat4f.set(mDir, 1,0,0,0,
                            0,1,0,0,
                            0,0,-1,0,
                            0,0,0,1
                    );
            Mat4f.set(mLoc,-1,0,0,0,
                            0,1,0,0,
                            0,0,-1,0,
                            0,0,0,1
            );
            // @formatter:on
            break;
        case SymMST:
         // @formatter:off
            Mat4f.set(mDir, -1,0,0,0,
                            0,-1,0,0,
                            0,0,1,0,
                            0,0,0,1
                    );
            Mat4f.set(mLoc,-1,0,0,0,
                            0,-1,0,0,
                            0,0,1,0,
                            0,0,0,1
            );
            // @formatter:on
            break;
        case SymMSF:
            // @formatter:off
            Mat4f.set(mDir, -1,0,0,0,
                            0,1,0,0,
                            0,0,-1,0,
                            0,0,0,1
                    );
            Mat4f.set(mLoc,-1,0,0,0,
                            0,1,0,0,
                            0,0,-1,0,
                            0,0,0,1
            );
            // @formatter:on
            break;
        case SymMTF:
            // @formatter:off
            Mat4f.set(mDir, 1,0,0,0,
                            0,-1,0,0,
                            0,0,-1,0,
                            0,0,0,1
                    );
            Mat4f.set(mLoc,-1,0,0,0,
                            0,-1,0,0,
                            0,0,-1,0,
                            0,0,0,1
            );
            // @formatter:on
            break;
        case SymMSTF:
         // @formatter:off
            Mat4f.set(mDir, -1,0,0,0,
                            0,-1,0,0,
                            0,0,-1,0,
                            0,0,0,1
                    );
            Mat4f.set(mLoc,-1,0,0,0,
                            0,-1,0,0,
                            0,0,-1,0,
                            0,0,0,1
            );
            // @formatter:on
            break;
        }
    }

    /**
     * Translate hand location symbol in figure root coords
     * @return false, if a syntax error occurred, true otherwise.
     */
    public boolean getHandLocation(String value, float[] location)
    {
        String vals[] = value.split("\\s+");
        String reference = vals[0];
        String locator = null;

        // vector description
        if (getSymbolValue(HAND_REFERENCES, reference) == null)
        {
            return parseVector(value, location);
        }

        if (vals.length >= 2)
        {
            locator = vals[1];
        }
        else
        {
            return false;
        }

        // string description
        boolean distanceRead = false;
        String distance = null;
        if (getSymbolValue(HAND_LOCATORS, locator) == null)
        {
            if (getSymbolValue(HAND_DISTANCES, locator) != null)
            {
                distanceRead = true;
                distance = locator;
            }
            else
            {
                locator = "LocCCenter";
            }
        }

        if (!distanceRead && vals.length >= 2)
        {
            distance = vals[2];
            if (getSymbolValue(HAND_DISTANCES, distance) == null)
            {
                log.warn("invalid distance symbol {}, assuming LocNorm", distance);
                distance = "LocNorm";
            }
        }
        else
        {
            return false;
        }

        double phi = 0;
        double r = 0;

        // location referent defines z-coordinate of xy-plane and
        // optionally origin of cylindrical coords within this plane
        if (getSymbolValue(HAND_REFERENCES, reference) != null)
        {
            location[1] = getSymbolValue(HAND_REFERENCES, reference).floatValue();
        }
        if (getSymbolValue(HAND_LOCATORS, locator) != null)
        {
            phi = getSymbolValue(HAND_LOCATORS, locator);
        }
        if (getSymbolValue(HAND_DISTANCES, distance) != null)
        {
            r = getEllipticDistance(phi, getSymbolValue(HAND_DISTANCES, distance));
        }
        location[2] = (float) (r * Math.cos(Math.toRadians(phi)));
        location[0] = (float) (r * Math.sin(Math.toRadians(phi)));
        return true;
    }

    /**
     * @return distance, or -1 on failure
     */
    public double getDistance(String value)
    {
        if (StringUtil.isNumeric(value))
        {
            return Double.parseDouble(value);
        }
        Double distStr = getSymbolValue(DISTANCES, value);
        if (distStr != null)
        {
            return distStr;
        }
        else
        {
            return -1;
        }
    }

    public double getElementExtent(String value)
    {
        try
        {
            ExtendSymbols es = ExtendSymbols.valueOf(value);
            switch (es)
            {
            case Flat:
                return 0.25;
            case Normal:
                return 0.5;
            case Large:
                return 0.75;
            }
        }
        catch (IllegalArgumentException ex)
        {
            if (StringUtil.isNumeric(value))
            {
                return Double.parseDouble(value);
            }
            else
            {
                throw new RuntimeException("Parsing failure for extend " + value);
            }
        }
        return 0.2;
    }

    public ShapeSymbols getElementShape(String value)
    {
        return ShapeSymbols.valueOf(value);
    }

    public double getElementRoundness(String value)
    {
        try
        {
            RoundnessSymbols rs = RoundnessSymbols.valueOf(value);
            {
                switch (rs)
                {
                case Sharp:
                    return 0.9;
                case Normal:
                    return 0;
                case Broad:
                    return -0.9;
                }
            }
        }
        catch (IllegalArgumentException ex)
        {
            if (StringUtil.isNumeric(value))
            {
                return Double.parseDouble(value);
            }
            else
            {
                throw new RuntimeException("Parsing failure for roundness " + value);
            }
        }
        return 0;
    }

    public double getElementSkewedness(String value)
    {
        try
        {
            SkewdnessSymbols ss = SkewdnessSymbols.valueOf(value);
            switch (ss)
            {
            case Round:
                return 0;
            case FunnelS:
                return -0.8;
            case FunnelE:
                return 0.8;
            }
        }
        catch (IllegalArgumentException ex)
        {
            if (StringUtil.isNumeric(value))
            {
                return Double.parseDouble(value);
            }
            else
            {
                throw new RuntimeException("Parsing failure for skewdness " + value);
            }
        }
        return 0;
    }

    public boolean isPalmOrientation(String value)
    {
        if (getSymbolValue(PALM_ORIENTATIONS, value) == null)
        {
            return StringUtil.isNumeric(value);
        }
        else
        {
            return getSymbolValue(PALM_ORIENTATIONS, value) != null;
        }
    }

    /**
     * Gets the palm orientation for value, in degrees
     * @param value value, e.g. PalmU or a double
     */
    public double getPalmOrientation(String value, String scope)
    {
        if (getSymbolValue(PALM_ORIENTATIONS, value) == null)
        {
            if (StringUtil.isNumeric(value))
            {
                return Double.parseDouble(value);
            }
            else
            {
                throw new RuntimeException("Parsing failure for palmOrientation " + value);
            }
        }
        else
        {
            double angle = getSymbolValue(PALM_ORIENTATIONS, value);
            if (scope.equals("right_arm"))
            {
                if (angle <= 0)
                {
                    angle += 180.0;
                }
                else
                {
                    angle -= 180.0;
                }
            }
            return angle;
        }
    }

    public Double getSymbolValue(String className, String name)
    {
        Map<String, Double> map = symbols.get(className);
        if (map == null) return null;
        return map.get(name);
    }

    private double getEllipticDistance(double phi, double r)
    {
        // Variablen fuer den Koerper von Max. Gleichbedeutend mit Achsenhaelfte a bei Ellipse
        // Offset von 140, da a = 220 (Haelfte von der Breite cia 500), b = 80 => 170 ist Breite vom Nullpunkt
        double a = r + getSymbolValue("offset", "ellipticDistance");
        double dist;
        double epsilon = (Math.sqrt(a * a - r * r)) / a;
        if (epsilon < 1)
        // Berechnung des Vektors r, der neuen Distanz
        dist = r / Math.sqrt(1 - epsilon * Math.cos(Math.toRadians(phi - 90)) * Math.cos(Math.toRadians(phi - 90)));
        else
        // Falls >= 1 nehme kleinen Wert
        dist = r / Math.sqrt(1 - 0.999999 * Math.cos(Math.toRadians(phi - 90)) * Math.cos(Math.toRadians(phi - 90)));
        return dist;
    }

    @Override
    public void decodeContent(XMLTokenizer tokenizer) throws IOException
    {
        while (tokenizer.atSTag())
        {
            switch (tokenizer.getTagName())
            {            
            case BaseJoint.XMLTAG:
                BaseJoint bj = new BaseJoint();
                bj.readXML(tokenizer);
                baseJoint = bj.getSid();
                break;
            case DefaultPointingHandshape.XMLTAG:
            	DefaultPointingHandshape hs = new DefaultPointingHandshape();
            	hs.readXML(tokenizer);
            	defaultPointingHandshape = hs.getName();
            	break;
            case Settings.XMLTAG:
                Settings set = new Settings();
                set.readXML(tokenizer);
                settings.putAll(set.getSettings());
                break;
            case Symbols.XMLTAG:
                Symbols sym = new Symbols();
                sym.readXML(tokenizer);
                for (Entry<String, Map<String, Double>> entry : sym.getSymbols().entrySet())
                {
                    Map<String, Double> map = symbols.get(entry.getKey());
                    if (map == null)
                    {
                        map = new HashMap<>();
                        symbols.put(entry.getKey(), map);
                    }
                    for (Entry<String, Double> entry2 : entry.getValue().entrySet())
                    {
                        map.put(entry2.getKey(), entry2.getValue());
                    }
                }
                break;
            default:
                throw new XMLScanException("Invalid tag " + tokenizer.getTagName() + " in <hns>");
            }
        }
    }

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
