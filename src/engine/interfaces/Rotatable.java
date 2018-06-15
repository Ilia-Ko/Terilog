package engine.interfaces;

public interface Rotatable {

    int NUM_ROTATIONS = 4;
    double ROTATION_ANGLE = 360.0 / (double) NUM_ROTATIONS;

    // the order of directions corresponds with the trigonometric circle
    int ROT_RIGHT = 0;
    int ROT_UP = 1;
    int ROT_LEFT = 2;
    int ROT_DOWN = 3;

    int DEFAULT = ROT_RIGHT; // default: no rotation

    void rotateCW();
    void rotateCCW();

    static String getAttrName(int rotation) {
        switch (rotation) {
            case DEFAULT:
                return "none";
            case ROT_UP:
                return "up";
            case ROT_LEFT:
                return "left";
            default:
                return "down";
        }
    }
    static int parseAttrName(String name) {
        switch (name) {
            case "down":
                return ROT_DOWN;
            case "up":
                return ROT_UP;
            case "left":
                return ROT_LEFT;
            default:
                return DEFAULT;
        }
    }

}
