package engine.connectivity;

import engine.LogicLevel;
import engine.components.Component;

import java.util.ArrayList;

public class Node implements SignalTransfer { // 'logical' class, no rendering

    // simulation
    private ArrayList<Component> components; // components, connected to this node

    private boolean isStable;
    private LogicLevel sigResult; // the current signal on the node

    Node() {
        components = new ArrayList<>();
        sigResult = LogicLevel.ZZZ;
        isStable = true;
    }

    // simulation
    public void announce(LogicLevel signal) {
        if (signal.conflicts(sigResult)) {
            sigResult = LogicLevel.ERR;
            isStable = false;
        } else if (signal.suppresses(sigResult)) {
            sigResult = signal;
            isStable = false;
        }
    }
    @Override public LogicLevel query() {
        return sigResult;
    }

//    public void reset() {
//        sigResult = LogicLevel.ZZZ;
//        isStable = true;
//    }
//    public ArrayList<Node> propagate() {
//        /* Propagation is a part of destabilization of a circuit. When a node is unstable, it affects
//        those components, whose inputs are connected to the node. It means, that these components should
//        compute their outputs again. When they produce new signals, their outputs become unstable too.
//        This function makes affected components to recalculate their outputs and returns the list of those
//        nodes, who become unstable after the computation.
//         */
//        ArrayList<Node> unstable = new ArrayList<>();
////        for (Component c : components) unstable.addAll(c.simulate());
//        return unstable;
//    }
//    public boolean stabilize() {
//        /* Definition: a node is STABLE if and only if the result of interaction between input signals
//        equals to the current signal on the node.
//        Stabilization of a node is a part of the global stabilization process which flows parallel
//        to the circuit destabilization, but usually dominate over the latest.
//        Note: if at least two input signals conflicts to each other, the result of their interaction
//        is LogicLevel.ERR signal, which suppresses everything.
//        */
////        LogicLevel sig = signals[0];
////        for (LogicLevel signal : signals)
////            if (sig.conflicts(signal)) {
////                sig = LogicLevel.ERR;
////                break;
////            }
////        boolean result = (sigResult == sig);
////        if (sig.suppresses(sigResult)) sigResult = sig;
////        return result;
//        return isStable;
//    }

    // connectivity
    ArrayList<Component> getComponents() {
        return components;
    }

    @Override public void connectTo(Connection con) {

    }
    @Override public void disconnectFrom(Connection con) {

    }
    @Override public void totalDisconnect() {

    }

}
