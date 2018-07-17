package engine;

import engine.components.Component;
import engine.components.Pin;
import engine.components.arithmetic.Counter;
import engine.components.arithmetic.FullAdder;
import engine.components.arithmetic.HalfAdder;
import engine.components.logic.one_arg.NTI;
import engine.components.logic.one_arg.PTI;
import engine.components.logic.one_arg.STI;
import engine.components.logic.two_arg.*;
import engine.components.lumped.*;
import engine.components.memory.Trigger;
import engine.components.memory.flat.RAM_6_6;
import engine.components.memory.linear.Dword;
import engine.components.memory.linear.Triplet;
import engine.components.memory.linear.Tryte;
import engine.components.memory.linear.Word;
import engine.components.mosfets.HardN;
import engine.components.mosfets.HardP;
import engine.components.mosfets.SoftN;
import engine.components.mosfets.SoftP;
import engine.connectivity.Node;
import engine.connectivity.Selectable;
import engine.wires.Wire;
import gui.control.ControlMain;
import javafx.application.Platform;
import javafx.beans.property.*;
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
    // After that, we can begin simulation. Simulation is
    // a controllable sequence of step() calls. Every step
    // makes components to compute their outputs and nodes
    // to transfer these outputs forward.

    private static final String DEF_NAME = "Untitled";
    private static final int DEF_SIM_DEPTH = 60;

    // construction
    private StringProperty name;
    private HashSet<Component> components;
    private HashSet<Clock> clocks;
    private ArrayList<Wire> wires;
    private HashSet<Pin> pins;
    private HashSet<Selectable> selected;
    private boolean hasSelected, isSelMoving;
    // simulation
    private HashSet<Node> nodes;
    private boolean needsParsing;
    private boolean isStableStateFound;
    private boolean isSimRunning;
    private IntegerProperty maxSimDepth;
    private DoubleProperty simFrequency;

    public Circuit() {
        name = new SimpleStringProperty(DEF_NAME);
        maxSimDepth = new SimpleIntegerProperty(DEF_SIM_DEPTH);
        simFrequency = new SimpleDoubleProperty(Clock.DEF_FREQUENCY);

        // flags
        needsParsing = true;
        isStableStateFound = false;
        hasSelected = false;
        isSelMoving = false;
        isSimRunning = false;

        // arrays
        components = new HashSet<>();
        clocks = new HashSet<>();
        wires = new ArrayList<>();
        pins = new HashSet<>();
        nodes = new HashSet<>();
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
                    case "clock":
                        add(new Clock(control, comp));
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
                    case "counter":
                        add(new Counter(control, comp));
                        break;
                    // memory
                    case "trigger":
                        add(new Trigger(control, comp));
                        break;
                    // memory.linear
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
                    // memory.flat
                    case "ram_6_6":
                        add(new RAM_6_6(control, comp));
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
        if (comp instanceof Clock) clocks.add((Clock) comp);
        pins.addAll(comp.getPins());
        needsParsing = true;
    }
    public void del(Component comp) {
        components.remove(comp);
        if (comp instanceof Clock) clocks.remove(comp);
        needsParsing = true;
    }
    public void destroy() {
        name.setValue(DEF_NAME);
        wires.forEach(wire -> wire.delete(false));
        wires.clear();
        components.forEach(comp -> comp.delete(false));
        components.clear();
    }

    // selection
    public void sel(Rectangle sel) {
        selected = new HashSet<>();
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
    public void selCopy() {
        if (hasSelected) {
            HashSet<Selectable> temp = new HashSet<>();
            selected.forEach(sel -> {
                temp.add(sel.copy());
                sel.breakSelection();
            });
            selected.clear();
            selected = temp;
            selMove();
        }
    }

    // simulation and connectivity
    private void parse() {
        // reset nodes
        nodes = new HashSet<>();
        needsParsing = true; // if nodes were eliminated, parsing is needed
        isStableStateFound = false;

        // reset wires & comps
        wires.forEach(wire -> wire.reset(true));
        components.forEach(comp -> comp.reset(true));

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
            if (wire.isNodeFree()) {
                Node node = new Node();
                nodes.add(node);
                wire.nodify(node);
            }

        // parsing.stage2.b: nodify pins - O(m)
        for (Pin pin : pins)
            if (pin.isNodeFree()) {
                Node node = new Node();
                nodes.add(node);
                pin.nodify(node);
            }

        // Nice system of nodes is ready to simulation!
        needsParsing = false;
    }
    private void clear() {
        // reset nodes
        nodes.forEach(Node::reset);
    }
    private void step() {
        isStableStateFound = true;
        components.forEach(Component::simulate);
        nodes.forEach(node -> isStableStateFound &= node.isStable());
    }

    // flags
    public boolean isReallyBig() {
        return wires.size() + pins.size() > 20736;
    }
    public boolean hasToBeParsed() {
        return needsParsing;
    }
    public boolean wasFinished() {
        return isStableStateFound;
    }

    // interaction
    public void doParse() {
        parse();
    }
    public void doClear() {
        clear();
    }
    public void doStepInto() {
        if (needsParsing) parse();
        step();
    }
    public void doStepOver() {
        if (needsParsing) parse();

        // simulate clock
        for (Clock clock : clocks) clock.nextImpulse();
        isStableStateFound = false;

        // simulate
        int attempts = 0;
        while (attempts++ <= maxSimDepth.get() && !isStableStateFound)
            step();
    }
    public void doRun(Runnable ifNotCatchingUp) {
        if (needsParsing) parse();

        new Thread(() -> {
            isSimRunning = true;
            isStableStateFound = true;
            // Wait time is computed in millis.
            // It is 4 times shorter than a period, because Clock must change its signal four times a period.
            // Finally, 2 millis are spared for 'catching up check'
            long wait = Math.round(1000.0 / simFrequency.get() / 4.0) - 2L;

            while (isSimRunning && isStableStateFound) { // simulation stops too if circuit cannot be stabilized
                // measure time spent to stabilization
                long time = System.currentTimeMillis();
                final BooleanProperty isReady = new SimpleBooleanProperty(); // stabilization flag

                // simulate (try to stabilize the circuit)
                Platform.runLater(() -> {
                    isReady.setValue(false);
                    isStableStateFound = false;
                    int attempts = 0;
                    for (Clock clock : clocks) clock.nextImpulse();
                    while (isSimRunning && attempts++ < maxSimDepth.get() && !isStableStateFound) step();
                    isReady.setValue(true); // mark stabilization finished
                });

                // wait till the next oscillation
                while (System.currentTimeMillis() - time < wait);

                // check whether stabilization process is catching up with clock
                if (!isReady.get()) {
                    isSimRunning = false;
                    while (!isReady.get()); // if it isn't - stop the simulation
                    isStableStateFound = false;
                    Platform.runLater(ifNotCatchingUp);
                }
            }
        }).start();
    }
    public void doStop() {
        isSimRunning = false;
    }
    public String getStatistics() {
        Summary summary = new Summary();
        components.forEach(comp -> comp.itIsAFinalCountdown(summary));
        return summary.makeFinalCountdown();
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
    public DoubleProperty simFrequencyProperty() {
        return simFrequency;
    }

    public class Summary {

        public static final int P_CH = 1, N_CH = -1, HARD = 2, SOFT = 1;

        private int numHardP, numHardN, numSoftP, numSoftN;
        private int numDiodes, numRes;
        private int numComps, numWires;
        private int numInputs, numOutputs;
        private int numVoltPos, numVoltNil, numVoltNeg;
        private int numRAMs;
        private boolean needsClock;
        private String info;

        private Summary() {
            numComps = components.size();
            numWires = wires.size();
            needsClock = false;
            info = "";
        }

        public void addMOSFET(int type, int channel, int qty) {
            if (type == HARD) {
                if (channel == P_CH) numHardP += qty;
                else if (channel == N_CH) numHardN += qty;
            } else if (type == SOFT) {
                if (channel == P_CH) numSoftP += qty;
                else if (channel == N_CH) numSoftN += qty;
            }
        }
        public void addDiode(int qty) {
            numDiodes += qty;
        }
        public void addResistor(int qty) {
            numRes += qty;
        }
        public void takeClockIntoAccount() {
            needsClock = true;
        }
        public void takeRAMIntoAccount() {
            numRAMs++;
        }
        public void addInput(LogicLevel sig, int qty) {
            numInputs += qty;
            if (sig == LogicLevel.POS) numVoltPos += qty;
            else if (sig == LogicLevel.NIL) numVoltNil += qty;
            else if (sig == LogicLevel.NEG) numVoltNeg += qty;
        }
        public void addOutput() {
            numOutputs++;
        }

        private String makeFinalCountdown() {
            // general quantities
            info += String.format("Circuit '%s' summary:\n", name.get());
            info += String.format("Components total:\t%d\n", numComps);
            info += String.format("Hard P:\t\t\t%d\n", numHardP);
            info += String.format("Hard N:\t\t\t%d\n", numHardN);
            info += String.format("Soft P:\t\t\t%d\n", numSoftP);
            info += String.format("Soft N:\t\t\t%d\n", numSoftN);
            info += String.format("Diodes:\t\t\t%d\n", numDiodes);
            info += String.format("Resistors:\t\t%d\n", numRes);
            info += String.format("Wires:\t\t\t%d\n", numWires);
            info += String.format("Inputs:\t\t\t%d\n", numInputs);
            info += String.format("Outputs:\t\t%d\n", numOutputs);
            info += needsClock ? "The circuit uses clock.\n" : "The circuit does not use clock.\n";
            info += numRAMs > 0 ? String.format("The circuit uses %d RAM block(s).\n", numRAMs) : "The circuit does not use RAM.\n";
            // percentage
            double numVolts = numVoltNeg + numVoltNil + numVoltPos;
            double perNeg = 100.0 * numVoltNeg / numVolts;
            double perNil = 100.0 * numVoltNil / numVolts;
            double perPos = 100.0 - perNeg - perNil;
            double imbalance = 100.0 * Math.abs(numVoltPos - numVoltNeg) / (double) numVoltNil;
            info += String.format("NEG DC Load:\t%.1f%%\n", perNeg);
            info += String.format("NIL DC Load:\t%.1f%%\n", perNil);
            info += String.format("POS DC Load:\t%.1f%%\n", perPos);
            info += String.format("DC Imbalance:\t%.3f%%\n", imbalance);
            return info;
        }

    }

}
