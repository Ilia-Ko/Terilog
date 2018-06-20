package engine.connectivity;

import javafx.beans.property.IntegerProperty;

public class Connection {

    private Connectible a, b; // subjects to connectTo
    private boolean hasA, hasB;
    private IntegerProperty x, y; // connection coordinates in Pane ControlMain.parent

    public Connection(Connectible a, Connectible b, IntegerProperty x, IntegerProperty y) { // create connection with both sides
        this.x = x;
        this.y = y;

        this.a = a;
        hasA = true;
        a.connectTo(this);

        this.b = b;
        hasB = true;
        b.connectTo(this);
    }
    public Connection(Connectible a, IntegerProperty x, IntegerProperty y) { // create connection with one side
        this.x = x;
        this.y = y;
        
        this.a = a;
        hasA = true;
        
        hasB = false;
    }
    public Connection(Connectible a, Connection old) { // create new connection from old one, with only one side "a"
        this.x = old.x;
        this.y = old.y;
        
        this.a = a;
        hasA = true;
        
        hasB = false;
    }
    public void terminate() {
        a.disconnectFrom(this);
        hasA = false;
        b.disconnectFrom(this);
        hasB = false;
    }

    public boolean isConnected(Connectible one, Connectible toAnother) {
        return hasA && hasB && (a == one && b == toAnother) || (a == toAnother && b == one);
    }
    public Connectible getPairFor(Connectible whom) {
        if (whom == a && hasB) return b;
        else if (whom == b && hasA) return a;
        else return null;
    }
    public boolean placeMatches(Connection another) {
        return x.get() == another.x.get() && y.get() == another.y.get();
    }

}
