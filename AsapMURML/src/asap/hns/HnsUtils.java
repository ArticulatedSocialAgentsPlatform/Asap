package asap.hns;

import hmi.math.Vec3f;

/**
 * HamNoSys utilities
 * @author hvanwelbergen
 */
public final class HnsUtils
{
    private static final float[] UP_VEC = Vec3f.getVec3f(0, 1, 0);
    private static final float[] DOWN_VEC = Vec3f.getVec3f(0, -1, 0);
    private static final float[] LEFT_VEC = Vec3f.getVec3f(1, 0, 0);
    private static final float[] RIGHT_VEC = Vec3f.getVec3f(1, 0, 0);
    private static final float[] A_VEC = Vec3f.getVec3f(0, 0, 1);
    private static final float[] T_VEC = Vec3f.getVec3f(0, 0, -1);

    private HnsUtils()
    {
    }

    /**
     * @param value a 3-vector, split by whitespace
     * @param vec return parameter
     * @return false if syntax error
     */
    public static boolean parseVector(String value, float[] vec)
    {
        
        String values[]=value.split("\\s+");
        if(values.length!=3)return false;
        Vec3f.set(vec, Float.parseFloat(values[0]),Float.parseFloat(values[1]),Float.parseFloat(values[2]));
        return true;
    }
    
    /**
     * Get the absolute direction of a HNS direction symbol (starting with 'Dir')
     * @param value the HNS symbol
     * @param direction output: the direction
     * @return false if a syntax error occured
     */
    public static boolean getAbsoluteDirection(String value, float[] direction)
    {
        if (!value.startsWith("Dir"))
        {
            float result[] = Vec3f.getVec3f();
            if(parseVector(value,result))
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
    
    
    
}
