package engine.lumped;

import engine.Component;
import engine.LogicLevel;
import engine.Node;
import gui.control.ControlMain;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;

public class Voltage extends Component {

    public static final String ATTR_CLASS_NAME = "volts";

    private static final int SIZE = 2;

    private Pin drain;
    private LogicLevel level;

    // initialization
    public Voltage() {
        level = LogicLevel.NIL;
    }
    public void setLogicLevel(LogicLevel level) {
        this.level = level;
        Tooltip.install(getBasis(), new Tooltip(toString()));
        render();
    }
    @Override protected Pin[] initPins() {
        drain = new Pin(this, Pin.OUTPUT, "drain");
        drain.setPos(SIZE / 2, SIZE);
        return new Pin[] {drain};
    }
    @Override public Component newCompOfTheSameClass() {
        Voltage v = new Voltage();
        v.setLogicLevel(level);
        return v;
    }
    @Override protected ContextMenu initContextMenu(ControlMain control) {
        // make new menu "set voltage"
        Menu itemSet = new Menu("Set voltage");
        LogicLevel[] levels = LogicLevel.values();
        MenuItem[] subItems = new MenuItem[levels.length];
        for (int i = 0; i < levels.length; i++) {
            LogicLevel lev = levels[i];
            subItems[i] = new MenuItem(String.format("%c (%s)", lev.getDigitCharacter(), lev.getStandardName()));
            subItems[i].setStyle("-fx-fill: #" + lev.colour().toString().substring(2));
            subItems[i].setOnAction(event -> setLogicLevel(lev));
        }
        itemSet.getItems().addAll(subItems);

        // insert this menu
        ContextMenu menu = control.makeContextMenuFor(this);
        menu.getItems().add(0, itemSet);
        return menu;
    }

    // simulation
    @Override public ArrayList<Node> simulate() {
        // send signal
        drain.sendSig(level);

        // report about affected node
        ArrayList<Node> affected = new ArrayList<>();
        affected.add(drain.getNode());
        return affected;
    }
    @Override protected boolean isIndependent() {
        return true;
    }

    // rendering
    @Override public void render() {
        double r = Pin.PIN_CIRCLE_RADIUS;
        double a = SIZE + r * 2;

        // configure gc
        GraphicsContext gc = getBasis().getGraphicsContext2D();
        gc.save();
        gc.setGlobalAlpha(alpha);
        gc.scale(p, p);
        gc.clearRect(0, 0, a, a);

        // draw focus frame
        if (isHovered) {
            double l = ControlMain.LINE_WIDTH;
            gc.setStroke(Color.LIGHTSKYBLUE);
            gc.setLineWidth(l);
            gc.strokeRect(l / 2, l / 2, a - l, a - l);
        }

        // render pin
        drain.render(gc);

        // render everything
        gc.translate(r, r);
        renderBody(gc);

        // reset gc
        gc.restore();
    }
    @Override protected void renderBody(GraphicsContext gc) {
        gc.setLineWidth(ControlMain.LINE_WIDTH);
        gc.setStroke(level.colour());

        // casing
        double a = SIZE / 2;
        RadialGradient fill = new RadialGradient(0, 0, a, a, SIZE, false,
                CycleMethod.NO_CYCLE, new Stop(0, level.colour()), new Stop(1, Color.WHITE));
        gc.setFill(fill);
        gc.fillRect(0, 0, SIZE, SIZE);

        // value
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(gc.getFont().getName(), SIZE));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(String.valueOf(level.getDigitCharacter()), a, a);
    }
    @Override protected int getAbsoluteWidth() {
        return SIZE;
    }
    @Override protected int getAbsoluteHeight() {
        return SIZE;
    }
    @Override public int getWidth() {
        return SIZE;
    }
    @Override public int getHeight() {
        return SIZE;
    }

    // informative
    @Override public String toString() {
        return level.getStandardName() + " voltage";
    }
    @Override protected String getAttrClassName() {
        return ATTR_CLASS_NAME;
    }
    @Override public String getPrefixID() {
        return "v";
    }

}
