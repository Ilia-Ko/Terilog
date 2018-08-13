package engine.components;

import engine.LogicLevel;
import engine.connectivity.Connectible;
import engine.connectivity.Node;
import engine.wires.Wire;
import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;

import java.util.HashSet;

import static engine.LogicLevel.ZZZ;

public class Pin extends Rectangle implements Connectible {

    private Component owner;
    private HashSet<Connectible> connectibles;
    private Node node;
    private int length, cx, cy;
    private LogicLevel[] signal;
    private boolean highImpedance;

    // initialization (vertical version)
    public Pin(Component owner, boolean highImpedance, int busLength, int xPosInOwner, int yPosInOwner) {
        this.owner = owner;
        this.highImpedance = highImpedance;
        length = busLength;
        cx = xPosInOwner;
        cy = yPosInOwner;
        connectibles = new HashSet<>();
        signal = new LogicLevel[length];

        double h = 0.1 * (length + 3);
        setX(xPosInOwner - 0.2);
        setY(yPosInOwner - h / 2.0);
        setWidth(0.4);
        setHeight(h);
        setArcWidth(0.1);
        setArcHeight(0.1);
        setFill(ZZZ.colour());
        owner.getRoot().getChildren().add(this);
    }

    // connectivity
    @Override public void reset(boolean denodify) {
        if (denodify) {
            node = null;
            connectibles = new HashSet<>();
        }
        for (int i = 0; i < length; i++) signal[i] = ZZZ;
    }
    @Override public void put(LogicLevel[] signal) { // called by owner only
        assert signal.length == length;
        System.arraycopy(signal, 0, this.signal, 0, length);
    }
    public void put(LogicLevel signal) { // called by owner only
        this.signal[0] = signal;
    }
    @Override public LogicLevel[] get() {
        return signal;
    }
    @Override public int length() {
        return length;
    }

    public boolean hasLowImpedance() {
        return !highImpedance;
    }
    // parsing.stage1
    @Override public void inspect(Wire wire) {
        Point2D pos = owner.getRoot().localToParent(cx, cy);
        int mx = (int) pos.getX();
        int my = (int) pos.getY();
        if (wire.inside(mx, my)) Connectible.establishConnection(this, wire);
    }
    @Override public boolean inside(int px, int py) {
        Point2D pos = owner.getRoot().localToParent(cx, cy);
        int mx = (int) pos.getX();
        int my = (int) pos.getY();
        return px == mx && py == my;
    }
    @Override public void connect(Connectible con) {
        connectibles.add(con);
    }
    // parsing.stage2
    @Override public boolean isNodeFree() {
        return node == null;
    }
    @Override public void nodify(Node node) {
        this.node = node;
        node.add(this);
        for (Connectible con : connectibles)
            if (con.isNodeFree()) con.nodify(node);
    }

}
