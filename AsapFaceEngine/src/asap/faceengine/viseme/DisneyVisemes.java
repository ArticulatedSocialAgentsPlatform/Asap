/*******************************************************************************
 *******************************************************************************/
package asap.faceengine.viseme;

/**
 * The list of Disney Visemes, used by the SAPI5 TTS engines for lipsynch info
 * @author reidsma
 **/
public final class DisneyVisemes
{
    private DisneyVisemes(){}
    /*
     * =====================================
     * VISEME NUMBERS
     * =====================================
     */
    public static final int V_NULL = -1; // null viseme -- no info
    public static final int V_SIL = 0; // Viseme for silence.
    public static final int V_AE_AX_AH = 1; // ae, ax, ah
    public static final int V_AA = 2; // aa
    public static final int V_AO = 3; // ao
    public static final int V_EY_EH_UH = 4; // ey, eh, uh
    public static final int V_ER = 5; // er
    public static final int V_Y_IY_IH_IX = 6; // y, iy, ih, ix
    public static final int V_W_UW = 7; // w, uw
    public static final int V_OW = 8; // ow
    public static final int V_AW = 9; // aw
    public static final int V_OY = 10; // oy
    public static final int V_AY = 11; // ay
    public static final int V_H = 12; // h
    public static final int V_R = 13; // r
    public static final int V_L = 14; // l
    public static final int V_S_Z = 15; // s, z
    public static final int V_SH_CH_JH_ZH = 16; // sh, ch, jh, zh
    public static final int V_TH_DH = 17; // th, dh
    public static final int V_F_V = 18; // f,v
    public static final int V_D_T_N = 19; // d, t, n
    public static final int V_K_G_NG = 20; // k, g, ng
    public static final int V_P_B_M = 21; // p, b, m
    public static final int MAX_VISEME = V_P_B_M;
    /*
     * =====================================
     * VISEME DESCRIPTIONS
     * =====================================
     */
    private static String[] visemeDescriptions = new String[] { "Viseme for silence.", "ae, ax, ah", "aa", "ao", "ey, eh, uh", "er",
            "y, iy, ih, ix", "w, uw", "ow", "aw", "oy", "ay", "h", "r", "l", "s, z", "sh, ch, jh, zh", "th, dh", "f,v", "d, t, n",
            "k, g, ng", "p, b, m" };

    public static String getVisemeInfo(int viseme)
    {
        if (viseme == -1) return "null viseme -- no info";
        if (viseme < -1 || viseme > MAX_VISEME) return "error: no such viseme";
        return visemeDescriptions[viseme];
    }

}
