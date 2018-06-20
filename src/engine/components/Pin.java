package engine.components;

import engine.LogicLevel;
import engine.connectivity.Connection;
import engine.connectivity.Node;
import engine.connectivity.SignalTransfer;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

public class Pin extends Circle implements SignalTransfer {

    private String name;
    private Connection connect;
    private boolean isConnected;
    private boolean isNodified;
    private Node node;

    public Pin(Pane parent, String name, int xPosInOwner, int yPosInOwner) {
        this.name = name;
        isConnected = false;
        isNodified = false;

        setCenterX(xPosInOwner);
        setCenterY(yPosInOwner);
        setRadius(0.3);
        setFill(LogicLevel.ZZZ.colour());
        parent.getChildren().add(this);
    }

    // simulation
    @Override public void announce(LogicLevel signal) {
        if (isConnected) node.announce(signal);
        setFill(signal.colour());
    }
    @Override public LogicLevel query() {
        if (isNodified) return node.query();
        else return LogicLevel.ZZZ;
    }

    // connectivity
    @Override public void connectTo(Connection con) {
        if (isConnected && con.placeMatches(connect)) connect = con;
    }
    @Override public void disconnectFrom(Connection con) {
        if (isConnected && con.placeMatches(connect)) {
            connect = null;
            isConnected = false;
        }
    }
    @Override public void totalDisconnect() {
        if (isConnected) connect.terminate();
    }

    // xml info
    public String getName() {
        return name;
    }

}
