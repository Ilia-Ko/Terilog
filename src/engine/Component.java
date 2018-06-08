package engine;

import engine.interfaces.Informative;
import engine.interfaces.Mirrorable;
import engine.interfaces.Renderable;
import engine.interfaces.Rotatable;

public abstract class Component implements Renderable, Rotatable, Mirrorable, Informative {

    protected int x, y;
    protected int rotation;
    protected int mirrorV, mirrorH;

    protected Wire[] inputs, outputs;
    protected int numInp, numOut;

    protected Component() {
        x = 0;
        y = 0;
        rotation = ROT_RIGHT;
        mirrorV = 1;
        mirrorH = 1;
        numInp = 0;
        numOut = 0;
    }

    // simulation
    public abstract void simulate();
    public Wire[] getAffected() {
        return outputs;
    }

    // connecting
    public abstract void connect(Wire unit, int type);
    public abstract void disconnect(Wire unit);

    // render
    @Override
    public void setPos(int xPos, int yPos) {
        x = xPos;
        y = yPos;
    }
    @Override
    public boolean inside(int mx, int my) {
        mx -= x;
        my -= y;
        int w2 = getWidth() / 2;
        int h2 = getHeight() / 2;
        return (mx >= -w2) && (mx <= +w2) && (my >= -h2) && (my <= +h2);
    }

    // rotating
    @Override
    public void rotateClockwise() {
        rotation += 3;
        rotation %= NUM_ROTATIONS;
    }
    @Override
    public void rotateCounterClockwise() {
        rotation += 1;
        rotation %= NUM_ROTATIONS;
    }

    // mirroring
    @Override
    public void mirrorHorizontal() {
        mirrorH *= -1;
    }
    @Override
    public void mirrorVertical() {
        mirrorV *= -1;
    }

    // save/load
    public void saveGeneralInfo() {

    }
    public void loadGeneralInfo() {

    }

}
