package engine;

import engine.interfaces.Informative;
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

    public void loadTLG(String path) throws IOException, SAXException {
        // prepare informatives
        ArrayList<Component> comps = new ArrayList<Component>();
        ArrayList<Node> nodes = new ArrayList<Node>();
        ArrayList<Wire> wires = new ArrayList<Wire>();

        // roots
        Document dom = builder.parse(path);
        Element doc = dom.getDocumentElement();
        Element root, logic, graph = null;
        NodeList list;
        // extract <circuit>
        list = doc.getElementsByTagName(TAG_CIRCUIT);
        if (list != null && list.getLength() > 0) root = (Element) list.item(0);
        else {
            System.out.printf("ERROR: missing <circuit> tag in XML '%s'.", path);
            return;
        }
        // extract <logic>
        list = root.getElementsByTagName(TAG_LOGIC);
        if (list != null && list.getLength() > 0) logic = (Element) list.item(0);
        else {
            System.out.printf("ERROR: missing <logic> tag in XML '%s'.", path);
            return;
        }
        // extract <graph> if present
        list = root.getElementsByTagName(TAG_GRAPH);
        if (list != null && list.getLength() > 0) graph = (Element) list.item(0);
        else System.out.printf("WARNING: tag <graph> not found in XML '%s'.", path);

        // logic.components
        list = logic.getElementsByTagName(TAG_COMP);
        if (list != null && list.getLength() > 0)
            for (int i = 0; i < list.getLength(); i++) {
                Element c = (Element) list.item(i);
                comps.add(parseComponent(c));
            }

        // logic.nodes
        list = logic.getElementsByTagName(TAG_NODE);
        if (list != null && list.getLength() > 0)
            for (int i = 0; i < list.getLength(); i++) {
                Element n = (Element) list.item(i);
                nodes.add(parseNode(n));
            }

        if (graph != null) {
            // graph.grid
            list = graph.getElementsByTagName(TAG_GRID);
            if (list != null && list.getLength() > 0) {
                Element g = (Element) list.item(0);
                int w = toInt(g.getAttribute(ATTR_W));
                int h = toInt(g.getAttribute(ATTR_H));
                circuit.setGridDimensions(w, h);
            }

            // graph.components
            list = graph.getElementsByTagName(TAG_COMP);
            if (list != null && list.getLength() > 0)
                for (int i = 0; i < list.getLength(); i++) {
                    Element c = (Element) list.item(i);

                }

            // graph.wires
            list = graph.getElementsByTagName(TAG_WIRE);
            if (list != null && list.getLength() > 0)
                for (int i = 0; i < list.getLength(); i++) {
                    Element w = (Element) list.item(i);

                }
        }

        // construct circuit
        circuit.setComponents(comps);
        circuit.setNodes(nodes);
        circuit.setWires(wires);
        circuit.connectEverything();
    }
    public void saveTLG(String path) throws IOException, TransformerException {
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
            c.setAttribute(ATTR_CLASS, comp.getClassName());
            c.setAttribute(ATTR_ID, comp.getID());
            for (Component.Pin pin : comp.getPins()) {
                Element p = doc.createElement(TAG_PIN);
                p.setAttribute(ATTR_TYPE, comp.getPinName(pin));
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
            w.setAttribute(ATTR_X1, wire.getX1());
            w.setAttribute(ATTR_Y1, wire.getY1());
            w.setAttribute(ATTR_HF, wire.getHorizontalFirst());
            graph.appendChild(w);
        }

        root.appendChild(logic);
        root.appendChild(graph);
        doc.appendChild(root);

        // write xml
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(path));
        transformer.transform(source, result);
    }

    // helpers
    private void genIDs(Informative[] list) {
        int i = 0;
        for (Informative inf : list)
            inf.setID(String.format("%s-%d", inf.getPrefixID(), i++));
    }
    private Informative getInfByID(Informative[] list, String id) {
        for (Informative inf : list)
            if (inf.getID().equals(id))
                return inf;
        return null;
    }
    // parsing
    private Component parseComponent(Element c) {

    }
    private Node parseNode(Element n) {

    }

    // utilities
    private static int toInt(String s) {
        return Integer.parseInt(s);
    }

}
