package engine;

import engine.interfaces.Informative;
import engine.interfaces.Renderable;
import gui.control.ControlMain;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class Wire implements Renderable, Informative {

    // rendering
    private Canvas basis;
    private double p;
    private int x, y; // in periods, point (x, y) is the beginning of the wire (global position)
    private int dx, dy; // in periods, point (x+dx, y+dy) is the end of the wire (global position)
    private int ctx, cty; // in periods, translates for canvas position (relative to (x, y) point)
    private double alpha;
    private boolean horizontalFirst; // layout form: true if horizontal line begins at (0, 0)

    // connectivity
    private ArrayList<Component.Pin> pins;
    private Node node; // the node, that contains this wire

    private String id;

    public Wire() {
        // rendering
        basis = new Canvas();
        x = 0;   y = 0;
        dx = 0;  dy = 0;
        ctx = 0; cty = 0;
        horizontalFirst = true;

        // connectivity
        node = null;
        pins = new ArrayList<>();
    }

    // connectivity
    // TODO: establish connection ideology
    void connect(Node node) {
        this.node = node;
    }
    void connect(Component.Pin pin) {
        pins.add(pin);
    }
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

    // wire insertion logic
    public void layoutAgain(int mx, int my) {
        // compute new pair (dx, dy) - vector to the end of the wire
        int dx = mx - x;
        int dy = my - y;

        // if something changed
        if (dx != this.dx || dy != this.dy) {
            this.dx = dx;
            this.dy = dy;

            // flip canvas if the end of the wire crossed over at least one of coordinate axis
            if (dx >= 0) ctx = 0;
            else ctx = dx;

            if (dy >= 0) cty = 0;
            else cty = dy;

            updateCanvasSize();
            updateCanvasPos();
            render();
        }
    }
    public void flip() {
        horizontalFirst = !horizontalFirst;
        render();
    }
    private void updateCanvasPos() {
        basis.setTranslateX((x + ctx - ControlMain.LINE_WIDTH / 2) * p);
        basis.setTranslateY((y + cty - ControlMain.LINE_WIDTH / 2) * p);
    }
    private void updateCanvasSize() {
        basis.setWidth(p * (getWidth() + ControlMain.LINE_WIDTH));
        basis.setHeight(p * (getHeight() + ControlMain.LINE_WIDTH));
    }

    // rendering
    @Override public void render() {
        double l = ControlMain.LINE_WIDTH;

        // configure gc
        GraphicsContext gc = basis.getGraphicsContext2D();
        gc.save();
        gc.scale(p, p);
        gc.clearRect(0, 0, getWidth() + l, getHeight() + l);
        gc.translate(-ctx + l / 2, -cty + l / 2);
        gc.setGlobalAlpha(alpha);
        gc.setLineWidth(l);
        if (node != null) gc.setStroke(node.getSignal().colour());
        else gc.setStroke(LogicLevel.ZZZ.colour());

        // draw the wire
        if (horizontalFirst) {
            gc.strokeLine(0, 0, dx, 0);
            gc.strokeLine(dx, 0, dx, dy);
        } else {
            gc.strokeLine(0, 0, 0, dy);
            gc.strokeLine(0, dy, dx, dy);
        }

        // reset gc
        gc.restore();
    }
    @Override public void setGridPeriod(double period) {
        p = period;
        updateCanvasSize();
        updateCanvasPos();
    }
    @Override public void setPos(int xPos, int yPos) { // set position of the beginning of the wire
        x = xPos;
        y = yPos;
        updateCanvasPos();
    }
    @Override public void setGlobalAlpha(double alpha) {
        this.alpha = alpha;
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
