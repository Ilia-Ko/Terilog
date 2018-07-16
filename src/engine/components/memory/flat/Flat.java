package engine.components.memory.flat;

import engine.Circuit;
import engine.LogicLevel;
import engine.TerilogIO;
import engine.components.Component;
import engine.components.Pin;
import gui.Main;
import gui.control.ControlMain;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import static engine.LogicLevel.*;

public abstract class Flat extends Component {

    // memory
    private int addressLen, unitLength, memorySize;
    private long[] data;
    // pins
    private Pin control, clock;
    private Pin[] address, read, write;
    // io
    private File dataFile;

    // initialization
    Flat(ControlMain control, int addressLength, int unitSize) {
        super(control);
        addressLen = addressLength;
        unitLength = unitSize;
        memorySize = (int) Math.pow(3.0, addressLength);

        data = new long[memorySize];
        dataFile = null;

        getPins().addAll(makePins());
    }
    Flat(ControlMain control, Element data, int addressLength, int unitSize) {
        this(control, addressLength, unitSize);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/memory/flat/" + getClass().getSimpleName().toLowerCase() + ".fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    private HashSet<Pin> makePins() {
        HashSet<Pin> pins = new HashSet<>();

        control = new Pin(this, true, unitLength + 1, 1);
        clock = new Pin(this, true, unitLength + 1, addressLen);
        pins.add(control);
        pins.add(clock);

        address = new Pin[addressLen];
        for (int i = 0; i < addressLen; i++) {
            address[i] = new Pin(this, true, 0, i + 1);
            pins.add(address[i]);
        }

        write = new Pin[unitLength];
        read = new Pin[unitLength];
        for (int i = 0; i < unitLength; i++) {
            write[i] = new Pin(this, true, i + 1, 0);
            read[i] = new Pin(this, false, i + 1, addressLen + 1);
            pins.add(write[i]);
            pins.add(read[i]);
        }

        return pins;
    }
    @Override protected ContextMenu buildContextMenu() {
        MenuItem itemLoad = new MenuItem("Load data");
        itemLoad.setOnAction(action -> {
            // configure file chooser
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Open data file");
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));

            // get file
            File file = chooser.showOpenDialog(getControl().getStage());
            if (file != null && file.exists()) {
                dataFile = file;
                try {
                    TerilogIO.loadFlatData(data, memorySize, unitLength, dataFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    getControl().makeAlert(Alert.AlertType.WARNING, "Flat memory", "Failed to load data from file.").showAndWait();
                }
            }
        });

        MenuItem itemSave = new MenuItem("Save data");
        itemSave.setOnAction(action -> {
            // configure file chooser
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Open data file");
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));

            // get file
            File file = chooser.showSaveDialog(getControl().getStage());
            if (file != null) {
                try {
                    if (file.exists() || file.createNewFile()) {
                        dataFile = file;
                        TerilogIO.saveFlatData(data, memorySize, unitLength, dataFile);
                    } else {
                        getControl().makeAlert(Alert.AlertType.WARNING, "Flat memory", "File not found.").showAndWait();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    getControl().makeAlert(Alert.AlertType.WARNING, "Flat memory", "Failed to save data to file.").showAndWait();
                }
            }
        });

        ContextMenu menu = super.buildContextMenu();
        menu.getItems().add(0, itemLoad);
        menu.getItems().add(0, itemSave);
        return menu;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel ctrl = control.get();
        LogicLevel clck = clock.get();
        LogicLevel[] addr = new LogicLevel[addressLen];
        LogicLevel[] in = new LogicLevel[unitLength];

        for (int i = 0; i < addressLen; i++) addr[i] = address[i].get();
        int address = (int) encode(addr, addressLen);

        for (int i = 0; i < unitLength; i++) in[i] = write[i].get();
        long input = encode(in, unitLength);

        for (Pin pin : read) pin.put(ZZZ);
        if (ctrl == ERR || clck == ERR)
            for (Pin pin : read) pin.put(ERR);
        else if (clck == POS) {
            if (ctrl == POS) data[address] = input;
            else if (ctrl == NEG) {
                LogicLevel[] out = decode(data[address], unitLength);
                for (int i = 0; i < unitLength; i++) read[i].put(out[i]);
            }
        }
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {

    }

    // xml info
    @Override public Element writeXML(Document doc) {
        Element f = super.writeXML(doc);
        if (dataFile != null) f.setAttribute("data", dataFile.getAbsolutePath());
        return f;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        String dataAttr = comp.getAttribute("data");
        if (!dataAttr.isEmpty()) {
            dataFile = new File(dataAttr);
            if (!dataFile.exists()) {
                dataFile = null;
                System.out.printf("WARNING: data file '%s' does not exist.", dataAttr);
            } else try {
                TerilogIO.loadFlatData(data, memorySize, unitLength, dataFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // utils
    public static long encode(LogicLevel[] digits, int size) {
        assert digits.length >= size;

        long res = 0L;
        for (int i = 0; i < size; i++) {
            res += (long) digits[i].volts();
            res *= 3L;
        }

        return res;
    }
    public static LogicLevel[] decode(long value, int size) {
        LogicLevel[] trits = new LogicLevel[size];
        boolean neg = false;

        // check sign
        if (value < 0L) {
            neg = true;
            value *= -1L;
        }

        // decode to non-symmetric unbalanced TNS
        int length = (int) Math.floor(Math.log(value) / Math.log(3.0));
        int[] digits = new int[length + 1]; // +1 for SBTNS
        for (int i = 0; i <= length; i++) {
            digits[i] = (int) (value % 3L);
            value /= 3L;
        }

        // decode to SBTNS
        for (int i = 0; i < length; i++) {
            if (digits[i] == 2) {
                trits[i] = NEG;
                digits[i+1]++;
            } else if (digits[i] == 3) {
                trits[i] = NIL;
                digits[i+1]++;
            } else {
                trits[i] = LogicLevel.parseValue(digits[i]);
            }
        }
        trits[length] = LogicLevel.parseValue(digits[length]);

        // fill with zeroes
        for (int i = length + 1; i < size; i++) trits[i] = NIL;

        // negate if needed
        if (neg)
            for (int i = 0; i <= length; i++)
                trits[i] = LogicLevel.parseValue(-trits[i].volts());

        return trits;
    }

}
