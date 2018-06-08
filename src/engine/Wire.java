package engine;

import engine.interfaces.Renderable;
import gui.Control;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class Wire implements Renderable {

    // rendering
    private static final double CONNECTION_OPACITY = 1.0 / 3.0;
    private static final double CONNECTION_SIZE = 3.0;
    private static final int TOLERANCE = 2; // for inside checking

    private int x, y, x1, y1; // x1, y1, x2 and y2 are relative to absolute (x, y)
    private boolean bendsOutside;
    private LogicLevel signal, futureSignal;
    private ArrayList<Wire> connected;

    public Wire() {
        x = 0;
        y = 0;
        x1 = 0;
        y1 = 0;
        bendsOutside = true;
        connected = new ArrayList<>();
    }

    // simulating
    public LogicLevel getSignal() {
        return signal;
    }
    public void setFutureSignal(LogicLevel signal) {
        futureSignal = signal;
    }
    public void propagateSignal() {
        signal = futureSignal;
        for (Wire wire : connected) wire.setFutureSignal(signal);
    }
    public Wire[] getAffectedWires() {
        return (Wire[]) connected.toArray();
    }
    public boolean isStable() {
        return signal == futureSignal;
    }

    // connecting
    public void connectWire(Wire wire) {
        connected.add(wire);
        wire.connectWire(this);
    }
    public void disconnectWire(Wire wire) {
        connected.remove(wire);
        wire.disconnectWire(this);
    }

    // rendering
    public void update(int mx, int my) {
        x1 = mx - x;
        y1 = my - y;
    }
    public void flipBending() {
        bendsOutside = !bendsOutside;
    }
    @Override
    public void render(GraphicsContext gc) {
        double lw = 4;
        // transform
        gc.translate(x, y);
        gc.setFill(signal.colour());
        gc.setLineWidth(lw);

        // draw the wire
        if (bendsOutside) {
            gc.strokeLine(0, 0, x1, 0);
            gc.strokeLine(x1, 0, x1, y1);
        } else {
            gc.strokeLine(0, 0, 0, y1);
            gc.strokeLine(0, y1, x1, y1);
        }

        // draw connections
        gc.setGlobalAlpha(gc.getGlobalAlpha() * CONNECTION_OPACITY);
        gc.fillOval(0, 0, lw * CONNECTION_SIZE, lw * CONNECTION_SIZE);
        gc.fillOval(x1, y1, lw * CONNECTION_SIZE, lw * CONNECTION_SIZE);
        gc.setGlobalAlpha(gc.getGlobalAlpha() / CONNECTION_OPACITY);

        // undo transform
        gc.translate(-x, -y);
    }
    @Override
    public void setPos(int xPos, int yPos) {
        x = xPos;
        y = yPos;
    }
    @Override
    public boolean inside(int mx, int my) {
        mx -= x;
        my -= y;
        boolean inside;
        boolean A = (mx >= -TOLERANCE);
        boolean B = (my >= -TOLERANCE);
        boolean C = (my <= +TOLERANCE);
        boolean D = (mx <= +TOLERANCE);
        boolean E = (mx <= x1 + TOLERANCE);
        boolean F = (my <= y1 + TOLERANCE);
        boolean G = (mx >= x1 - TOLERANCE);
        boolean H = (my >= y1 - TOLERANCE);
        if (bendsOutside)
            inside = B && E && (A && C || F && G);
        else
            inside = A && F && (B && D || E && H);
        return inside;
    }

    @Override
    public int getWidth() {
        return Math.abs(x1);
    }
    @Override
    public int getHeight() {
        return Math.abs(y1);
    }

    // save/load
    @Override
    public void saveSpecificInfo() {

    }
    @Override
    public void loadSpecificInfo() {

    }

}
