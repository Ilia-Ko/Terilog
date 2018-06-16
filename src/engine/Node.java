package engine;

import engine.interfaces.Informative;

import java.util.ArrayList;
import java.util.Arrays;

public class Node implements Informative { // 'logical' class, no rendering

    // simulation
    private ArrayList<Component> components; // components, connected to this node

    private LogicLevel[] signals; // input signals from components
    private LogicLevel sigResult; // the current signal on the node

    // informative
    private String id;
    private ArrayList<Wire> wires;

    Node() {
        components = new ArrayList<>();
        wires = new ArrayList<>();
        signals = null;
        sigResult = LogicLevel.ZZZ;
    }

    // simulation
    void reset() {
        signals = new LogicLevel[components.size()];
        Arrays.fill(signals, LogicLevel.ZZZ);
        sigResult = LogicLevel.ZZZ;
    }
    ArrayList<Node> propagate() {
        /* Propagation is a part of destabilization of a circuit. When a node is unstable, it affects
        those components, whose inputs are connected to the node. It means, that these components should
        compute their outputs again. When they produce new signals, their outputs become unstable too.
        This function makes affected components to recalculate their outputs and returns the list of those
        nodes, who become unstable after the computation.
         */
        ArrayList<Node> unstable = new ArrayList<>();
        for (Component c : components) unstable.addAll(c.simulate());
        return unstable;
    }
    boolean stabilize() {
        /* Definition: a node is STABLE if and only if the result of interaction between input signals
        equals to the current signal on the node.
        Stabilization of a node is a part of the global stabilization process which flows parallel
        to the circuit destabilization, but usually dominate over the latest.
        Note: if at least two input signals conflicts to each other, the result of their interaction
        is LogicLevel.ERR signal, which suppresses everything.
        */
        LogicLevel sig = signals[0];
        for (LogicLevel signal : signals)
            if (sig.conflicts(signal)) {
                sig = LogicLevel.ERR;
                break;
            }
        boolean result = (sigResult == sig);
        if (sig.suppresses(sigResult)) sigResult = sig;
        return result;
    }
    LogicLevel getSignal() {
        return sigResult;
    }
    void putSignal(Component fromWho, LogicLevel signal) {
        int address = components.indexOf(fromWho);
        signals[address] = signal;
    }

    // connectivity
    // TODO: establish connection ideology
    void connect(Component.Pin pin) {

    }
    void addPin(Component.Pin pin) {
        components.add(pin.getParent());
    }
    void delPin(Component.Pin pin) {
        components.remove(pin.getParent());
    }
    void delWire(Wire wire) {
        wires.remove(wire);
    }
    ArrayList<Component> getComponents() {
        return components;
    }

    // informative
    @Override public String getPrefixID() {
        return "n";
    }
    @Override public void setID(String id) {
        this.id = id;
    }
    @Override public String getID() {
        return id;
    }
    void addWire(Wire w) {
        wires.add(w);
    }
    ArrayList<Wire> getWires() {
        return wires;
    }

}
