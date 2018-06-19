package engine.components;

import engine.LogicLevel;
import engine.connectivity.Connectible;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;

public class Pin extends Circle implements Connectible {

    private String name;
    private Connectible connect;

    public Pin(Pane parent, String name, int xPosInOwner, int yPosInOwner) {
        this.name = name;
        setCenterX(xPosInOwner);
        setCenterY(yPosInOwner);
        setRadius(0.3);
        setFill(LogicLevel.ZZZ.colour());
        parent.getChildren().add(this);
    }

    // simulation
    @Override public void sendSig(LogicLevel signal) {
        connect.sendSig(signal);
        setFill(signal.colour());
    }

    // connectivity
    @Override public void connect(Connectible con) {
        connect = con;
    }
    @Override public void disconnect(Connectible con) {
        if (connect == con) {
            connect = null;
            con.disconnect(this);
        }
    }
    @Override public void disconnect() {
        connect.disconnect(this);
    }
    @Override public LogicLevel sig() {
        return connect.sig();
    }

    // xml info
    public String getName() {
        return name;
    }

}
