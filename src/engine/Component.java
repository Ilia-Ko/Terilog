package engine;

import javafx.scene.canvas.GraphicsContext;

import java.nio.ByteBuffer;

public abstract class Component {

    private String id;
    protected double x, y;

    public Component(String uniqueID, double xPos, double yPos) {
        id = uniqueID;
        x = xPos;
        y = yPos;
    }

    public abstract void render(GraphicsContext gc);

    // saving and loading from TLG files
    public void save(ByteBuffer buffer) {
        // write id
        buffer.putInt(id.length());
        buffer.put(id.getBytes());

        // write position
        buffer.putDouble(x);
        buffer.putDouble(y);
    }
    public void load(ByteBuffer buffer) {
        // id has been already read
    }

}
