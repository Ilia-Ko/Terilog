package engine;

import engine.interfaces.Informative;
import engine.interfaces.Renderable;
import javafx.scene.canvas.GraphicsContext;

public class Wire implements Renderable, Informative {

    private static final String STR_ID_PREFIX = "HardN";
    private static int numOfWires = 0;

    private String id;
    private double x, y;
    private double[] xArr, yArr;
    private int num;
    private double width, height;
    private LogicLevel signal, futureSignal;

    public Wire(double[] xVertices, double[] yVertices, int numVertices) {
        id = genUniqueID();
        xArr = xVertices;
        yArr = yVertices;
        num = numVertices;
        assert xArr.length == num && yArr.length == num;
    }

    public LogicLevel getSignal() {
        return signal;
    }
    public void setFutureSignal(LogicLevel signal) {
        futureSignal = signal;
    }
    public void propagateSignal() {
        signal = futureSignal;
    }

    // rendering
    @Override
    public void render(GraphicsContext gc) {

    }
    @Override
    public void setPos(double xPos, double yPos) {
        x = xPos;
        y = yPos;
    }
    @Override
    public double getWidth() {
        return width;
    }
    @Override
    public double getHeight() {
        return height;
    }

    // save/load
    @Override
    public void saveSpecificInfo() {

    }
    @Override
    public void loadSpecificInfo() {

    }

    private void updateWidthHeight() {

    }

    private static String genUniqueID() {
        return String.format("%s-%d", STR_ID_PREFIX, numOfWires++);
    }

}
