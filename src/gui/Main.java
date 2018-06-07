package gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    // general dimensions
    private static final double G = 0.5 * (1.0 + Math.sqrt(5.0));
    private static final double DEF_FONT_SIZE = 1.0 / 60.0;

    // MENU structure
    private static final String STR_SEPARATOR = "#separator";
    private static final String[][] STR_MENUS = new String[][] {
            {"File",
                    "Ctrl+O\tOpen",
                    "Ctrl+S\tSave",
                    "Shft+S\tSave as...",
                    STR_SEPARATOR,
                    "F11   \tToggle fullscreen",
                    "Ctrl+Q\tExit"},
            {"Add",
                    "N     \tHard N MOSFET",
                    "P     \tHard P MOSFET",
                    "Ctrl+N\tSoft N MOSFET",
                    "Ctrl+P\tSoft P MOSFET",
                    STR_SEPARATOR,
                    "W     \tWire",
                    "V     \tDC Voltage",
                    "I     \tIndicator"},
            {"About",
                    "Help",
                    "Credits"}
    };

    // event handlers
    private static EventHandler[] MENU_EVENTS;

    private Controller control;

    // dimensions
    private double maxWidth, maxHeight;
    private double defWidth, defHeight;
    private double minWidth, minHeight;
    private String defFont;

    // gui components
    private Stage stage;
    private VBox root;

    // initialization
    private void initDimensions() {
        // take delicate care of DPI: (I hate when some apps look awful on Retina displays :)
        Rectangle2D rect = Screen.getPrimary().getBounds();
        maxWidth    = rect.getWidth();
        maxHeight   = rect.getHeight();

        // adjust font:
        int defFontSize = rnd(maxHeight * DEF_FONT_SIZE);
        defFont = "-fx-font: normal " + Integer.toString(defFontSize) + "px monospace";

        // set default dimensions
        minHeight = defFontSize * 5.0;
        minWidth = minHeight * G;
        defWidth = maxWidth / G;
        defHeight = maxHeight / G;
    }
    private void initHandlers() {
        control = new Controller(stage);

        // init menu controls
        int numItems = 0;
        for (String[] STR_MENU : STR_MENUS) numItems += STR_MENU.length;
        MENU_EVENTS = new EventHandler[numItems];
        int i = 0;

        // Menu.File
        MENU_EVENTS[i++] = control::onMenuOpenClicked;
        MENU_EVENTS[i++] = control::onMenuSaveClicked;
        MENU_EVENTS[i++] = control::onMenuSaveAsClicked;
        MENU_EVENTS[i++] = null; // separator
        MENU_EVENTS[i++] = control::onMenuToggleFullscreenClicked;
        MENU_EVENTS[i++] = control::onMenuExitClicked;

        // Menu.Add
        MENU_EVENTS[i++] = control::onMenuHardNClicked;
        MENU_EVENTS[i++] = control::onMenuHardPClicked;
        MENU_EVENTS[i++] = control::onMenuSoftNClicked;
        MENU_EVENTS[i++] = control::onMenuSoftPClicked;
        MENU_EVENTS[i++] = null; // separator
        MENU_EVENTS[i++] = control::onMenuWireClicked;
        MENU_EVENTS[i++] = control::onMenuVoltageClicked;
        MENU_EVENTS[i++] = control::onMenuIndicatorClicked;

        // Menu.About
        MENU_EVENTS[i++] = control::onMenuHelpClicked;
        MENU_EVENTS[i  ] = control::onMenuCreditsClicked;
    }

    // dynamic gui
    private void constructGUI() {
        // build menu:
        MenuBar menuBar = new MenuBar();
        for (int i = 0; i < STR_MENUS.length; i++) {
            Menu menu = new Menu(STR_MENUS[i][0]);
            for (int j = 1; j < STR_MENUS[i].length; j++) {
                MenuItem item;
                if (STR_MENUS[i][j].equals(STR_SEPARATOR)) {
                    item = new SeparatorMenuItem();
                } else {
                    item = new MenuItem(STR_MENUS[i][j]);
                    item.setOnAction(MENU_EVENTS[i]);
                    item.setStyle(defFont);
                }
                menu.getItems().add(item);
            }
            menuBar.getMenus().add(menu);
        }

        // build field:
        Canvas field = new Canvas(maxWidth, maxHeight);
        field.setStyle(defFont);
        field.setCursor(Cursor.CROSSHAIR);
        field.setOnMouseClicked(control::onFieldClicked);
        control.setField(field);
        control.clearField();

        // wrap field with scroll pane:
        ScrollPane scroll = new ScrollPane(field);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // combine everything together:
        root = new VBox(menuBar, scroll);
        root.setStyle(defFont);
        root.setMinWidth(minWidth);
        root.setMinHeight(minHeight);
        root.setPrefWidth(defHeight);
        root.setPrefHeight(defHeight);
        root.setMaxWidth(maxWidth);
        root.setMaxHeight(maxWidth);
    }
    private void setupStage() {
        // adjust window geometry
        stage.setResizable(true);
        stage.setMinWidth(minWidth);
        stage.setMinHeight(minHeight);
        stage.setMaxWidth(maxWidth);
        stage.setMaxHeight(maxHeight);
        stage.setX((maxWidth - defWidth) * 0.5);
        stage.setY((maxHeight - defHeight) * 0.5);

        // setup window
        stage.setTitle("TERILOG");
        stage.setScene(new Scene(root, defWidth, defHeight));
        stage.show();
    }

    // JavaFX
    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        // complete initialization
        initDimensions();
        initHandlers();

        // dynamically generate GUI
        constructGUI();
        setupStage();
    }
    public static void main(String[] args) {
        launch(args);
    }

    // utils
    static void sout(String text) {
        System.out.print(text);
    }
    static int rnd(double val) {
        return (int) Math.round(val);
    }

}
