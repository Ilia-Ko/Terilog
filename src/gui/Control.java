package gui;

import engine.Circuit;
import engine.interfaces.Renderable;
import engine.transistors.HardN;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.awt.*;

@SuppressWarnings("unused")
class Control {

    // defaults
    private static final double DEF_GRID_PERIOD = 0.004;
    private static final double DEF_GRID_POINT_THICKNESS = DEF_GRID_PERIOD / 20.0;
    private static final double DEF_LINE_WIDTH = DEF_GRID_PERIOD / 10.0;
    private static final double DEF_FLYING_OPACITY = 0.5;

    // colours
    private static final Color COL_CLEAR    = Color.rgb(219, 255, 244);
    private static final Color COL_GRID     = Color.rgb(0, 0, 0);

    private static Stage stage;
    private static Canvas field;

    // general logic
    private static Circuit circuit;
    private static double w, h; // field width and height
    private static double p; // field grid period
    // adding a component
    private static Robot robot;
    private static double mouseX, mouseY;
    private static boolean isMouseEmpty; // whether a mouse holds a component
    private static Renderable toBeInserted;
    private static Canvas flyingCanvas;
    private static OnObjectCreated onCreate;

    static void init(Stage stage, Canvas field) {
        try {
            robot = new Robot();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        Control.stage = stage;
        isMouseEmpty = true;

        // init field
        Control.field = field;
        Control.field.setOnMouseMoved(event -> {
            if (!isMouseEmpty) {
                flyingCanvas.setLayoutX(event.getX() - flyingCanvas.getWidth() / 2.0);
                flyingCanvas.setLayoutY(event.getY() - flyingCanvas.getHeight() / 2.0);
            }
            if (event.isShiftDown()) {
                double x = event.getX();
                double y = event.getY();
                x = Math.round(x / p) * p;
                y = Math.round(y / p) * p;
                robot.mouseMove(Main.rnd(x), Main.rnd(y));
            }
        });
        Control.field.setOnMouseClicked(event -> {
            if (!isMouseEmpty) {
                double x = event.getX();
                double y = event.getY();
                x = Math.round(x / p) * p;
                y = Math.round(y / p) * p;
                circuit.add(onCreate.createObject(x, y));
            }
        });
        Control.field.setOnKeyPressed(event -> {
            if (!isMouseEmpty && event.getCode() == KeyCode.ESCAPE) {
                isMouseEmpty = true;
                toBeInserted = null;
                flyingCanvas = null;
                onCreate = null;
            }
        });

        circuit = new Circuit();
        w = field.getWidth();
        h = field.getHeight();
        p = w * DEF_GRID_PERIOD;
    }

    // field
    static void onFieldClicked(Event event) {

    }
    static void clearField() {
        GraphicsContext gc = field.getGraphicsContext2D();

        // clear background
        gc.setFill(COL_CLEAR);
        gc.fillRect(0.0, 0.0, w, h);

        // draw grid points
        int numHorizontalGridPoints = Main.rnd(w / p);
        int numVerticalGridPoints = Main.rnd(h / p);
        double r = w * DEF_GRID_POINT_THICKNESS;
        if (r < 1.0) r = 1.0;
        gc.setFill(COL_GRID);
        gc.setLineWidth(r);
        for (int i = 0; i < numHorizontalGridPoints; i++)
            for (int j = 0; j < numVerticalGridPoints; j++)
                gc.fillOval(i * p, j * p, r, r);
    }

    // inserting a component
    private static void begin(Renderable newObject) {
        isMouseEmpty = false;
        toBeInserted = newObject;
        flyingCanvas = new Canvas(newObject.getWidth() * p, newObject.getHeight() * p);
        flyingCanvas.setOpacity(0.5);
        flyingCanvas.setVisible(true);
        newObject.render(flyingCanvas.getGraphicsContext2D());
    }

    enum TheAction {

        // Menu.File --> 6 items
        Open("Open TLG", new KeyCodeCombination(KeyCode.O, KeyCodeCombination.CONTROL_DOWN)) {
            @Override
            void commit(Event event) {

            }
        },
        Save("Save TLG", new KeyCodeCombination(KeyCode.S, KeyCodeCombination.CONTROL_DOWN)) {
            @Override
            void commit(Event event) {

            }
        },
        SaveAs("Save as ...", new KeyCodeCombination(KeyCode.S, KeyCodeCombination.SHIFT_DOWN)) {
            @Override
            void commit(Event event) {

            }
        },
        _Sep0(Main.STR_SEPARATOR, KeyCombination.NO_MATCH) {
            @Override
            void commit(Event event) {}
        },
        FullScreen("Toggle fullscreen", new KeyCodeCombination(KeyCode.F11)) {
            @Override
            void commit(Event event) {
                stage.setFullScreen(!stage.isFullScreen());
            }
        },
        Quit("Quit", new KeyCodeCombination(KeyCode.Q, KeyCodeCombination.CONTROL_DOWN)) {
            @Override
            void commit(Event event) {
                Platform.exit();
                System.exit(0);
            }
        },

        // Menu.Add --> 8 items
        Hard_N("Hard N MOSFET", new KeyCodeCombination(KeyCode.N)) {
            @Override
            void commit(Event event) {
                onCreate = (x, y) -> {
                    HardN hardN = new HardN(true);
                    hardN.setPos(x, y);
                    return hardN;
                };
                begin(new HardN(false));
            }
        },
        HardP("Hard P MOSFET", new KeyCodeCombination(KeyCode.P)) {
            @Override
            void commit(Event event) {

            }
        },
        SoftN("Soft N MOSFET", new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN)) {
            @Override
            void commit(Event event) {

            }
        },
        SoftP("Soft P MOSFET", new KeyCodeCombination(KeyCode.P, KeyCodeCombination.CONTROL_DOWN)) {
            @Override
            void commit(Event event) {

            }
        },
        _Sep1(Main.STR_SEPARATOR, KeyCombination.NO_MATCH) {
            @Override
            void commit(Event event) {}
        },
        Wire("Wire", new KeyCodeCombination(KeyCode.W)) {
            @Override
            void commit(Event event) {

            }
        },
        Voltage("DC voltage source", new KeyCodeCombination(KeyCode.V)) {
            @Override
            void commit(Event event) {

            }
        },
        Indicator("Indicator", new KeyCodeCombination(KeyCode.I)) {
            @Override
            void commit(Event event) {

            }
        },

        // Menu.About --> 2 items
        Help("Help", new KeyCodeCombination(KeyCode.H, KeyCodeCombination.CONTROL_DOWN)) {
            @Override
            void commit(Event event) {

            }
        },
        Credits("Credits", KeyCodeCombination.NO_MATCH) {
            @Override
            void commit(Event event) {

            }
        };

        private KeyCombination combo;
        private String description;

        TheAction(String description, KeyCombination combo) {
            this.description = description;
            this.combo = combo;
        }

        KeyCombination getCombo() {
            return combo;
        }
        String getDescription() {
            return combo.getName() + "\t" + description ;
        }

        abstract void commit(Event event);

    }

    private interface OnObjectCreated {

        Renderable createObject(double x, double y);

    }

}
