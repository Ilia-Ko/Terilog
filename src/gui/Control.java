package gui;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

@SuppressWarnings("unused")
class Control {

    // default grid
    private static final double DEF_GRID_PERIOD = 0.004;
    private static final double DEF_GRID_POINT_THICKNESS = DEF_GRID_PERIOD / 20.0;
    // default lines
    private static final double DEF_LINE_WIDTH = DEF_GRID_PERIOD / 10.0;

    // colours
    private static final Color COL_CLEAR    = Color.rgb(219, 255, 244);
    private static final Color COL_GRID     = Color.rgb(0, 0, 0);

    private static Stage stage;
    private static Canvas field;

    // general logic
    private static double w, h; // field width and height
    private static double p; // field grid period
    private static boolean isMouseEmpty; // whether a mouse holds a component

    static void init(Stage stage, Canvas field) {
        Control.stage = stage;
        Control.field = field;

        w = field.getWidth();
        h = field.getHeight();
        p = w * DEF_GRID_PERIOD;
    }

    // Field
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
        HardN("Hard N MOSFET", new KeyCodeCombination(KeyCode.N)) {
            @Override
            void commit(Event event) {

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

}
