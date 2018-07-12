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
        if (oldSig != sigResult) {
            update();
            return false;
        }
        return true;
    }
    private void update() {
        if (sigResult != ERR) pins.forEach(pin -> pin.put(sigResult));
        wires.forEach(wire -> wire.put(sigResult));
    }

}
