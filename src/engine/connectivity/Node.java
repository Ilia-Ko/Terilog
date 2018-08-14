package engine.connectivity;

import engine.LogicLevel;
import engine.components.Pin;
import engine.wires.Wire;

import java.util.Arrays;
import java.util.HashSet;

import static engine.LogicLevel.ERR;
import static engine.LogicLevel.ZZZ;

public class Node {

    private HashSet<Wire> wires; // wires, included in this node
    private HashSet<Pin> pins; // pins, connected to this node
    private int length;
    private LogicLevel[] sigResult; // the current signal on the node

    public Node(int busLength) {
        length = busLength;
        sigResult = new LogicLevel[length];
        Arrays.fill(sigResult, ZZZ);

        pins = new HashSet<>();
        wires = new HashSet<>();
    }

    // connectivity
    public void add(Wire wire) {
        assert wire.capacity() == length;
        wires.add(wire);
    }
    public void add(Pin pin) {
        assert pin.capacity() == length;
        pins.add(pin);
    }

    // simulation
    public void reset() {
        Arrays.fill(sigResult, ZZZ);
        update();
    }
    public boolean isStable() {
        // remember previous state
        LogicLevel[] oldSig = new LogicLevel[length];
        System.arraycopy(sigResult, 0, oldSig, 0, length);
        Arrays.fill(sigResult, ZZZ);

        // compute new state
        pins.forEach(pin -> {
            if (pin.hasLowImpedance()) {
                LogicLevel[] pinSig = pin.get();
                for (int i = 0; i < length; i++) {
                    if (pinSig[i].conflicts(sigResult[i])) sigResult[i] = ERR;
                    else if (pinSig[i].suppresses(sigResult[i])) sigResult[i] = pinSig[i];
                }
            }
        });
        update();

        // return true if sigResult does not change
        return Arrays.equals(oldSig, sigResult);
    }
    private void update() {
        pins.forEach(pin -> pin.put(sigResult));
        wires.forEach(wire -> wire.put(sigResult));
    }

}
