package engine.connectivity;

import engine.LogicLevel;
import engine.components.Pin;

import java.util.ArrayList;
import java.util.HashSet;

public class Node {

    private ArrayList<Pin> pins; // pins, connected to this node
    private ArrayList<Wire> wires; // wires, included in this node

    private LogicLevel sigResult; // the current signal on the node

    public Node(Pin initial) { // create node with initial pin
        this();
        pins.add(initial);
    }
    Node(Wire initial) {
        this();
        wires.add(initial);
    }
    private Node() {
        pins = new ArrayList<>();
        wires = new ArrayList<>();

        sigResult = LogicLevel.ZZZ;
    }

    // connectivity
    public Node mergeAndCopy(Node another) {
        another.pins.addAll(pins);
        another.wires.addAll(wires);
        return another;
    }

    // simulation
    public HashSet<Node> simulate() {
        HashSet<Node> affected = new HashSet<>();
        for (Pin pin : pins) affected.addAll(pin.simulate());
        return affected;
    }
    public boolean update(LogicLevel signal) {
        if (signal.conflicts(sigResult)) {
            sigResult = LogicLevel.ERR;
            return true;
        } else if (signal.suppresses(sigResult)) {
            sigResult = signal;
            return true;
        }
        return false;
    }
    public LogicLevel query() {
        return sigResult;
    }

}
