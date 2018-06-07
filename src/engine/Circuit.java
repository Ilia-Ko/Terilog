package engine;

import engine.interfaces.Renderable;

import java.util.ArrayList;

public class Circuit {

    private ArrayList<Component> components;
    private ArrayList<Wire> wires;

    public Circuit() {
        components = new ArrayList<>();
        wires = new ArrayList<>();
    }

    public void add(Renderable object) {
        if (object instanceof Component) components.add((Component) object);
        else if (object instanceof Wire) wires.add((Wire) object);
    }

}
