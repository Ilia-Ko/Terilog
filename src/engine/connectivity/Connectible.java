package engine.connectivity;

public interface Connectible {

    void connect(Connectible con);
    void disconnect(Connectible con);
    void disconnect(); // disconnect from everything

}
