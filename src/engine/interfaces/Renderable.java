package engine.interfaces;

import javafx.scene.canvas.Canvas;

public interface Renderable {

    void render();
    void setGridPeriod(double period); // in pixels
    void setPos(int xPos, int yPos); // in periods
    void setGlobalAlpha(double alpha);

    boolean inside(int mx, int my); // everything in periods

    Canvas getBasis();
    int getWidth(); // in periods, current width (affected by rotation)
    int getHeight(); // in periods, current height (affected by rotation)

}
