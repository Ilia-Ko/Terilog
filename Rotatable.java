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

    void rotateClockwise();
    void rotateCounterClockwise();

    static String getName(int rotation) {
        if (rotation == DEFAULT)
            return "none";
        else if (rotation == ROT_UP)
            return "up";
        else if (rotation == ROT_LEFT)
            return "left";
        else
            return "down";
    }
    static int parseName(String name) {
        if (name.equals("none") || name.equals("right"))
            return ROT_RIGHT;
        else if (name.equals("up"))
            return ROT_UP;
        else if (name.equals("left"))
            return ROT_LEFT;
        else
            return ROT_DOWN;
    }

}
