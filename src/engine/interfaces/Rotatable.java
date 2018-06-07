package engine.interfaces;

public interface Rotatable {

    int NUM_ROTATIONS = 4;
    double ROTATION_ANGLE = 360.0 / (double) NUM_ROTATIONS;

    int ROT_RIGHT = 0;
    int ROT_UP = 1;
    int ROT_LEFT = 2;
    int ROT_DOWN = 3;

    void rotateClockwise();
    void rotateCounterClockwise();

}
