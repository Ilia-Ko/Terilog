package engine;

import engine.interfaces.Informative;
import engine.transistors.HardN;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TerilogIO {

    // tags
    private static final String TAG_CIRCUIT = "circuit";
    private static final String TAG_LOGIC = "logic";
    private static final String TAG_GRAPH = "graph";
    private static final String TAG_COMP = "comp";
    private static final String TAG_PIN = "pin";
    private static final String TAG_NODE = "node";
    private static final String TAG_WIRE = "wire";
    private static final String TAG_GRID = "grid";
    // attributes
    private static final String ATTR_ID = "id";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_CLASS = "class";
    private static final String ATTR_TYPE = "type";
    private static final String ATTR_NODE = "node";
    private static final String ATTR_W = "w";
    private static final String ATTR_H = "h";
    private static final String ATTR_X = "x";
    private static final String ATTR_Y = "y";
    private static final String ATTR_X1 = "x1";
    private static final String ATTR_Y1 = "y1";
    private static final String ATTR_HF = "hf";
    private static final String ATTR_ROTATION = "rot";
    private static final String ATTR_MIRROR_H = "mh";
    private static final String ATTR_MIRROR_V = "mv";

    private Circuit circuit;
    private DocumentBuilder builder;
    private Transformer transformer;

    public TerilogIO(Circuit circuit) throws ParserConfigurationException, TransformerConfigurationException {
        this.circuit = circuit;

        // init XML engine
        DocumentBuilderFactory f1 = DocumentBuilderFactory.newInstance();
        builder = f1.newDocumentBuilder();
        TransformerFactory f2 = TransformerFactory.newInstance();
        transformer = f2.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    }

    public void loadTLG(File inputFile) throws IOException, SAXException {
        // prepare informatives
        ArrayList<Component> comps = new ArrayList<>();
        ArrayList<Node> nodes = new ArrayList<>();
        ArrayList<Wire> wires = new ArrayList<>();

        // roots
        Document dom = builder.parse(inputFile);
        Element doc = dom.getDocumentElement();
        Element root, logic, graph;
        NodeList list;

        // extract <circuit>
        list = doc.getElementsByTagName(TAG_CIRCUIT);
        if (list != null && list.getLength() > 0) {
            root = (Element) list.item(0);
            circuit.setName(root.getAttribute(ATTR_NAME));
        } else {
            System.out.printf("ERROR: missing <circuit> tag in XML '%s'.", inputFile);
            return;
        }

        // extract <logic>
        list = root.getElementsByTagName(TAG_LOGIC);
        if (list != null && list.getLength() > 0)
            logic = (Element) list.item(0);
        else {
            System.out.printf("ERROR: missing <logic> tag in XML '%s'.", inputFile);
            return;
        }

        // extract <graph>
        list = root.getElementsByTagName(TAG_GRAPH);
        if (list != null && list.getLength() > 0)
            graph = (Element) list.item(0);
        else {
            System.out.printf("ERROR: missing <graph> tag in XML '%s'.", inputFile);
            return;
        }

        // graph.wires - make list of wires, fully initialize them
        list = graph.getElementsByTagName(TAG_WIRE);
        if (list != null) for (int i = 0; i < list.getLength(); i++) {
            Element w = (Element) list.item(i);
            Wire wire = parseWire(w);
            if (wire != null) wires.add(wire);
        }
        circuit.setWires(wires);

        // logic.nodes - make list of nodes, fully initialize them
        list = logic.getElementsByTagName(TAG_NODE);
        if (list != null) for (int i = 0; i < list.getLength(); i++) {
            Element n = (Element) list.item(i);
            Node node = parseNode(n);
            if (node != null) nodes.add(node);
        }
        circuit.setNodes(nodes);

        // logic.components - make list of components, connect them, but do not initialize graphics yet
        list = logic.getElementsByTagName(TAG_COMP);
        if (list != null) for (int i = 0; i < list.getLength(); i++) {
            Element c = (Element) list.item(i);
            Component comp = parseComponent(c);
            if (comp != null) comps.add(comp);
        }
        circuit.setComponents(comps);

        // graph.components - initialize graphics for every component
        list = graph.getElementsByTagName(TAG_COMP);
        if (list != null) for (int i = 0; i < list.getLength(); i++) {
            Element c = (Element) list.item(i);
            parseComponentProperties(c);
        }

        // graph.grid
        list = graph.getElementsByTagName(TAG_GRID);
        if (list != null && list.getLength() > 0) {
            Element g = (Element) list.item(0);
            int w = toInt(g.getAttribute(ATTR_W));
            int h = toInt(g.getAttribute(ATTR_H));
            circuit.setGridDimensions(w, h);
        } else System.out.println("WARNING: tag <grid> not found inside <graph>.");
    }
    public void saveTLG(File outputFile) throws TransformerException {
        // prepare informatives
        Component[] components = (Component[]) circuit.getComponents().toArray();
        Node[] nodes = (Node[]) circuit.getNodes().toArray();
        Wire[] wires = (Wire[]) circuit.getWires().toArray();
        genIDs(components);
        genIDs(nodes);
        genIDs(wires);

        // roots
        Document doc = builder.newDocument();
        Element root = doc.createElement(TAG_CIRCUIT);
        Element logic = doc.createElement(TAG_LOGIC);
        Element graph = doc.createElement(TAG_GRAPH);

        // logic.components
        for (Component comp : components) {
            Element c = doc.createElement(TAG_COMP);
            c.setAttribute(ATTR_CLASS, comp.getAttrClassName());
            c.setAttribute(ATTR_ID, comp.getID());
            for (Component.Pin pin : comp.getPins()) {
                Element p = doc.createElement(TAG_PIN);
                p.setAttribute(ATTR_TYPE, pin.getName());
                p.setAttribute(ATTR_NODE, pin.getNode().getID());
                c.appendChild(p);
            }
            logic.appendChild(c);
        }

        // logic.nodes
        for (Node node : nodes) {
            Element n = doc.createElement(TAG_NODE);
            n.setAttribute(ATTR_ID, node.getID());
            for (Wire wire : node.getWires()) {
                Element w = doc.createElement(TAG_WIRE);
                w.setAttribute(ATTR_ID, wire.getID());
                n.appendChild(w);
            }
            logic.appendChild(n);
        }

        // graph.grid
        Element g = doc.createElement(TAG_GRID);
        g.setAttribute(ATTR_W, this.circuit.getGridWidth());
        g.setAttribute(ATTR_H, this.circuit.getGridHeight());
        graph.appendChild(g);

        // graph.components
        for (Component comp : components) {
            Element c = doc.createElement(TAG_COMP);
            c.setAttribute(ATTR_ID, comp.getID());
            c.setAttribute(ATTR_X, comp.getX());
            c.setAttribute(ATTR_Y, comp.getY());
            c.setAttribute(ATTR_ROTATION, comp.getRotation());
            c.setAttribute(ATTR_MIRROR_H, comp.getMirrorH());
            c.setAttribute(ATTR_MIRROR_V, comp.getMirrorV());
            graph.appendChild(c);
        }

        // graph.wires
        for (Wire wire : wires) {
            Element w = doc.createElement(TAG_WIRE);
            w.setAttribute(ATTR_ID, wire.getID());
            w.setAttribute(ATTR_X, wire.getX());
            w.setAttribute(ATTR_Y, wire.getY());
            w.setAttribute(ATTR_X1, wire.getDX());
            w.setAttribute(ATTR_Y1, wire.getDY());
            w.setAttribute(ATTR_HF, wire.getHorizontalFirst());
            graph.appendChild(w);
        }

        root.appendChild(logic);
        root.appendChild(graph);
        root.setAttribute(ATTR_NAME, circuit.getName());
        doc.appendChild(root);

        // write xml
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputFile);
        transformer.transform(source, result);
    }

    // helpers
    private void genIDs(Informative[] list) {
        int i = 0;
        for (Informative inf : list)
            inf.setID(String.format("%s-%d", inf.getPrefixID(), i++));
    }
    private Wire findWireByID(String id) {
        for (Wire w : circuit.getWires())
            if (w.getID().equals(id))
                return w;
        return null;
    }
    private Node findNodeByID(String id) {
        for (Node n : circuit.getNodes())
            if (n.getID().equals(id))
                return n;
        return null;
    }
    private Component findComponentByID(String id) {
        for (Component c : circuit.getComponents())
            if (c.getID().equals(id))
                return c;
        return null;
    }
    // parsing
    private Wire parseWire(Element w) {
        // create a wire with ID
        Wire wire = new Wire();
        String id = w.getAttribute(ATTR_ID);
        if (id.isEmpty()) {
            System.out.println("WARNING: the ID of a wire is not specified. Ignoring this wire.");
            return null;
        }
        wire.setID(id);

        // set position
        try {
            int x  = toInt(w.getAttribute(ATTR_X));
            int y  = toInt(w.getAttribute(ATTR_Y));
            int x1 = toInt(w.getAttribute(ATTR_X1));
            int y1 = toInt(w.getAttribute(ATTR_Y1));
            wire.setPos(x, y);
            wire.layoutAgain(x1, y1);
        } catch (NumberFormatException e) {
            System.out.printf("WARNING: the coordinate(s) of wire '%s' malformed. Ignoring this wire.\n", id);
            return null;
        }

        // set form
        boolean hf = Boolean.parseBoolean(w.getAttribute(ATTR_HF));
        if (!hf) wire.flip();

        return wire;
    }
    private Node parseNode(Element n) {
        // create a node with ID
        Node node = new Node();
        String id = n.getAttribute(ATTR_ID);
        if (id.isEmpty()) {
            System.out.println("WARNING: the ID of a node is not specified. Ignoring this node.");
            return null;
        }
        node.setID(id);

        // add wires
        NodeList list = n.getElementsByTagName(TAG_WIRE);
        if (list == null || list.getLength() == 0)
            System.out.printf("WARNING: empty node '%s'.", id);
        else for (int i = 0; i < list.getLength(); i++) {
            Element w = (Element) list.item(i);
            id = w.getAttribute(ATTR_ID);
            if (id.isEmpty())
                System.out.println("WARNING: wire without ID cannot be included into a node.");
            else {
                Wire wire = findWireByID(id);
                if (wire == null) {
                    System.out.printf("WARNING: wire '%s' (mentioned inside a node '%s') missing in the <graph> section.", id, node.getID());
                    wire = new Wire();
                    wire.setID(id);
                }
                node.addWire(wire);
            }
        }

        return node;
    }
    private Component parseComponent(Element c) {
        // create a component of a certain class - I don't know how to do it in 'genuine OOP' style!
        Component comp;
        String className = c.getAttribute(ATTR_CLASS);
        if (className.equals(Component.ATTR_NAME_OF_HARD_N))
            comp = new HardN();
        // else if ...
        else {
            System.out.printf("WARNING: component with attribute 'class=\"%s\"' not recognized.", className);
            return null;
        }

        // set id
        String id = c.getAttribute(ATTR_ID);
        if (id.isEmpty()) {
            System.out.println("WARNING: component id is empty. Ignoring this component.");
            return null;
        }
        comp.setID(id);

        // connect pins
        NodeList list = c.getElementsByTagName(TAG_PIN);
        if (list != null)
            for (int i = 0; i < list.getLength(); i++) {
                Element p = (Element) list.item(i);
                String type = p.getAttribute(ATTR_TYPE);
                if (type.isEmpty()) {
                    System.out.println("WARNING: pin type is empty. Ignoring this pin.");
                    continue;
                }
                Node node = findNodeByID(p.getAttribute(ATTR_NODE));
                comp.connect(node, comp.getPinByName(type));
            }

        return comp;
    }
    private void parseComponentProperties(Element c) {
        String id = c.getAttribute(ATTR_ID);
        Component comp = findComponentByID(id);
        if (comp == null)
            System.out.printf("WARNING: component '%s' (defined in <graph> section) is not present in <logic> section. Ignoring this component.\n", id);
        else {
            // set position
            try {
                int x = toInt(c.getAttribute(ATTR_X));
                int y = toInt(c.getAttribute(ATTR_Y));
                comp.setPos(x, y);
            } catch (NumberFormatException e) {
                System.out.printf("WARNING: the coordinate(s) of component '%s' malformed. Cannot place this component properly.\n", id);
            }

            // set rotation and mirroring
            comp.setRotation(c.getAttribute(ATTR_ROTATION));
            comp.setMirroring(c.getAttribute(ATTR_MIRROR_H), c.getAttribute(ATTR_MIRROR_V));
        }
    }

    // utilities
    private static int toInt(String s) {
        return Integer.parseInt(s);
    }

}
