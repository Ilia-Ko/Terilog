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
    private boolean canInfluenceOwner;

    public Pin(Component owner, boolean canInfluence, int xPosInOwner, int yPosInOwner) {
        this.owner = owner;
        canInfluenceOwner = canInfluence;
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
            setFill(signal.colour());
            return true;
        }
        return false;
    }
    public LogicLevel query() {
        if (isNodified) return node.query();
        else return LogicLevel.ZZZ;
    }
    public HashSet<Node> simulate() { // called by nodes only
        if (canInfluenceOwner) return owner.simulate();
        else return new HashSet<>();
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
