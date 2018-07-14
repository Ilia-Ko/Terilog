package engine.components;

import engine.LogicLevel;
import engine.connectivity.Connectible;
import engine.connectivity.Node;
import engine.wires.Wire;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;

import java.util.HashSet;

import static engine.LogicLevel.ZZZ;

public class Pin extends Circle implements Connectible {

    private Component owner;
    private HashSet<Connectible> connectibles;
    private Node node;
    private LogicLevel signal;
    private boolean highImpedance;

    // initialization
    public Pin(Component owner, boolean highImpedance, int xPosInOwner, int yPosInOwner) {
        this.owner = owner;
        this.highImpedance = highImpedance;
        connectibles = new HashSet<>();
        signal = ZZZ;

        setCenterX(xPosInOwner);
        setCenterY(yPosInOwner);
        setRadius(0.3);
        setFill(ZZZ.colour());
        owner.getRoot().getChildren().add(this);
    }

    // connectivity
    @Override public void reset(boolean denodify) {
        if (denodify) {
            node = null;
            connectibles = new HashSet<>();
        }
        put(ZZZ);
    }
    @Override public void put(LogicLevel signal) { // called by owner only
        this.signal = signal;
        setFill(signal.colour());
    }
    @Override public LogicLevel get() {
        return signal;
    }
    public Node node() {
        return node;
    }
    public boolean hasLowImpedance() {
        return !highImpedance;
    }
    // parsing.stage1
    @Override public void inspect(Wire wire) {
        Point2D pos = owner.getRoot().localToParent(getCenterX(), getCenterY());
        int mx = (int) pos.getX();
        int my = (int) pos.getY();
        if (wire.inside(mx, my)) Connectible.establishConnection(this, wire);
    }
    @Override public boolean inside(int px, int py) {
        Point2D pos = owner.getRoot().localToParent(getCenterX(), getCenterY());
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
