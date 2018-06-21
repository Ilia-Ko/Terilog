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
import javafx.application.Platform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashSet;

public class Circuit {

    // The circuit is just a couple of arrays of wires and
    // components, disconnected from each other until
    // user starts the simulation process. Before the
    // simulation the circuit has to be parsed in order to
    // make components understand with whom they are
    // connected and to whom they will send signals and
    // from whom they will receive signals. Parsing is
    // done by geometrically locating points of contact
    // between wires and pins of components, wires with
    // each other. From the point of view of simulation
    // there is no matter in how wires are placed, the
    // only important thing is - who is connected with
    // whom. So that it would be inefficient to transfer
    // signals via wires, node instance is created instead.
    // Node is a system of wires that connects some pins
    // together. Node is responsible for signal transfer
    // and for colouring wires according to signal value.
    // How to create a proper system of nodes? It is the
    // main goal of parsing. Initially, every wire and pin
    // represents separate node. When a connection between
    // two objects is located (geometrically), their nodes
    // are joined. Finally, we get a complete circuit graph -
    // the minimal and quite efficient system of nodes.
    // After that, we can begin simulation. The components,
    // whose outputs does not depend on inputs become entry
    // points of the simulation. They update their output
    // signals at their output nodes and these nodes invoke
    // those components, whose inputs are connected to them.

    private static final String DEF_NAME = "untitled";

    private String name;
    private ArrayList<Component> components, entries;
    private ArrayList<Wire> wires;
    private ArrayList<Node> nodes; // simulation only
    private boolean isSimRunning; // flag for threading

    public Circuit() {
        name = DEF_NAME;
        isSimRunning = false;
        components = new ArrayList<>();
        entries = new ArrayList<>();
        wires = new ArrayList<>();
    }
    public Circuit(ControlMain control, Element c) {
        this();
        name = c.getAttribute("name");
        NodeList list;

        // create components
        list = c.getElementsByTagName("comp");
        if (list != null)
            for (int i = 0; i < list.getLength(); i++) {
                Element comp = (Element) list.item(i);
                String attrClass = comp.getAttribute("class");
                switch (attrClass) {
                    case "hardn":
                        components.add(new HardN(control, comp));
                        break;
                    case "hardp":
                        components.add(new HardP(control, comp));
                        break;
                    case "softn":
                        components.add(new SoftN(control, comp));
                        break;
                    case "softp":
                        components.add(new SoftP(control, comp));
                        break;
                    case "diode":
                        components.add(new Diode(control, comp));
                        break;
                    case "indicator":
                        components.add(new Indicator(control, comp));
                        break;
                    case "reconciliator":
                        components.add(new Reconciliator(control, comp));
                        break;
                    case "voltage":
                        Voltage voltage = new Voltage(control, comp);
                        entries.add(voltage);
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

    // construction
    public void add(Wire wire) {
        wires.add(wire);
    }
    public void del(Wire wire) {
        wires.remove(wire);
    }
    public void add(Component comp) {
        components.add(comp);
        if (comp.isEntryPoint()) entries.add(comp);
    }
    public void del(Component comp) {
        components.remove(comp);
        if (comp.isEntryPoint()) entries.remove(comp);
    }

    // connectivity and simulation
    public void parse() {
        // set separate node for every Connectible
        components.forEach(Component::nodify);
        wires.forEach(Wire::nodify);

        // search for connections between wires - O(1/2 * n^2), n = wires.size()
        for (int i = 0; i < wires.size(); i++) {
            Wire wire = wires.get(i);
            for (int j = i + 1; j < wires.size(); j++)
                wire.inspect(wires.get(j));
        }

        // search for connections between wires and pins - O(n * m), m = components.size()
        for (Wire wire : wires)
            for (Component comp : components)
                comp.inspect(wire);

        // gather nodes all over the circuit
        nodes = new ArrayList<>();
        wires.forEach(wire -> nodes.add(wire.gather()));
        components.forEach(comp -> nodes.addAll(comp.gather()));

        // Nice system of nodes is ready to simulation!
    }
    private void simulate() {
        isSimRunning = true;
        HashSet<Node> unstable = new HashSet<>();

        // entry points
        for (Component entry : entries) unstable.addAll(entry.simulate());

        // continuous simulation
        while (isSimRunning && unstable.size() > 0) {
            HashSet<Node> tmp = new HashSet<>(unstable);
            unstable.clear();
            for (Node node : tmp) unstable.addAll(node.simulate());
        }

        isSimRunning = false;
    }

    public void startSimulation() {
        Platform.runLater(this::simulate);
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
