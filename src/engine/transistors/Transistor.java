package engine.transistors;

import engine.Component;
import engine.Mirrorable;
import engine.Rotatable;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public abstract class Transistor extends Component implements Rotatable, Mirrorable {

    private static final Color COL_LEGS = Color.rgb(0, 0, 0);

    private int rotation;
    private int mirrorV, mirrorH;

    public Transistor(String uniqueID, double xPos, double yPos, int rotation, boolean mirroredVertical, boolean mirroredHorizontal) {
        super(uniqueID, xPos, yPos);

        // fill values
        this.rotation = rotation;
        if (mirroredVertical) mirrorV = -1;
        else mirrorV = 1;
        if (mirroredHorizontal) mirrorH = -1;
        else mirrorH = 1;
    }

    abstract void finishRendering(GraphicsContext gc);

    @Override
    public void render(GraphicsContext gc) {
        // transform
        gc.rotate(ROTATION_ANGLE * rotation);
        gc.scale(mirrorH, mirrorV);

        // render transistor
        gc.setFill(COL_LEGS);
        // legs:
        gc.strokeLine(x-2, y-1, x+0, y-1);
        gc.strokeLine(x+1, y-1, x+2, y-1);
        gc.strokeLine(x-1, y+0, x-1, y-1);
        gc.strokeLine(x+0, y+0, x-1, y-1);
        gc.strokeLine(x+1, y+0, x+1, y-1);
        // legs' caps:
        gc.strokeLine(x-1.2, y+0.0, x-0.8, y+0.0);
        gc.strokeLine(x-0.2, y+0.0, x+0.2, y+0.0);
        gc.strokeLine(x+0.8, y+0.0, x+1.2, y+0.0);
        // gate:
        gc.strokeLine(x-1.0, y+0.2, x+1.0, y+0.2);
        gc.strokeLine(x+0.0, y+0.2, x+0.0, y+1.0);

        finishRendering(gc);

        // transform back
        gc.scale(-mirrorH, -mirrorV);
        gc.rotate(-ROTATION_ANGLE * rotation);
    }

    // rotating
    @Override
    public void rotateClockwise() {
        rotation += 3;
        rotation %= NUM_ROTATIONS;
    }
    @Override
    public void rotateCounterClockwise() {
        rotation += 1;
        rotation %= NUM_ROTATIONS;
    }

    // mirroring
    @Override
    public void mirrorHorizontal() {
        mirrorH *= -1;
    }
    @Override
    public void mirrorVertical() {
        mirrorV *= -1;
    }

}
