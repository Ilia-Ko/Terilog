package engine.transistors;

import engine.LogicLevel;
import engine.interfaces.Rotatable;
import javafx.scene.canvas.GraphicsContext;

public class HardN extends MOSFET {

    private static final String STR_ID_PREFIX = "HardN";
    private static final int VGSTH = 2;
    private static int numOfHardNs = 0;

    public HardN(boolean isReal) {
        super(isReal, isReal ? genUniqueID() : "null", 2.0, 1.0, Rotatable.ROT_RIGHT, false, false, VGSTH);
    }

    @Override
    void finishRendering(GraphicsContext gc) {
        gc.fillPolygon(new double[] {0.0, -0.2, +0.2},
                       new double[] {0.0, -0.2, -0.2}, 3);
    }

    @Override
    public void simulate() {
        LogicLevel source = inputs[SOURCE].getSignal();
        LogicLevel gate = inputs[GATE].getSignal();
        if (gate.volts() - source.volts() == vgsth) outputs[DRAIN].setFutureSignal(source);
        else outputs[DRAIN].setFutureSignal(LogicLevel.ZZZ);
    }

    private static String genUniqueID() {
        return String.format("%s-%d", STR_ID_PREFIX, numOfHardNs++);
    }

}
