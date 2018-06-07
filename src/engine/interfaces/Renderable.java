package engine.interfaces;

import javafx.scene.canvas.GraphicsContext;

public interface Renderable {

    void render(GraphicsContext gc);
    void setPos(double xPos, double yPos);

    double getWidth();
    double getHeight();

}
