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
    public void reset() {
        sigResult = LogicLevel.ZZZ;
        updateWires();
    }
    public HashSet<Node> simulate() {
        HashSet<Node> affected = new HashSet<>();
        for (Pin pin : pins) affected.addAll(pin.simulate());
        return affected;
    }
    public boolean update() { // return true if sigResult changes
        boolean changed = sigResult != LogicLevel.ZZZ;
        sigResult = LogicLevel.ZZZ;
        for (Pin pin : pins) {
            LogicLevel pinSig = pin.querySigFromOwner();
            if (pinSig.conflicts(sigResult)) {
                sigResult = LogicLevel.ERR;
                changed = true;
            } else if (pinSig.suppresses(sigResult)) {
                sigResult = pinSig;
                changed = true;
            }
        }
        if (changed) updateWires();
        return changed;
    }
    public LogicLevel query() {
        return sigResult;
    }
    private void updateWires() {
        for (Wire wire : wires) wire.setStroke(sigResult.colour());
    }

}
