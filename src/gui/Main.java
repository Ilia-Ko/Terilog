package gui;

import javafx.application.Application;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    // general dimensions
    private static final double G = 0.5 * (1.0 + Math.sqrt(5.0));
    private static final double DEF_FONT_SIZE = 1.0 / 60.0;

    // MENU structure
    static final String STR_SEPARATOR = "#separator";
    private static final String[] STR_MENUS = new String[] {"File", "Add", "About"};
    private static final int[] NUM_MENUS = new int[] {6, 8, 2};

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

    // dynamic gui
    private void constructGUI() {
        // build menu
        int k = 0;
        MenuBar menuBar = new MenuBar();
        Control.TheAction[] actions = Control.TheAction.values();
        for (int i = 0; i < STR_MENUS.length; i++) {
            Menu menu = new Menu(STR_MENUS[i]);
            for (int j = 0; j < NUM_MENUS[i]; j++) {
                MenuItem item;
                if (actions[k].getDescription().endsWith(STR_SEPARATOR)) {
                    item = new SeparatorMenuItem();
                } else {
                    item = new MenuItem(actions[k].getDescription());
                    item.setOnAction(actions[k]::commit);
                    item.setStyle(defFont);
                }
                k++;
                menu.getItems().add(item);
            }
            menuBar.getMenus().add(menu);
        }

        // build field
        Canvas field = new Canvas(maxWidth, maxHeight);
        field.setStyle(defFont);
        field.setCursor(Cursor.CROSSHAIR);
        field.setOnMouseClicked(Control::onFieldClicked);
        Control.init(stage, field);
        Control.clearField();

        // wrap field with scroll pane
        ScrollPane scroll = new ScrollPane(field);
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        // combine everything together
        root = new VBox(menuBar, scroll);
        root.setStyle(defFont);
        root.setMinWidth(minWidth);
        root.setMinHeight(minHeight);
        root.setPrefWidth(defHeight);
        root.setPrefHeight(defHeight);
        root.setMaxWidth(maxWidth);
        root.setMaxHeight(maxWidth);

        // setup keyboard callbacks
        root.setOnKeyPressed(event -> {
            for (Control.TheAction action : actions)
                if (action.getCombo().match(event)) {
                    action.commit(event);
                    break;
                }
        });

    }
    private void setupStage() {
        // adjust window geometry
        stage.setResizable(true);
        stage.setFullScreenExitHint("Press F11 to leave fullscreen mode.");
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
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
