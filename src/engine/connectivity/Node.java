package engine.connectivity;

import engine.LogicLevel;
import engine.components.Pin;
import engine.wires.Wire;

import java.util.HashSet;

public class Node {

    private HashSet<Wire> wires; // wires, included in this node
    private HashSet<Pin> pins; // pins, connected to this node

    private LogicLevel sigResult; // the current signal on the node

    public Node() {
        sigResult = LogicLevel.ZZZ;

        pins = new HashSet<>();
        wires = new HashSet<>();
    }

    // connectivity
    public void add(Wire wire) {
        wires.add(wire);
    }
    public void add(Pin pin) {
        pins.add(pin);
    }

    // simulation
    public HashSet<Node> simulate() {
        HashSet<Node> affected = new HashSet<>();
        for (Pin pin : pins) affected.addAll(pin.simulate());
        return affected;
    }
    public boolean update(LogicLevel signal) { // return true if sigResult changes
        if (signal.conflicts(sigResult)) {
            sigResult = LogicLevel.ERR;
            updateWires();
            return true;
        } else if (signal.suppresses(sigResult)) {
            sigResult = signal;
            updateWires();
            return true;
        }
        return false;
    }
    public LogicLevel query() {
        return sigResult;
    }
    private void updateWires() {
        for (Wire wire : wires) wire.setStroke(sigResult.colour());
    }

}
