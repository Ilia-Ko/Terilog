package engine.components.memory;

import engine.LogicLevel;
import engine.components.Pin;
import engine.connectivity.Node;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;

public class MemCell {

    private Pin write, read;
    private ObjectProperty<LogicLevel> mem;

    MemCell(Linear owner, int pos) {
        // init pins
        int pinPos = pos / 3 + pos + 1;
        write = new Pin(owner, Pin.IN, pinPos, 0);
        read = new Pin(owner, Pin.OUT, pinPos, 3);
        owner.getPins().add(write);
        owner.getPins().add(read);

        // init memory
        final Label lbl = (Label) owner.getRoot().lookup(String.format("#t%d%d", pos / 3, pos % 3));
        mem = new SimpleObjectProperty<>();
        mem.addListener((observable, oldValue, newValue) -> {
            lbl.setTextFill(newValue.colour());
            lbl.setText(String.valueOf(newValue.getDigitCharacter()));
        });
        mem.setValue(LogicLevel.ZZZ);
    }

    Node simulate(LogicLevel ctrl, LogicLevel clck) {
        boolean changed;

        // simulate
        if (ctrl.isUnstable() || clck.isUnstable())
            changed = read.update(LogicLevel.ERR);
        else if (ctrl == LogicLevel.NEG)
            changed = read.update(mem.get());
        else {
            if (ctrl == LogicLevel.POS && clck == LogicLevel.POS)
                mem.setValue(write.querySigFromNode());
            changed = read.update(LogicLevel.ZZZ);
        }

        return changed ? read.getNode() : null;
    }
    public ObjectProperty<LogicLevel> memProperty() {
        return mem;
    }

}
