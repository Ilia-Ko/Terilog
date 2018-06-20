package engine.connectivity;

import engine.LogicLevel;

public interface SignalTransfer extends Connectible {

    // simulation
    void announce(LogicLevel signal);
    LogicLevel query();

}
