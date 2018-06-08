package engine;

import engine.interfaces.Renderable;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;

public class Circuit {

    // construction logic
    private ArrayList<Component> components;
    private ArrayList<Wire> wires;

    // simulation logic
    private boolean isSimRunning;

    public Circuit() {
        // construction logic
        components = new ArrayList<>();
        wires = new ArrayList<>();

        // simulation logic
        isSimRunning = false;
    }

    public void renderAll(GraphicsContext gc) {
        for (Wire wire : wires) wire.render(gc);
        for (Component component : components) component.render(gc);
    }

    // construction logic
    public void addWire(Wire wire) {
        wires.add(wire);
    }
    public void addComponent(Component component) {
        components.add(component);
    }
    public Renderable getPointedObject(int x, int y) {
        for (Wire wire : wires) if (wire.inside(x, y)) return wire;
        for (Component component : components) if (component.inside(x, y)) return component;
        return null;
    }

    // simulation logic
    public void startSimulation() {
        isSimRunning = true;

    }
    public void stopSimulation() {
        isSimRunning = false;

    }
    public boolean isSimulationRunning() {
        return isSimRunning;
    }

}
