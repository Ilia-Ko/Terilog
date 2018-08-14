package engine.components.memory;

import engine.Circuit;
import engine.LogicLevel;
import engine.TerilogIO;
import engine.components.BusComponent;
import engine.components.Component;
import engine.components.Pin;
import engine.connectivity.Selectable;
import gui.Main;
import gui.control.ControlMain;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import static engine.LogicLevel.NEG;
import static engine.LogicLevel.POS;

public class Flat extends Component {

    // memory
    private IntegerProperty addrCap, memCap;
    private int memorySize;
    private long[] data;
    // pins
    private Pin control, fill, clock;
    private Pin addr, in, out;
    // io
    private File dataFile;

    // initialization
    public Flat(ControlMain control) {
        super(control);
        addrCap = new SimpleIntegerProperty(6);
        memCap = new SimpleIntegerProperty(6);
        memorySize = (int) Math.pow(3.0, addrCap.doubleValue());

        data = new long[memorySize];
        dataFile = null;

        // label
        Label lbl = (Label) getRoot().lookup("#name");
        lbl.setText("RAM\n6x6");

        addrCap.addListener((observable, oldValue, newValue) -> {
            memorySize = (int) Math.pow(3.0, newValue.doubleValue());
            data = new long[memorySize];
            lbl.setText(String.format("RAM\n%dx%d", newValue.intValue(), memCap.get()));
            getPins().clear();
            getPins().addAll(initPins());
        });
        memCap.addListener((observable, oldValue, newValue) -> {
            lbl.setText(String.format("RAM\n%dx%d", addrCap.get(), newValue.intValue()));
            getPins().clear();
            getPins().addAll(initPins());
        });
    }
    public Flat(ControlMain control, Element data) {
        this(control);
        confirm();
        readXML(data);
    }
    @Override protected Pane loadContent() {
        try {
            String location = "view/components/memory/Flat.fxml";
            return FXMLLoader.load(Main.class.getResource(location));
        } catch (IOException e) {
            e.printStackTrace();
            return new Pane();
        }
    }
    @Override protected HashSet<Pin> initPins() {
        int capAddr = (addrCap == null) ? 6 : addrCap.get();
        int capMem = (memCap == null) ? 6 : memCap.get();

        control = new Pin(this, true, 1, 4, 1);
        fill = new Pin(this, true, 1, 4, 2);
        clock = new Pin(this, true, 1, 4, 3);
        addr = new Pin(this, true, capAddr, 0, 2);
        in = new Pin(this, true, capMem, 2, 0, false);
        out = new Pin(this, false, capMem, 2, 4, false);

        HashSet<Pin> pins = new HashSet<>();
        pins.add(control);
        pins.add(fill);
        pins.add(clock);
        pins.add(addr);
        pins.add(in);
        pins.add(out);
        return pins;
    }
    @Override protected ContextMenu buildContextMenu() {
        // capacity
        Menu menuAddrCap = BusComponent.makeCapMenu("Set address size", addrCap);
        Menu menuMemCap = BusComponent.makeCapMenu("Set data unit size", memCap);

        // load data
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
                    TerilogIO.loadFlatData(data, memorySize, memCap.get(), dataFile);
                } catch (IOException e) {
                    e.printStackTrace();
                    getControl().makeAlert(Alert.AlertType.WARNING, "Flat memory", "Failed to load data from file.").showAndWait();
                }
            }
        });
        // save data
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
                        TerilogIO.saveFlatData(data, memorySize, memCap.get(), dataFile);
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
        menu.getItems().add(0, menuAddrCap);
        menu.getItems().add(1, menuMemCap);
        menu.getItems().add(2, itemLoad);
        menu.getItems().add(3, itemSave);
        return menu;
    }

    // simulation
    @Override public void simulate() {
        LogicLevel ctrl = control.get()[0];
        LogicLevel[] base = new LogicLevel[addrCap.get()];
        Arrays.fill(base, NEG);
        int addr = (int) (encode(this.addr.get(), addrCap.get()) - encode(base, addrCap.get()));

        if (clock.get()[0] == POS) {
            if (ctrl == POS) {
                data[addr] = encode(in.get(), memCap.get());
            } else if (ctrl == NEG) {
                Arrays.fill(in.get(), fill.get()[0]);
                data[addr] = encode(in.get(), memCap.get());
            }
        }
        out.put(decode(data[addr], memCap.get()));
    }
    @Override public void itIsAFinalCountdown(Circuit.Summary summary) {
        summary.takeRAMIntoAccount();
    }

    // xml info
    @Override public Element writeXML(Document doc) {
        Element f = super.writeXML(doc);
        f.setAttribute("addrCap", Integer.toString(addrCap.get()));
        f.setAttribute("memCap", Integer.toString(memCap.get()));
        if (dataFile != null) f.setAttribute("data", dataFile.getAbsolutePath());
        return f;
    }
    @Override protected void readXML(Element comp) {
        super.readXML(comp);
        // capacity
        String capAddrAttr = comp.getAttribute("addrCap");
        String capMemAttr = comp.getAttribute("memCap");
        try {
            int capAddr = Integer.parseInt(capAddrAttr);
            int capMem = Integer.parseInt(capMemAttr);
            memCap.setValue(capMem);
            addrCap.setValue(capAddr);
        } catch (NumberFormatException e) {
            System.out.printf("WARNING: invalid RAM mode: '%sx%s'. Using default mode 6x6.\n", capAddrAttr, capMemAttr);
        }

        // data
        String dataAttr = comp.getAttribute("data");
        if (!dataAttr.isEmpty()) {
            dataFile = new File(dataAttr);
            if (!dataFile.exists()) {
                dataFile = null;
                System.out.printf("WARNING: data file '%s' does not exist.", dataAttr);
            } else try {
                TerilogIO.loadFlatData(data, memorySize, memCap.get(), dataFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override public Selectable copy() {
        Flat copy = (Flat) super.copy();
        copy.memCap.setValue(memCap.get());
        copy.addrCap.setValue(addrCap.get());
        copy.dataFile = null;
        return copy;
    }

    // utils
    public static long encode(LogicLevel[] digits, int size) {
        assert digits.length >= size;
        long res = 0L;
        for (int i = size - 1; i >= 0; i--) {
            res *= 3L;
            res += (long) digits[i].volts();
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
        // decode to SBTNS
        int[] digits = new int[size];
        for (int i = 0; i < size; i++) {
            digits[i] += (int) (value % 3L);
            value /= 3L;
            if (digits[i] > 1) {
                digits[i] -= 3;
                digits[i+1]++;
            }
            if (neg) digits[i] = -digits[i];
            trits[i] = LogicLevel.parseValue(digits[i]);
        }
        return trits;
    }

}
