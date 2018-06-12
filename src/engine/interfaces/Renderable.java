package engine.interfaces;

import javafx.scene.canvas.GraphicsContext;

public interface Renderable {

    void render(GraphicsContext gc);
    void setPos(int xPos, int yPos);

    boolean inside(int mx, int my);

    int getWidth();
    int getHeight();

}
