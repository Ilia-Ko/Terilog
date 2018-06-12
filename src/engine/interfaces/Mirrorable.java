package engine.interfaces;

public interface Mirrorable {

    int NOT_MIRRORED = +1;
    int MIRRORED = -1;

    int DEFAULT = NOT_MIRRORED;

    void mirrorHorizontal();
    void mirrorVertical();

    static String getAttrName(int mirror) {
        if (mirror == MIRRORED)
            return "yes";
        else
            return "no";
    }
    static int parseAttrName(String name) {
        if (name.equals("yes"))
            return MIRRORED;
        else
            return DEFAULT;
    }

}
