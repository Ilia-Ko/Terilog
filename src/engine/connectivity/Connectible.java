package engine.connectivity;

import engine.wires.Wire;

public interface Connectible {

    // parsing.stage1: searching for connections
    boolean inside(int px, int py);
    void inspect(Wire wire);
    void connect(Connectible con);

    // parsing.stage2: spreading and sharing nodes
    boolean isNodeFree();
    void nodify(Node node);

    void reset();
    static void establishConnection(Connectible a, Connectible b) {
        a.connect(b);
        b.connect(a);
    }

}
