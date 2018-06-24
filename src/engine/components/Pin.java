package engine.components;

import engine.LogicLevel;
import engine.connectivity.Connectible;
import engine.connectivity.Node;
import engine.wires.Wire;
import javafx.geometry.Point2D;
import javafx.scene.shape.Circle;

import java.util.HashSet;

public class Pin extends Circle implements Connectible {

    private Component owner;
    private HashSet<Connectible> connectibles;
    private Node node;
    private LogicLevel sigFromOwner;
    private boolean canAffectOwner, canAffectNode;

    public Pin(Component owner, boolean affectsOwner, boolean affectsNode, int xPosInOwner, int yPosInOwner) {
        this.owner = owner;
        sigFromOwner = LogicLevel.ZZZ;
        canAffectOwner = affectsOwner;
        canAffectNode = affectsNode;

        setCenterX(xPosInOwner);
        setCenterY(yPosInOwner);
        setRadius(0.3);
        setFill(LogicLevel.ZZZ.colour());
        owner.getRoot().getChildren().add(this);
    }

    // simulation
    public boolean update(LogicLevel signal) { // called by owner only
        sigFromOwner = signal;
        setFill(signal.colour());
        return node != null && canAffectNode && node.update();
    }
    public LogicLevel querySigFromNode() {
        if (node != null) return node.query();
        else return LogicLevel.ZZZ;
    }
    public LogicLevel querySigFromOwner() {
        if (canAffectNode) return sigFromOwner;
        else return LogicLevel.ZZZ;
    }
    public HashSet<Node> simulate() { // called by nodes only
        if (node != null) setFill(node.query().colour());
        if (canAffectOwner) return owner.simulate();
        else return new HashSet<>();
    }
    public Node getNode() {
        return node;
    }

    // connectivity
    @Override public void reset(boolean denodify) {
        if (denodify) {
            node = null;
            connectibles = new HashSet<>();
        }
        setFill(LogicLevel.ZZZ.colour());
        if (canAffectOwner) owner.simulate();
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
