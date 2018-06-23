package engine;

import engine.components.Component;
import engine.components.Pin;
import engine.components.lumped.Diode;
import engine.components.lumped.Indicator;
import engine.components.lumped.Reconciliator;
import engine.components.lumped.Voltage;
import engine.components.mosfets.HardN;
import engine.components.mosfets.HardP;
import engine.components.mosfets.SoftN;
import engine.components.mosfets.SoftP;
import engine.connectivity.Connectible;
import engine.connectivity.Node;
import engine.wires.Wire;
import gui.control.ControlMain;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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

    private static final String DEF_NAME = "Untitled";

    private String name;
    private ArrayList<Component> components, entries;
    private ArrayList<Wire> wires;
    private ArrayList<Pin> pins;
    private BooleanProperty simRunProperty; // flag for threading

    public Circuit() {
        name = DEF_NAME;
        simRunProperty = new SimpleBooleanProperty(false);
        components = new ArrayList<>();
        entries = new ArrayList<>();
        wires = new ArrayList<>();
        pins = new ArrayList<>();
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
                        add(new HardN(control, comp));
                        break;
                    case "hardp":
                        add(new HardP(control, comp));
                        break;
                    case "softn":
                        add(new SoftN(control, comp));
                        break;
                    case "softp":
                        add(new SoftP(control, comp));
                        break;
                    case "diode":
                        add(new Diode(control, comp));
                        break;
                    case "indicator":
                        add(new Indicator(control, comp));
                        break;
                    case "reconciliator":
                        add(new Reconciliator(control, comp));
                        break;
                    case "voltage":
                        add(new Voltage(control, comp));
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
        pins.addAll(comp.getPins());
    }
    public void del(Component comp) {
        components.remove(comp);
        if (comp.isEntryPoint()) entries.remove(comp);
    }

    // connectivity
    public void parse() {
        // reset everything
        wires.forEach(Connectible::reset);
        pins.forEach(Connectible::reset);

        // parsing.stage1.a: searching for connections between wires
        int len = wires.size();
        for (int i = 0; i < len; i++)
            for (int j = i + 1; j < len; j++)
                wires.get(i).inspect(wires.get(j));

        // parsing.stage1.b: searching for connections between wires and pins
        for (Pin pin : pins)
            for (Wire wire : wires)
                pin.inspect(wire);

        // parsing.stage2.a: nodify wires
        for (Wire wire : wires)
            if (wire.isNodeFree())
                wire.nodify(new Node());

        // parsing.stage2.b: nodify pins
        for (Pin pin : pins)
            if (pin.isNodeFree())
                pin.nodify(new Node());

        // Nice system of nodes is ready to simulation!
    }
    private void simulate() {
        simRunProperty.setValue(true);
        HashSet<Node> unstable = new HashSet<>();

        // entry points
        for (Component entry : entries) unstable.addAll(entry.simulate());

        // continuous simulation
        while (simRunProperty.get() && unstable.size() > 0) {
            HashSet<Node> tmp = new HashSet<>(unstable);
            unstable.clear();
            for (Node node : tmp) unstable.addAll(node.simulate());
        }

        simRunProperty.setValue(false);
    }

    // simulation
    public void startSimulation() {
        Platform.runLater(this::simulate);
    }
    public void stopSimulation() {
        simRunProperty.setValue(false);
    }
    public BooleanProperty getSimRunProperty() {
        return simRunProperty;
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
    public String getName() {
        return name;
    }

}
