package engine;

import engine.components.Component;
import engine.components.Pin;
import engine.components.arithmetic.FullAdder;
import engine.components.arithmetic.HalfAdder;
import engine.components.logic.one_arg.NTI;
import engine.components.logic.one_arg.PTI;
import engine.components.logic.one_arg.STI;
import engine.components.logic.two_arg.*;
import engine.components.lumped.Diode;
import engine.components.lumped.Indicator;
import engine.components.lumped.Reconciliator;
import engine.components.lumped.Voltage;
import engine.components.memory.*;
import engine.components.mosfets.HardN;
import engine.components.mosfets.HardP;
import engine.components.mosfets.SoftN;
import engine.components.mosfets.SoftP;
import engine.connectivity.Node;
import engine.connectivity.Selectable;
import engine.wires.Wire;
import gui.control.ControlMain;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.shape.Rectangle;
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
    private static final int DEF_SIM_DEPTH = 60;

    // construction
    private StringProperty name;
    private ArrayList<Component> components, entries;
    private ArrayList<Wire> wires;
    private ArrayList<Pin> pins;
    private ArrayList<Selectable> selected;
    private boolean hasSelected, isSelMoving;
    // simulation
    private HashSet<Node> unstable;
    private boolean needsParsing;
    private boolean needsEntry;
    private boolean isFinished;
    private IntegerProperty maxSimDepth;

    public Circuit() {
        name = new SimpleStringProperty(DEF_NAME);
        maxSimDepth = new SimpleIntegerProperty(DEF_SIM_DEPTH);

        // flags
        needsParsing = true;
        needsEntry = true;
        hasSelected = false;
        isSelMoving = false;

        // arrays
        components = new ArrayList<>();
        entries = new ArrayList<>();
        wires = new ArrayList<>();
        pins = new ArrayList<>();
    }
    public Circuit(ControlMain control, Element c) {
        this();
        name.setValue(c.getAttribute("name"));
        try {
            int depth = Integer.parseInt(c.getAttribute("depth"));
            if (depth != 0) maxSimDepth.setValue(depth);
        } catch (NumberFormatException e) { maxSimDepth.setValue(DEF_SIM_DEPTH); }
        NodeList list;

        // create components
        list = c.getElementsByTagName("comp");
        if (list != null)
            for (int i = 0; i < list.getLength(); i++) {
                Element comp = (Element) list.item(i);
                String attrClass = comp.getAttribute("class");
                switch (attrClass) {
                    // mosfets
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
                    // lumped
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
                    // logic.one_arg
                    case "nti":
                        add(new NTI(control, comp));
                        break;
                    case "sti":
                        add(new STI(control, comp));
                        break;
                    case "pti":
                        add(new PTI(control, comp));
                        break;
                    // logic.two_arg
                    case "nand":
                        add(new NAND(control, comp));
                        break;
                    case "nor":
                        add(new NOR(control, comp));
                        break;
                    case "ncon":
                        add(new NCON(control, comp));
                        break;
                    case "nany":
                        add(new NANY(control, comp));
                        break;
                    case "okey":
                        add(new OKEY(control, comp));
                        break;
                    case "ckey":
                        add(new CKEY(control, comp));
                        break;
                    // arithmetic
                    case "halfadder":
                        add(new HalfAdder(control, comp));
                        break;
                    case "fulladder":
                        add(new FullAdder(control, comp));
                        break;
                    // memory
                    case "trigger":
                        add(new Trigger(control, comp));
                        break;
                    case "triplet":
                        add(new Triplet(control, comp));
                        break;
                    case "tryte":
                        add(new Tryte(control, comp));
                        break;
                    case "word":
                        add(new Word(control, comp));
                        break;
                    case "dword":
                        add(new Dword(control, comp));
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
        needsParsing = true;
    }
    public void del(Wire wire) {
        wires.remove(wire);
        needsParsing = true;
    }
    public void add(Component comp) {
        components.add(comp);
        if (comp.isEntryPoint()) entries.add(comp);
        pins.addAll(comp.getPins());
        needsParsing = true;
    }
    public void del(Component comp) {
        components.remove(comp);
        if (comp.isEntryPoint()) entries.remove(comp);
        needsParsing = true;
    }
    void destroy() {
        name.setValue(DEF_NAME);
        wires.forEach(wire -> wire.delete(false));
        wires.clear();
        components.forEach(comp -> comp.delete(false));
        components.clear();
    }

    // selection
    public void sel(Rectangle sel) {
        selected = new ArrayList<>();
        components.forEach(comp -> {
            if (comp.checkSelection(sel))
                selected.add(comp);
        });
        wires.forEach(wire -> {
            if (wire.checkSelection(sel))
                selected.add(wire);
        });
        hasSelected = selected.size() > 0;
    }
    public void unsel() {
        if (isSelMoving) selDel();
        else if (hasSelected) selected.forEach(Selectable::breakSelection);
        hasSelected = false;
    }
    public boolean hasSelectedItems() {
        return hasSelected;
    }
    public boolean isSelectionMoving() {
        return isSelMoving;
    }
    public void selMove() {
        if (hasSelected) {
            selected.forEach(Selectable::move);
            isSelMoving = true;
        }
    }
    public void selStop() {
        if (hasSelected && isSelMoving) {
            isSelMoving = false;
            hasSelected = false;
            selected.forEach(Selectable::stop);
        }
    }
    public void selDel() {
        if (hasSelected) {
            selected.forEach(Selectable::delete);
            hasSelected = false;
        }
    }

    // simulation and connectivity
    private void reset() {
        // reset nodes

        // reset wires
        wires.forEach(wire -> wire.reset(true));

        // reset components
        components.forEach(comp -> comp.reset(true));

        // if nodes were eliminated, parsing is needed
        needsParsing = true;
        needsEntry = true;
        unstable = new HashSet<>();
    }
    private void parse() {
        // reset
        reset();

        // parsing.stage1.a: searching for connections between wires - O(1/2 * n^2)
        int len = wires.size();
        for (int i = 0; i < len; i++)
            for (int j = i + 1; j < len; j++)
                wires.get(i).inspect(wires.get(j));

        // parsing.stage1.b: searching for connections between wires and pins - O(m * n)
        for (Pin pin : pins)
            for (Wire wire : wires)
                pin.inspect(wire);

        // parsing.stage2.a: nodify wires - O(n)
        for (Wire wire : wires)
            if (wire.isNodeFree())
                wire.nodify(new Node());

        // parsing.stage2.b: nodify pins - O(m)
        for (Pin pin : pins)
            if (pin.isNodeFree())
                pin.nodify(new Node());

        // Nice system of nodes is ready to simulation!
        needsParsing = false;
        unstable = new HashSet<>();
        needsEntry = true;
    }
    private void begin() {
        for (Component entry : entries) unstable.addAll(entry.simulate());
        needsEntry = false;
    }
    private void step() {
        HashSet<Node> tmp = new HashSet<>(unstable);
        unstable.clear();
        for (Node node : tmp) unstable.addAll(node.simulate());
    }

    // flags
    public boolean isReallyBig() {
        return wires.size() + pins.size() > 20736;
    }
    public boolean hasToBeParsed() {
        return needsParsing;
    }
    public boolean wasFinished() {
        return isFinished;
    }

    // interaction
    public void addUnstable(Node node) {
        if (unstable != null) unstable.add(node);
    }
    public void doReset() {
        reset();
    }
    public void doParse() {
        parse();
    }
    public void doStepInto() {
        if (needsParsing) parse();
        if (needsEntry) begin();
        else step();
    }
    public void doStepOver() {
        // prepare
        if (needsParsing) parse();
        if (needsEntry) begin();

        // simulate
        int attempts = 0;
        while (attempts++ <= maxSimDepth.get() && unstable.size() > 0)
            step();

        // analyze state
        isFinished = attempts < maxSimDepth.get();
    }

    // xml info
    Element writeCircuitToXML(Document doc) {
        Element c = doc.createElement("circuit");
        c.setAttribute("name", name.get());
        c.setAttribute("depth", Integer.toString(maxSimDepth.get()));

        for (Component comp : components)
            c.appendChild(comp.writeXML(doc));

        for (Wire wire : wires)
            c.appendChild(wire.writeXML(doc));

        return c;
    }
    public StringProperty nameProperty() {
        return name;
    }
    public IntegerProperty simDepthProperty() {
        return maxSimDepth;
    }

}
