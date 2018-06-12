package engine;

import engine.interfaces.Informative;
import engine.interfaces.Renderable;
import gui.Control;
import javafx.scene.canvas.GraphicsContext;

public class Wire implements Renderable, Informative {

    // rendering
    private int x, y, x1, y1; // absolute values
    private boolean horizontalFirst; // layout form: true if horizontal line begins at (0, 0)
    private Node node; // the node, that contains this wire

    private String id;

    public Wire() {
        x = 0;
        y = 0;
        x1 = 0;
        y1 = 0;
        horizontalFirst = true;
        node = null;
    }

    // connectivity
    int[][] getPoints() {
        int[][] points = new int[3][];
        points[0] = new int[] {x, y};
        points[1] = new int[] {x1, y1};
        if (horizontalFirst)
            points[2] = new int[] {x1, y};
        else
            points[2] = new int[] {y1, x};
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
        x1 = mx;
        y1 = my;
    }
    public void flip() {
        horizontalFirst = !horizontalFirst;
    }

    // rendering
    @Override public void render(GraphicsContext gc) {
        // transform
        gc.setFill(node.getCurrentSignal().colour());
        gc.setLineWidth(Control.LINE_WIDTH);

        // draw the wire
        if (horizontalFirst) {
            gc.strokeLine(x, y, x1, y);
            gc.strokeLine(x1, y, x1, y1);
        } else {
            gc.strokeLine(x, y, x, y1);
            gc.strokeLine(x, y1, x1, y1);
        }
    }
    @Override public void setPos(int xPos, int yPos) {
        x = xPos;
        y = yPos;
    }
    @Override public boolean inside(int mx, int my) {
        boolean first, second;
        if (horizontalFirst) {
            first = between(x, x1, mx) && my == x;
            second = between(y, y1, my) && mx == x1;
        } else {
            first = between(y, y1, my) && mx == y;
            second = between(x, x1, mx) && my == y1;
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
    String getX() {
        return Integer.toString(x);
    }
    String getY() {
        return Integer.toString(y);
    }
    String getX1() {
        return Integer.toString(x1);
    }
    String getY1() {
        return Integer.toString(y1);
    }
    String getHorizontalFirst() {
        return horizontalFirst ? "true" : "false";
    }

    private static boolean between(int a, int b, int c) { // whether c is between a and b
        return Math.abs(a - c) + Math.abs(c - b) == Math.abs(a - b);
    }

}
