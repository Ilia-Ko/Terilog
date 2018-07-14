package engine.connectivity;

import engine.LogicLevel;
import engine.components.Pin;
import engine.wires.Wire;

import java.util.HashSet;

import static engine.LogicLevel.ERR;
import static engine.LogicLevel.ZZZ;

public class Node {

    private HashSet<Wire> wires; // wires, included in this node
    private HashSet<Pin> pins; // pins, connected to this node

    private LogicLevel sigResult; // the current signal on the node

    public Node() {
        sigResult = ZZZ;

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
        sigResult = ZZZ;
        update();
    }
    public boolean isStable() { // return true if sigResult does not change
        LogicLevel oldSig = sigResult;
        sigResult = ZZZ;
        pins.forEach(pin -> {
            if (pin.hasLowImpedance()) {
                LogicLevel pinSig = pin.get();
                if (pinSig.conflicts(sigResult)) sigResult = ERR;
                else if (pinSig.suppresses(sigResult)) sigResult = pinSig;
            }
        });
        update();
        return oldSig == sigResult;
    }
    private void update() {
        if (sigResult != ERR) pins.forEach(pin -> pin.put(sigResult));
        wires.forEach(wire -> wire.put(sigResult));
    }

//    public static void join(Pin p1, Pin p2) {
//        Node n1 = p1.node();
//        Node n2 = p2.node();
//        n1.joint.addAll(n2.pins);
//        n2.joint.addAll(n1.pins);
//    }
//    public static void separate(Pin p1, Pin p2) {
//        Node n1 = p1.node();
//        Node n2 = p2.node();
//        n1.joint.removeAll(n2.pins);
//        n2.joint.removeAll(n1.pins);
//    }

}
