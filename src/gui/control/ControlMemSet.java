package gui.control;

import engine.LogicLevel;
import engine.components.memory.Linear;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class ControlMemSet {

    // gui
    @FXML private TextField t00, t01, t02;
    @FXML private TextField t10, t11, t12;
    @FXML private TextField t20, t21, t22;
    @FXML private TextField t30, t31, t32;
    @FXML private TextField t40, t41, t42;
    @FXML private TextField t50, t51, t52;
    @FXML private TextField t60, t61, t62;
    @FXML private TextField t70, t71, t72;
    private TextField[] texts;

    // callback
    private Stage dialog;
    private LogicLevel[] memory;

    // initialization
    public void initialSetup(Stage dialog, Linear memory) {
        this.dialog = dialog;
        this.memory = memory.getMemory();

        switch (this.memory.length) {
            case 3:
                texts = new TextField[] {t00, t01, t02};
                break;
            case 6:
                texts = new TextField[] {t00, t01, t02,
                                        t10, t11, t12};
                break;
            case 12:
                texts = new TextField[] {t00, t01, t02,
                                        t10, t11, t12,
                                        t20, t21, t22,
                                        t30, t31, t32};
                break;
            case 24:
                texts = new TextField[] {t00, t01, t02,
                                        t10, t11, t12,
                                        t20, t21, t22,
                                        t30, t31, t32,
                                        t40, t41, t42,
                                        t50, t51, t52,
                                        t60, t61, t62,
                                        t70, t71, t72};
        }
        for (TextField txt : texts) {
            txt.setEditable(true);
            txt.setFocusTraversable(true);
        }
        btnResetClicked();
    }

    // events
    @FXML private void btnConfirmClicked() {
        for (int i = 0; i < memory.length; i++)
            memory[i] = LogicLevel.parseDigit(texts[i].getText().charAt(0));
        dialog.close();
    }
    @FXML private void btnResetClicked() {
        for (int i = 0; i < memory.length; i++)
            texts[i].setText(String.valueOf(memory[i].getDigitCharacter()));
    }
    @FXML private void btnClearClicked() {
        for (TextField txt : texts) txt.setText("0");
    }
    @FXML private void btnCancelClicked() {
        dialog.close();
    }
    @FXML private void keyTyped(KeyEvent key) {
        char d = key.getCharacter().charAt(0);
        switch (d) {
            case '1':
            case '+':
            case '=':
                d = '1';
                break;
            case '0':
            case ' ':
            case ')':
                d = '0';
                break;
            case '\u03bb':
            case '-':
            case '_':
                d = '\u03bb';
                break;
            case '?':
            case 'Z':
            case 'z':
                d = '?';
                break;
            case '!':
            case 'E':
            case 'e':
                d = '!';
                break;
            default:
                d = '?';
        }
        TextField txt = (TextField) key.getTarget();
        txt.setText(String.valueOf(d));
    }

}
