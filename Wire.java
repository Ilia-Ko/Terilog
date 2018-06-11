package engine;

import engine.interfaces.Renderable;
import gui.Main;
import javafx.scene.canvas.GraphicsContext;

public class Wire implements Renderable {

    // rendering
    private static final double CONNECTION_OPACITY = 1.0 / 3.0;
    private static final double CONNECTION_SIZE = 3.0;

    private int x, y, x1, y1; // x1, y1, x2 and y2 are relative to absolute (x, y)
    private boolean horizontalFirst; // layout form: true if horizontal line begins at (0, 0)
    private Node node; // the node, that contains this wire

    private String id;

    public Wire() {
        x = 0;
        y = 0;
        x1 = 0;
        y1 = 0;
        node = null;
    }

    // connecting
    public int[][] getPoints() {
        int[][] points = new int[3][];
        points[0] = new int[] {x, y};
        points[1] = new int[] {x1, y1};
        if (horizontalFirst)
            points[2] = new int[] {x1, y};
        else
            points[2] = new int[] {y1, x};
        return points;
    }
    public Node getNode() {
        return node;
    }
    public void setNode(Node node) {
        this.node = node;
    }

    // laying out by the mouse
    public void layoutAgain(int mx, int my) {
        x1 = mx - x;
        y1 = my - y;
    }
    public void flip() {
        horizontalFirst = !horizontalFirst;
    }

    // rendering
    @Override public void render(GraphicsContext gc) {
        double lw = 4;
        // transform
        gc.translate(x, y);
        gc.setFill(node.getCurrentSignal().colour());
        gc.setLineWidth(lw);

        // draw the wire
        if (horizontalFirst) {
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
    @Override public void setPos(int xPos, int yPos) {
        x = xPos;
        y = yPos;
    }
    @Override public boolean inside(int mx, int my) {
        mx -= x;
        my -= y;
        boolean first, second;
        if (horizontalFirst) {
            first = between(0, x1, mx) && my == 0;
            second = between(0, y1, my) && mx == x1;
        } else {
            first = between(0, y1, my) && mx == 0;
            second = between(0, x1, mx) && my == y1;
        }
        return first || second;
    }

    @Override public int getWidth() {
        return Math.abs(x1);
    }
    @Override public int getHeight() {
        return Math.abs(y1);
    }

    // informative
    @Override public String getPrefixID() {
        return "w";
    }
    @Override public void setID(String id) {
        this.id = id;
    }
    @Override public String getID() {
        return id;
    }
    @Override public String getX() {
        return Integer.toString(x);
    }
    @Override public String getY() {
        return Integer.toString(y);
    }
    public String getX1() {
        return Integer.toString(x1);
    }
    public String getY1() {
        return Integer.toString(y1);
    }
    public String getHorizontalFirst() {
        return horizontalFirst ? "true" : "false";
    }

    private boolean between(int a, int b, int c) { // whether c is between a and b
        return Math.abs(a - c) + Math.abs(c - b) == Math.abs(a - b);
    }

}
