package engine.connectivity;

public interface Connectible { // and 'Simulatable' too

    // connectivity
    void connectTo(Connection con);
    void disconnectFrom(Connection con);
    void totalDisconnect(); // totalDisconnect from everything

}
