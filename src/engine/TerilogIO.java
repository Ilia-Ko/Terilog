package engine;

import engine.components.memory.flat.Flat;
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
import java.io.*;
import java.util.StringTokenizer;

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
        transformer.setOutputProperty(OutputKeys.MEDIA_TYPE, "Terilog circuit layout");
    }

    public void saveTLG(File outputFile) throws TransformerException {
        Document doc = builder.newDocument();
        Element root = control.getCircuit().writeCircuitToXML(doc);
        root.appendChild(control.writeGridToXML(doc));
        doc.appendChild(root);
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(outputFile);
        transformer.transform(source, result);
    }
    public void loadTLG(File inputFile) throws IOException, SAXException {
        Document dom = builder.parse(inputFile);
        Element root = dom.getDocumentElement();
        control.getCircuit().destroy();
        control.setCircuit(new Circuit(control, root));
        control.readGridFromXML(root);
    }

    public static void saveFlatData(long[] data, int length, int unitSize, File file) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));

        for (int i = 0; i < length; i++) {
            LogicLevel[] value = Flat.decode(data[i], unitSize);
            StringBuilder number = new StringBuilder();
            for (int j = unitSize - 1; j >= 0; j--) number.append(value[j].getDigitCharacter());
            writer.write(number.toString());
            if ((i + 1) % 27 == 0) writer.newLine();
            else writer.write(' ');
        }
        writer.flush();
        writer.close();
    }
    public static void loadFlatData(long[] data, int length, int unitSize, File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));

        int index = 0;
        while (reader.ready() && index < length) {
            StringTokenizer tokenizer = new StringTokenizer(reader.readLine());
            while (tokenizer.hasMoreTokens() && index < length) {
                String token = tokenizer.nextToken();
                LogicLevel[] trits = new LogicLevel[token.length()];
                for (int i = 0; i < trits.length; i++) trits[i] = LogicLevel.parseDigit(token.charAt(i));
                data[index++] = Flat.encode(trits, unitSize);
            }
        }
        reader.close();

        for (; index < length; index++) data[index] = 0L;
    }

}
