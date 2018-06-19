package engine;

import engine.components.Component;
import engine.components.lumped.Diode;
import engine.components.lumped.Indicator;
import engine.components.lumped.Reconciliator;
import engine.components.lumped.Voltage;
import engine.components.mosfets.HardN;
import engine.components.mosfets.HardP;
import engine.components.mosfets.SoftN;
import engine.components.mosfets.SoftP;
import engine.connectivity.Node;
import engine.connectivity.Wire;
import gui.control.ControlMain;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;

public class Circuit {

    private static final String DEF_NAME = "untitled";
    private static final int SIM_DEPTH = 100;

    // construction logic
    private String name;
    private ArrayList<Wire> wires;

    // simulation logic
    private boolean isSimRunning;
    private ArrayList<Component> components, constants;
    private ArrayList<Node> nodes;

    public Circuit(ControlMain control) {
        name = DEF_NAME;
        isSimRunning = false;
        components = new ArrayList<>();
        constants = new ArrayList<>();
        nodes = new ArrayList<>();
        wires = new ArrayList<>();
    }
    public Circuit(ControlMain control, Element c) {
        this(control);
        name = c.getAttribute("name");
        NodeList list;

        // create components
        list = c.getElementsByTagName("comp");
        if (list != null)
            for (int i = 0; i < list.getLength(); i++) {
                Element comp = (Element) list.item(i);
                String attrClass = comp.getAttribute("class");
                switch (attrClass) {
                    case HardN.ATTR_CLASS:
                        components.add(new HardN(control, comp));
                        break;
                    case HardP.ATTR_CLASS:
                        components.add(new HardP(control, comp));
                        break;
                    case SoftN.ATTR_CLASS:
                        components.add(new SoftN(control, comp));
                        break;
                    case SoftP.ATTR_CLASS:
                        components.add(new SoftP(control, comp));
                        break;
                    case Diode.ATTR_CLASS:
                        components.add(new Diode(control, comp));
                        break;
                    case Indicator.ATTR_CLASS:
                        components.add(new Indicator(control, comp));
                        break;
                    case Reconciliator.ATTR_CLASS:
                        components.add(new Reconciliator(control, comp));
                        break;
                    case Voltage.ATTR_CLASS:
                        Voltage voltage = new Voltage(control, comp);
                        constants.add(voltage);
                        components.add(voltage);
                        break;
                    default:
                        System.out.printf("WARNING: unknown component of class %s.\n", attrClass);
                }
            }

        // create wires
        list = c.getElementsByTagName("wire");
        if (list != null)
            for (int i = 0; i < list.getLength(); i++)
                wires.add(new Wire(control, (Element) list.item(i)));
    }

    // construction logic
    // TODO: establish connection ideology
    public void add(Wire wire) {
//        // check whether 'w' should be connected to existing wires
//        boolean isNewNode = true;
//        for (Wire old : wires)
//            for (int[] p : wire.getPoints())
//                if (old.inside(p[0], p[1])) {
//                    wire.connect(old.getNode());
//                    isNewNode = false;
//                    break;
//                }
//
//        // create new node if needed (if 'w' does not participate in any existing ones)
//        if (isNewNode) {
//            Node node = new Node();
//            nodes.add(node);
//            wire.connect(node);
//        }
//
//        // check whether 'w' should be connected to existing components
//        for (Component comp : components)
//            for (Component.Pin pin : comp.getPins())
//                if (wire.inside(pin.getX(), pin.getY()))
//                    pin.connect(wire);
//
//        wires.add(wire);
    }
    public void del(Wire wire) {
//        // disconnect wire from node
//        Node node = wire.getNode();
//        node.delWire(wire);
//
//        // disconnect component from node (previously connected by this wire)
//        for (Component comp : node.getComponents())
//            comp.disconnect(wire);
    }
    public void add(Component comp) {
//        if (comp.isIndependent()) constants.add(comp);
//
//        // check whether 'comp' should be connected to existing nodes (their wires)
//        for (Component.Pin pin : comp.getPins())
//            for (Wire wire : wires)
//                if (wire.inside(pin.getX(), pin.getY())) {
//                    pin.connect(wire);
//                    break;
//                }
//
//        components.add(comp);
    }
    public void del(Component comp) {
//        components.remove(comp);
//        comp.disconnect();
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
//        for (Component constant : constants) unstable.addAll(constant.simulate()); // 'entry point'
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

    // xml info
    Element writeCircuitToXML(Document doc) {
        Element c = doc.createElement("circuit");
        c.setAttribute("name", name);

        for (Component comp : components)
            c.appendChild(comp.writeXML(doc));

        for (Wire wire : wires)
            c.appendChild(wire.writeXML(doc));

        return c;
    }

}
