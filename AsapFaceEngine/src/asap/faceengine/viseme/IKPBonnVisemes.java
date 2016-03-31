/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.viseme;

/**
 * The list of Visemes from [1], developed for the German BOSS phoneme set. [1]
 * Aschenberner and Weiss, Phoneme-Viseme Mapping for German VIdeo-realistic
 * Audio-Visual Speech Synthesis, IKP Working paper NF 11
 * @author reidsma
 */
public final class IKPBonnVisemes
{
    private IKPBonnVisemes(){}
    /*
     * ===================================== VISEME NUMBERS
     * =====================================
     */
    public static final int V_NULL = -1; // null viseme -- no info
    public static final int V_SIL = 0; // Viseme for silence.
    public static final int V_P = 1; // p, b
    public static final int V_T = 2; // t, d, k, g
    public static final int V_N = 3; // n, @n, l, @l
    public static final int V_M = 4; // m
    public static final int V_F = 5; // f, v
    public static final int V_S = 6; // s, z
    public static final int V_Z = 7; // S, Z, tS, dZ
    public static final int V_R = 8; // h, r, x, N
    public static final int V_C = 9; // j, C
    public static final int V_E = 10; // i:, I, e:, E:, E
    public static final int V_A = 11; // a:, a
    public static final int V_O = 12; // o:, O
    public static final int V_U = 13; // u:, U
    public static final int V_Q = 14; // @, 6
    public static final int V_Y = 15; // y:, Y, 2:, 9
    public static final int MAX_VISEME = V_Y;
    /*
     * ===================================== VISEME DESCRIPTIONS
     * =====================================
     */
    private static String[] visemeDescriptions = new String[] {
            "Viseme for silence.", "p, b", "t, d, k, g", "n, @n, l, @l", "m",
            "f, v", "s, z", "S, Z, tS, dZ", "h, r, x, N", "j, C",
            "i:, I, e:, E:, E", "a:, a", "o:, O", "u:, U", "@, 6",
            "y:, Y, 2:, 9" };

    public static String getVisemeInfo(int viseme)
    {
        if (viseme == -1)
            return "null viseme -- no info";
        if (viseme < -1 || viseme > MAX_VISEME)
            return "error: no such viseme";
        return visemeDescriptions[viseme];
    }

}
