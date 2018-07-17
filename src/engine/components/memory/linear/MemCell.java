package engine.components.memory.linear;

import engine.LogicLevel;
import engine.components.Pin;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.Label;

import static engine.LogicLevel.*;

public class MemCell {

    private Pin write, read;
    private ObjectProperty<LogicLevel> mem;

    MemCell(Linear owner, int pos, int digit) {
        // init pins
        int pinPos = pos / 3 + pos + 1;
        write = new Pin(owner, true, pinPos, 0);
        read = new Pin(owner, false, pinPos, 3);
        owner.getPins().add(write);
        owner.getPins().add(read);

        // init memory
        final Label lbl = (Label) owner.getRoot().lookup(String.format("#t%d%d", digit / 3, digit % 3));
        mem = new SimpleObjectProperty<>();
        mem.addListener((observable, oldValue, newValue) -> {
            lbl.setTextFill(newValue.colour());
            lbl.setText(String.valueOf(newValue.getDigitCharacter()));
        });
        mem.setValue(ZZZ);
    }

    void simulate(LogicLevel ctrl, LogicLevel fill, LogicLevel clck) {
        if (ctrl == ERR || clck == ERR) {
            mem.setValue(ERR);
        } else if (clck == POS) {
            if (ctrl == POS) mem.setValue(write.get());
            else if (ctrl == NEG) mem.setValue(fill);
        }
        read.put(mem.get());
    }
    public ObjectProperty<LogicLevel> memProperty() {
        return mem;
    }

}
