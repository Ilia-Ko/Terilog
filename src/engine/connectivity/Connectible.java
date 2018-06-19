package engine.connectivity;

import engine.LogicLevel;

public interface Connectible { // and 'Simulatable' too

    // connectivity
    void connect(Connectible con);
    void disconnect(Connectible con);
    void disconnect(); // disconnect from everything

    // simulation
    LogicLevel sig(); // get current signal on this Connectible
    void sendSig(LogicLevel signal); // inform this Connectible about neighbour's signal

}
