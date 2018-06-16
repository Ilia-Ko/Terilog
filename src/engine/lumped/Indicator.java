package engine.lumped;

import engine.Component;
import engine.Node;
import gui.control.ControlMain;
import javafx.geometry.VPos;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;

public class Indicator extends Component {

    public static final String ATTR_CLASS_NAME = "ind";
    private static final int RADIUS = 1;

    private Pin source;

    // initialization
    @Override protected Pin[] initPins() {
        source = new Pin(this, Pin.INPUT, "source");
        source.setPos(0, RADIUS);
        return new Pin[] {source};
    }
    @Override public Component newCompOfTheSameClass() {
        return new Indicator();
    }

    // simulation
    @Override public ArrayList<Node> simulate() {
        return new ArrayList<>();
    }
    @Override protected boolean isIndependent() {
        return false;
    }

    // rendering
    @Override public void render() {
        double r = Pin.PIN_CIRCLE_RADIUS;
        double a = (RADIUS + r) * 2;

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
        source.render(gc);

        // render everything
        gc.translate(r, r);
        renderBody(gc);

        // reset gc
        gc.restore();
    }
    @Override protected void renderBody(GraphicsContext gc) {
        // casing
        RadialGradient fill = new RadialGradient(0, 0, RADIUS, RADIUS, RADIUS * 2, false,
                CycleMethod.NO_CYCLE, new Stop(0, source.sig().colour()), new Stop(1, Color.WHITE));
        gc.setFill(fill);
        gc.fillOval(0, 0, RADIUS * 2, RADIUS * 2);

        // value
        gc.setFill(Color.WHITE);
        gc.setFont(new Font(gc.getFont().getName(), RADIUS * 2));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.fillText(String.valueOf(source.sig().getDigitCharacter()), RADIUS, RADIUS);
    }
    @Override protected int getAbsoluteWidth() {
        return RADIUS * 2;
    }
    @Override protected int getAbsoluteHeight() {
        return RADIUS * 2;
    }

    // informative
    @Override public String toString() {
        return source.sig().getStandardName();
    }
    @Override protected String getAttrClassName() {
        return ATTR_CLASS_NAME;
    }
    @Override public String getPrefixID() {
        return "i";
    }
}
