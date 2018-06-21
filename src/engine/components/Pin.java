package engine.components;

import engine.LogicLevel;
import engine.connectivity.Node;
import engine.connectivity.Wire;
import javafx.scene.shape.Circle;

import java.util.HashSet;

public class Pin extends Circle {

    private Component owner;
    private boolean isNodified;
    private Node node;

    public Pin(Component owner, int xPosInOwner, int yPosInOwner) {
        this.owner = owner;
        isNodified = false;

        setCenterX(xPosInOwner);
        setCenterY(yPosInOwner);
        setRadius(0.3);
        setFill(LogicLevel.ZZZ.colour());
        owner.getRoot().getChildren().add(this);
    }

    // simulation
    public boolean update(LogicLevel signal) { // called by owner only
        if (isNodified && node.update(signal)) {
            return true;
        }
        setFill(signal.colour());
        return false;
    }
    public LogicLevel query() {
        if (isNodified) return node.query();
        else return LogicLevel.ZZZ;
    }
    public HashSet<Node> simulate() { // called by nodes only
        return owner.simulate();
    }

    // connectivity
    void inspect(Wire wire) {
        int mx = (int) (getParent().getLayoutX() + getCenterX());
        int my = (int) (getParent().getLayoutY() + getCenterY());
        if (wire.inside(mx, my)) node = node.mergeAndCopy(wire.gather());
    }
    void nodify() {
        node = new Node(this);
        isNodified = true;
    }
    public Node gather() {
        if (isNodified) return node;
        else return null;
    }

}
