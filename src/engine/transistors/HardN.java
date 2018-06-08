package engine.transistors;

import engine.LogicLevel;
import javafx.scene.canvas.GraphicsContext;

public class HardN extends MOSFET {

    private static final int VGSTH = 2;

    public HardN() {
        super(VGSTH);
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

}
