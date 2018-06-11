package engine.interfaces;

public interface Mirrorable {

    int NOT_MIRRORED = +1;
    int MIRRORED = -1;

    int DEFAULT = NOT_MIRRORED;

    void mirrorHorizontal();
    void mirrorVertical();

}
