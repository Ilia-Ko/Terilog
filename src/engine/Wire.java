package engine;

import engine.interfaces.Informative;
import engine.interfaces.Renderable;
import gui.control.ControlMain;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Wire implements Renderable, Informative {

    // rendering
    private Canvas basis;
    private double p;
    private int x, y, dx, dy; // absolute values
    private boolean horizontalFirst; // layout form: true if horizontal line begins at (0, 0)
    private Node node; // the node, that contains this wire

    private String id;

    public Wire() {
        basis = new Canvas();
        x = 0;
        y = 0;
        dx = 0;
        dy = 0;
        horizontalFirst = true;
        node = null;
    }

    // connectivity
    int[][] getPoints() {
        int[][] points = new int[3][];
        points[0] = new int[] {x, y};
        points[1] = new int[] {dx, dy};
        if (horizontalFirst)
            points[2] = new int[] {dx, y};
        else
            points[2] = new int[] {dy, x};
        return points;
    }
    Node getNode() {
        return node;
    }
    void connect(Node node) {
        this.node = node;
        node.addWire(this);
    }

    // laying out by the mouse
    public void layoutAgain(int mx, int my) {
        dx = mx - x;
        dy = my - y;
    }
    public void flip() {
        horizontalFirst = !horizontalFirst;
    }

    // rendering
    @Override public void render() {
        // prepare gc
        GraphicsContext gc = basis.getGraphicsContext2D();
        gc.save();
        if (dx < 0) gc.translate(-dx, 0);
        if (dy < 0) gc.translate(0, -dy);
        if (node != null)
            gc.setStroke(node.getCurrentSignal().colour());
        else
            gc.setStroke(Color.GRAY);
        gc.setLineWidth(ControlMain.LINE_WIDTH * p);

        // draw the wire
        if (horizontalFirst) {
            gc.strokeLine(0, 0, dx, 0);
            gc.strokeLine(dx, 0, dx, dy);
        } else {
            gc.strokeLine(0, 0, 0, dy);
            gc.strokeLine(0, dy, dx, dy);
        }

        gc.restore();
    }
    @Override public void setGridPeriod(double period) {
        p = period;
        basis.setWidth(p * getWidth());
        basis.setHeight(p * getHeight());
        basis.setTranslateX(p * x);
        basis.setTranslateY(p * y);
        // rescale gc
        GraphicsContext gc = basis.getGraphicsContext2D();
        gc.restore();
        gc.scale(p, p);
    }
    @Override public void setPos(int xPos, int yPos) {
        x = xPos;
        y = yPos;
        basis.setTranslateX(x);
        basis.setTranslateY(y);
    }
    @Override public void setGlobalAlpha(double alpha) {
        basis.getGraphicsContext2D().setGlobalAlpha(alpha);
    }
    @Override public boolean inside(int mx, int my) {
        boolean first, second;
        if (horizontalFirst) {
            first = between(x, x+dx, mx) && my == y;
            second = between(y, y+dy, my) && mx == x+dx;
        } else {
            first = between(y, y+dy, my) && mx == x;
            second = between(x, x+dx, mx) && my == y+dy;
        }
        return first || second;
    }

    @Override
    public Canvas getBasis() {
        return basis;
    }
    @Override public int getWidth() {
        return Math.abs(dx);
    }
    @Override public int getHeight() {
        return Math.abs(dy);
    }

    // informative
    @Override public String getPrefixID() {
        return "dx";
    }
    @Override public void setID(String id) {
        this.id = id;
    }
    @Override public String getID() {
        return id;
    }
    String getX() {
        return Integer.toString(x);
    }
    String getY() {
        return Integer.toString(y);
    }
    String getDX() {
        return Integer.toString(dx);
    }
    String getDY() {
        return Integer.toString(dy);
    }
    String getHorizontalFirst() {
        return horizontalFirst ? "true" : "false";
    }

    private static boolean between(int a, int b, int c) { // whether c is between a and b
        return Math.abs(a - c) + Math.abs(c - b) == Math.abs(a - b);
    }

}
