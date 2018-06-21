package engine;

import gui.control.ControlMain;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

public class TerilogIO {

    private ControlMain control;
    private DocumentBuilder builder;
    private Transformer transformer;

    public TerilogIO(ControlMain control) throws ParserConfigurationException, TransformerConfigurationException {
        this.control = control;

        // init XML engine
        DocumentBuilderFactory f1 = DocumentBuilderFactory.newInstance();
        builder = f1.newDocumentBuilder();
        TransformerFactory f2 = TransformerFactory.newInstance();
        transformer = f2.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    }

    public void saveTLG(File outputFile) throws TransformerException {
        Document doc = builder.newDocument();
        doc.appendChild(control.writeGridToXML(doc));
        doc.appendChild(control.getCircuit().writeCircuitToXML(doc));
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputFile);
        transformer.transform(source, result);
    }
    public void loadTLG(File inputFile) throws IOException, SAXException {
        // roots
        Document dom = builder.parse(inputFile);
        Element doc = dom.getDocumentElement();
        control.readGridFromXML(doc);
        control.setCircuit(new Circuit(control, doc));
    }

}
