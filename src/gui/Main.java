package gui;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Main extends Application {

    // general dimensions
    private static final double DEF_SCENE_WIDTH     = 0.618033;
    private static final double DEF_SCENE_HEIGHT    = 0.618033;
    private static final double DEF_FONT_SIZE       = 1.0 / 60.0;
    private static final double DEF_TOOL_HEIGHT     = 1.0 / 8.0;
    private static final double DEF_SPACING         = 0.005;

    // MENU structure
    private static final int NUM_MENUS = 2;
    private static final int NUM_ITEMS = 8;
    private static final String[][] STR_MENUS = new String[][] {
            {"File",
                    "Ctrl+O\tOpen",
                    "Ctrl+S\tSave",
                    "      \tSave as...",
                    "#separator",
                    "F11   \tToggle fullscreen",
                    "Ctrl+Q\tExit"},
            {"About",
                    "Help",
                    "Credits"}
    };

    // event handlers
    private static EventHandler[] MENU_EVENTS;
    private static EventHandler[] TOOLS_EVENTS;
    private Controller control;

    // dimensions
    private double maxWidth, maxHeight;
    private double defWidth, defHeight;
    private double minWidth, minHeight;
    private double defSpacing;
    private String defFont;

    // gui components
    private Stage stage;
    private MenuBar menuBar;
    private VBox root;

    // dynamic gui
    private void initHandlers() {
        control = new Controller(this, stage);

        // init menu controls
        MENU_EVENTS = new EventHandler[NUM_ITEMS];
        // Menu.File
        MENU_EVENTS[0] = control::onMenuOpenClicked;
        MENU_EVENTS[1] = control::onMenuSaveClicked;
        MENU_EVENTS[2] = control::onMenuSaveAsClicked;
        MENU_EVENTS[4] = control::onMenuToggleFullscreenClicked;
        MENU_EVENTS[5] = control::onMenuExitClicked;
        // Menu.About
        MENU_EVENTS[6] = control::onMenuHelpClicked;
        MENU_EVENTS[7] = control::onMenuCreditsClicked;

        // init tools controls
        TOOLS_EVENTS = new EventHandler[Tools.NUM_TOOLS];
        TOOLS_EVENTS[0] = control::onToolHardNClicked;
        TOOLS_EVENTS[1] = control::onToolHardPClicked;
        TOOLS_EVENTS[2] = control::onToolSoftNClicked;
        TOOLS_EVENTS[3] = control::onToolSoftPClicked;

    }
            void measureGUI() {
        // take delicate care of DPI: (I hate when some apps look awful on Retina displays :)
        Rectangle2D rect = Screen.getPrimary().getBounds();
        maxWidth    = rect.getWidth();
        maxHeight   = rect.getHeight();
        if (stage.isFullScreen()) {
            defWidth = maxWidth;
            defHeight = maxHeight;
        } else {
            defWidth = maxWidth * DEF_SCENE_WIDTH;
            defHeight = maxHeight * DEF_SCENE_HEIGHT;
        }
        defSpacing = maxHeight * DEF_SPACING;
    }
    private void constructGUI() {
        measureGUI();

        // adjust font:
        int defFontSize = rnd(maxHeight * DEF_FONT_SIZE);
        defFont = "-fx-font: normal " + Integer.toString(defFontSize) + "px monospace";

        // build menu:
        menuBar = new MenuBar();
        for (int i = 0; i < NUM_MENUS; i++) {
            Menu menu = new Menu(STR_MENUS[i][0]);
            for (int j = 1; j < STR_MENUS[i].length; j++) {
                MenuItem item;
                if (STR_MENUS[i][j].equals("#separator")) {
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

        // (re)create other gui
        resizeGUI();
    }
            void resizeGUI() {
        // make  spacers:
        Region[] spacer = new Region[3];
        for (int i = 0; i < 3; i++) {
            spacer[i] = new Region();
            spacer[i].setMinHeight(defSpacing);
            spacer[i].setPrefHeight(defSpacing);
            spacer[i].setMaxHeight(defSpacing);
        }

        // build toolbar:
        Tools[] tools = Tools.values();
        Node[] toolNodes = new Node[Tools.NUM_TOOLS + 1];
        toolNodes[0] = spacer[0];
        double size = defHeight * DEF_TOOL_HEIGHT;
        for (int i = 0; i < Tools.NUM_TOOLS; i++) {
            Button button;
            try {
                FileInputStream fis = new FileInputStream(tools[i].getImageSourcePath());
                ImageView image = new ImageView(new Image(fis));
                image.setPreserveRatio(true);
                image.setFitWidth(size);
                image.setFitHeight(size);
                button = new Button(tools[i].getButtonName(), image);
            } catch (FileNotFoundException e) {
                sout("Failed to load image for " + tools[i].getToolTipString() + '\n');
                button = new Button(tools[i].getButtonName());
            }
            button.setOnAction(TOOLS_EVENTS[i]);
            button.setStyle(defFont);
            toolNodes[i+1] = button;
        }
        HBox toolBar = new HBox(toolNodes);
        toolBar.setSpacing(defSpacing);

        // build field:
        double fieldWidth = defWidth;
        double fieldHeight = defHeight - menuBar.getHeight() - toolBar.getHeight();
        minWidth = toolBar.getWidth() > menuBar.getWidth() ? toolBar.getWidth() : menuBar.getWidth();
        minHeight = defHeight - fieldHeight + 50;
        Canvas field = new Canvas(fieldWidth, fieldHeight);
        field.setStyle(defFont);
        field.setOnMouseClicked(control::onFieldClicked);
        control.setField(field);
        control.clearField();

        // combine everything together:
        root = new VBox(menuBar, spacer[1], toolBar, spacer[2], field);
        root.setStyle(defFont);
        root.setMinWidth(minWidth);
        root.setMinHeight(minHeight);
        root.setPrefWidth(defHeight);
        root.setPrefHeight(defHeight);
        root.setMaxWidth(maxWidth);
        root.setMaxHeight(maxWidth);

        setupStage();
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
        initHandlers();

        // dynamically generate GUI
        constructGUI();
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
