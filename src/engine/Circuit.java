package engine;

import gui.control.ControlMain;

import java.util.ArrayList;

public class Circuit {

    private static final String DEF_NAME = "untitled";
    private static final int SIM_DEPTH = 100;

    // construction logic
    private String name;
    private ControlMain control;
    private ArrayList<Wire> wires;

    // simulation logic
    private boolean isSimRunning;
    private ArrayList<Component> components, constants;
    private ArrayList<Node> nodes;

    public Circuit(ControlMain control) {
        // construction logic
        name = DEF_NAME;
        this.control = control;
        wires = new ArrayList<>();

        // simulation logic
        isSimRunning = false;
        components = new ArrayList<>();
        constants = new ArrayList<>();
        nodes = new ArrayList<>();
    }

    // rendering
    public void renderAll() {
        for (Wire w : wires) w.render();
        for (Component c : components) c.render();
    }
    public void updateGridPeriod(double period) {
        for (Component comp : components) comp.setGridPeriod(period);
        for (Wire wire : wires) wire.setGridPeriod(period);
    }

    // construction logic
    public void add(Wire wire) {
        // check whether 'w' should be connected to existing wires
        boolean isNewNode = true;
        for (Wire old : wires)
            for (int[] p : wire.getPoints())
                if (old.inside(p[0], p[1])) {
                    wire.connect(old.getNode());
                    isNewNode = false;
                    break;
                }

        // create new node if needed (if 'w' does not participate in any existing ones)
        if (isNewNode) {
            Node node = new Node();
            nodes.add(node);
            wire.connect(node);
        }

        // check whether 'w' should be connected to existing components
        for (Component c : components)
            for (Component.Pin pin : c.getPins())
                if (wire.inside(pin.getX(), pin.getY()))
                    c.connect(wire.getNode(), pin);

        wires.add(wire);
    }
    public void add(Component comp) {
        if (comp.isIndependent()) constants.add(comp);

        // check whether 'c' should be connected to existing nodes (their wires)
        for (Component.Pin pin : comp.getPins())
            for (Wire wire : wires)
                if (wire.inside(pin.getX(), pin.getY())) {
                    comp.connect(wire.getNode(), pin);
                    break;
                }

        components.add(comp);
    }

    // simulation logic
    public void startSimulation() {
        isSimRunning = true;
        ArrayList<Node> unstable = new ArrayList<>();

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
            renderAll();

            // destabilizing pass: propagate changes from unstable nodes to affected components
            ArrayList<Node> tmp = new ArrayList<>(unstable);
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
    String getName() {
        return name;
    }
    void setName(String name) {
        this.name = name;
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
