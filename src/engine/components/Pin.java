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
    private Node node;
    private HashSet<Connectible> connectibles;
    private boolean canInfluenceOwner;

    public Pin(Component owner, boolean canInfluence, int xPosInOwner, int yPosInOwner) {
        this.owner = owner;
        canInfluenceOwner = canInfluence;

        setCenterX(xPosInOwner);
        setCenterY(yPosInOwner);
        setRadius(0.3);
        setFill(LogicLevel.ZZZ.colour());
        owner.getRoot().getChildren().add(this);
    }

    // simulation
    public boolean update(LogicLevel signal) { // called by owner only
        if (node != null && node.update(signal)) {
            setFill(signal.colour());
            return true;
        }
        return false;
    }
    public LogicLevel query() {
        if (node != null) return node.query();
        else return LogicLevel.ZZZ;
    }
    public HashSet<Node> simulate() { // called by nodes only
        if (node != null) setFill(node.query().colour());
        if (canInfluenceOwner) return owner.simulate();
        else return new HashSet<>();
    }
    public Node getNode() {
        return node;
    }

    // connectivity
    @Override public void reset() {
        node = null;
        connectibles = new HashSet<>();
        setFill(LogicLevel.ZZZ.colour());
        if (canInfluenceOwner) owner.simulate();
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
