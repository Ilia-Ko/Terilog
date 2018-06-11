package engine;

import gui.Control;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class Circuit {

    private static final int SIM_DEPTH = 100;

    // construction logic
    private Control control;
    private ArrayList<Wire> wires;

    // simulation logic
    private boolean isSimRunning;
    private ArrayList<Component> components, constants;
    private ArrayList<Node> nodes;

    public Circuit(Control control) {
        // construction logic
        this.control = control;
        wires = new ArrayList<Wire>();

        // simulation logic
        isSimRunning = false;
        components = new ArrayList<Component>();
        constants = new ArrayList<Component>();
        nodes = new ArrayList<Node>();
    }

    public void renderAll(GraphicsContext gc) {
        for (Wire w : wires) w.render(gc);
        for (Component c : components) c.render(gc);
    }

    // construction logic
    public void add(Wire w) {
        wires.add(w);
        ArrayList<Component> ins = new ArrayList<Component>();
        ArrayList<Component> outs = new ArrayList<Component>();

        // check whether 'w' should be connected to existing components
        for (Component c : components)
            for (Component.Pin pin : c.getPins())
                if (w.inside(pin.getX(), pin.getY())) {
                    pin.bindWithWire(w);
                    int type = pin.getType();
                    if (type == Component.Pin.INPUT) // if 'pin' is input (relative to a component)
                        outs.add(c); // then mark it as output (relative to a node)
                    else if (type == Component.Pin.OUTPUT) // and vice versa
                        ins.add(c);
                }

        // check whether 'w' should be connected to existing wires
        boolean isNewNode = true;
        for (Wire old : wires)
            for (int[] p : w.getPoints())
                if (old.inside(p[0], p[1])) {
                    Node n = old.getNode();
                    w.setNode(n);
                    n.addWire(w);
                    n.addInputs(ins);
                    n.addOutputs(outs);
                    isNewNode = false;
                    break;
                }

        // create new node if needed (if 'w' does not participate in any existing ones)
        if (isNewNode) {
            Node n = new Node();
            w.setNode(n);
            n.addWire(w);
            n.addInputs(ins);
            n.addOutputs(outs);
            nodes.add(n);
        }
    }
    public void add(Component c) {
        components.add(c);
        if (c.isIndependent()) constants.add(c);

        // check whether 'c' should be connected to existing nodes (their wires)
        for (Component.Pin pin : c.getPins())
            for (Wire w : wires)
                if (w.inside(pin.getX(), pin.getY())) {
                    pin.bindWithWire(w);
                    w.getNode().addPin(pin);
                    break;
                }
    }

    // simulation logic
    public void startSimulation() {
        isSimRunning = true;
        ArrayList<Node> unstable = new ArrayList<Node>();

        /* Part I. Global destabilization:
            1) Reset all nodes - set every signal to LogicLevel.NIL
            2) 'Entry point': simulate 'constant' components and make list of those nodes,
                who was affected by this simulation step - 'unstable'.
            3) Try to stabilize them, remove all stable nodes. See /src/engine/Node.stabilize()
                for more info about the stabilization process.
            4) Now 'entry point' step is complete - the circuit is unstable and requires stabilization.
         */
        for (Node node : nodes) node.reset(); // reset all nodes
        for (Component constant : constants) unstable.addAll(constant.simulate()); // 'entry point'
        for (Node n : unstable)
            if (n.stabilize()) unstable.remove(n); // initial stabilization

        int attempts = 0; // count our attempts to stabilize the circuit

        /* Part II. Global stabilization:
            1) From the previous part we have a list of unstable nodes. It is important that they
                affect a lot of components, who are currently stable. We should propagate the unstable
                signal in order to find the new stable state of the circuit. After propagating these
                signals to components, the latest will change their outputs and a lot of new nodes will
                become unstable.
            2) After a destabilizing pass (propagation), we should make a stabilizing pass in order to
                find a new stable state of the circuit. If a node is stable, it does not affect its
                outputs, so they do not need to be recomputed. Removal of such nodes decreases the
                complexity of the simulation algorithm.
         */
        do {
            // destabilizing pass: propagate changes from unstable nodes to affected components
            ArrayList<Node> tmp = new ArrayList<Node>(unstable);
            for (Node n : tmp)
                unstable.addAll(n.propagate());

            // stabilizing pass: remove stable nodes from the next pass
            for (Node n : unstable)
                if (n.stabilize())
                    unstable.remove(n);

        } while (unstable.size() > 0 && (++attempts) < SIM_DEPTH && isSimRunning);

        // finalize the simulation
        stopSimulation();
    }
    public void stopSimulation() {
        isSimRunning = false;

    }
    public boolean isSimulationRunning() {
        return isSimRunning;
    }

    // informative
    ArrayList<Component> getComponents() {
        return components;
    }
    ArrayList<Node> getNodes() {
        return nodes;
    }
    ArrayList<Wire> getWires() {
        return wires;
    }
    void setComponents(ArrayList<Component> comps) {
        components = comps;
        for (Component comp : components)
            if (comp.isIndependent())
                constants.add(comp);
    }
    void setNodes(ArrayList<Node> nodes) {
        this.nodes = nodes;
    }
    void setWires(ArrayList<Wire> wires) {
        this.wires = wires;
    }
    void connectEverything() {

    }
    // grid
    String getGridWidth() {
        return Integer.toString(control.getGridWidth());
    }
    String getGridHeight() {
        return Integer.toString(control.getGridHeight());
    }
    void setGridDimensions(int w, int h) {
        control.setGridDimensions(w, h);
    }

}
