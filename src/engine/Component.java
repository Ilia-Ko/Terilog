package engine;

import engine.interfaces.Informative;
import engine.interfaces.Renderable;

public abstract class Component implements Renderable, Informative {

    public static final int AS_INPUT = 0;
    public static final int AS_OUTPUT = 1;

    private String id;
    protected double x, y;

    protected Wire[] inputs, outputs;
    protected int numInp, numOut;

    public Component(boolean isReal, String uniqueID, double xPos, double yPos) {
        id = uniqueID;
        x = xPos;
        y = yPos;
        numInp = 0;
        numOut = 0;
    }

    // simulation
    public abstract void connect(Wire wire, int type);
    public abstract void simulate();

    // render
    @Override
    public void setPos(double xPos, double yPos) {
        x = xPos;
        y = yPos;
    }

    // save/load
    public void saveGeneralInfo() {

    }
    public void loadGeneralInfo() {

    }

    @Override
    public String toString() {
        return id;
    }

}
