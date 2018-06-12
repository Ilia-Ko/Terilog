package engine;

import engine.interfaces.Informative;

import java.util.ArrayList;
import java.util.Arrays;

public class Node implements Informative { // 'logical' class, no rendering

    // simulation
    private ArrayList<Component> inputs; // those components, who puts a signal to this node
    private ArrayList<Component> outputs; // those components, who gets a signal from this node

    private LogicLevel[] inputSignals; // input signals from components
    private LogicLevel currentSig; // the current signal on the node

    // informative
    private String id;
    private ArrayList<Wire> wires;

    Node() {
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        wires = new ArrayList<>();
        inputSignals = null;
        currentSig = LogicLevel.ZZZ;
    }

    // simulation
    void reset() {
        inputSignals = new LogicLevel[inputs.size()];
        Arrays.fill(inputSignals, LogicLevel.NIL);
        currentSig = LogicLevel.NIL;
    }
    ArrayList<Node> propagate() {
        /* Propagation is a part of destabilization of a circuit. When a node is unstable, it affects
        those components, whose inputs are connected to the node. It means, that these components should
        compute their outputs again. When they produce new signals, their outputs become unstable too.
        This function makes affected components to recalculate their outputs and returns the list of those
        nodes, who become unstable after the computation.
         */
        ArrayList<Node> unstable = new ArrayList<>();
        for (Component c : outputs) unstable.addAll(c.simulate());
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
        LogicLevel sig = inputSignals[0];
        for (LogicLevel signal : inputSignals)
            if (sig.conflicts(signal)) {
                sig = LogicLevel.ERR;
                break;
            }
        boolean result = (currentSig == sig);
        if (sig.suppresses(currentSig)) currentSig = sig;
        return result;
    }
    LogicLevel getCurrentSignal() {
        return currentSig;
    }
    public void receiveSignal(Component fromWho, LogicLevel signal) {
        int address = inputs.indexOf(fromWho);
        inputSignals[address] = signal;
    }

    // connectivity
    void addPin(Component.Pin pin) {
        int type = pin.getType();
        Component c = pin.getParent();

        if (type == Component.Pin.INPUT) // if 'pin' is input (relative to a component)
            outputs.add(c); // then add it to outputs (relative to a node)
        else if (type == Component.Pin.OUTPUT) // and vice versa
            inputs.add(c);
    }
    void delPin(Component.Pin pin) {
        int type = pin.getType();
        Component c = pin.getParent();

        if (type == Component.Pin.INPUT) // if 'pin' is input (relative to a component)
            outputs.remove(c); // then add it to outputs (relative to a node)
        else if (type == Component.Pin.OUTPUT) // and vice versa
            inputs.remove(c);
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
