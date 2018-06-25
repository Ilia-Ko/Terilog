package gui.control;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class ControlAbout {

    // gui
    @FXML private TreeView<Label> content;
    @FXML private WebView text;

    // callback
    private Stage dialog;

    // initialization
    void initialSetup(Stage dialog) {
        this.dialog = dialog;
        loadTree();
    }
    private void loadTree() {
        // roots
        TreeItem<Label> rootGlob = newItem("About");
        TreeItem<Label> rootMenu = newItem("Menu");
        TreeItem<Label> rootComp = newItem("Components");
        rootGlob.getChildren().addAll(rootMenu, rootComp);
        content.setRoot(rootGlob);

        // about: menu.add
        TreeItem<Label> menuAdd = newItem("Add");
        TreeItem<Label> menuAddComp = newItem("Component");
        TreeItem<Label> menuAddWire = newItem("Wire");
        menuAdd.getChildren().addAll(menuAddComp, menuAddWire);
        rootMenu.getChildren().add(menuAdd);

        // about: menu.circuit
        TreeItem<Label> menuCir = newItem("Circuit");
        TreeItem<Label> menuCirReset = newItem("Reset");
        TreeItem<Label> menuCirParse = newItem("Parse");
        TreeItem<Label> menuCirStepInto = newItem("Step into");
        TreeItem<Label> menuCirStepOver = newItem("Step over");
        TreeItem<Label> menuCirRun = newItem("Run");
        TreeItem<Label> menuCirSettings = newItem("Settings");
        menuCir.getChildren().addAll(menuCirReset, menuCirParse, menuCirStepInto, menuCirStepOver, menuCirRun, menuCirSettings);
        rootMenu.getChildren().add(menuCir);

        // about: menu.grid
        TreeItem<Label> menuGrid = newItem("Grid");
        rootMenu.getChildren().add(menuGrid);

        // about: comp.MOSFET
        TreeItem<Label> compMOSFET = newItem("MOSFET");
        TreeItem<Label> compMOSFETHardN = newItem("Hard N");
        TreeItem<Label> compMOSFETHardP = newItem("Hard P");
        TreeItem<Label> compMOSFETSoftN = newItem("Soft N");
        TreeItem<Label> compMOSFETSoftP = newItem("Soft P");
        compMOSFET.getChildren().addAll(compMOSFETHardN, compMOSFETHardP, compMOSFETSoftN, compMOSFETSoftP);
        rootComp.getChildren().add(compMOSFET);

        // about: comp.lumped
        TreeItem<Label> compLumped = newItem("Lumped");
        TreeItem<Label> compLumpedDiode = newItem("Diode");
        TreeItem<Label> compLumpedReconciliator = newItem("Reconciliator");
        TreeItem<Label> compLumpedIndicator = newItem("Indicator");
        TreeItem<Label> compLumpedVoltage = newItem("Voltage");
        compLumped.getChildren().addAll(compLumpedDiode, compLumpedIndicator, compLumpedReconciliator, compLumpedVoltage);

        // about: comp.logic 1-arg
        TreeItem<Label> compLog1 = newItem("Logic 1-arg");
        TreeItem<Label> compLog1Inv = newItem("Inverters");
        TreeItem<Label> compLog1InvNTI = newItem("NTI");
        TreeItem<Label> compLog1InvSTI = newItem("STI");
        TreeItem<Label> compLog1InvPTI = newItem("PTI");
        compLog1Inv.getChildren().addAll(compLog1InvNTI, compLog1InvSTI, compLog1InvPTI);
        compLog1.getChildren().add(compLog1Inv);
        rootComp.getChildren().add(compLog1);

        // about: comp.logic 2-arg
        TreeItem<Label> compLog2 = newItem("Logic 2-arg");
        TreeItem<Label> compLog2Gate = newItem("Gates");
        TreeItem<Label> compLog2GateNAND = newItem("NAND");
        TreeItem<Label> compLog2GateNOR = newItem("NOR");
        TreeItem<Label> compLog2GateNCON = newItem("NCON");
        TreeItem<Label> compLog2GateNANY = newItem("NANY");
        TreeItem<Label> compLog2Key = newItem("Keys");
        TreeItem<Label> compLog2KeyOKEY = newItem("OKEY");
        TreeItem<Label> compLog2KeyCKEY = newItem("CKEY");
        compLog2Gate.getChildren().addAll(compLog2GateNAND, compLog2GateNOR, compLog2GateNCON, compLog2GateNANY);
        compLog2Key.getChildren().addAll(compLog2KeyOKEY, compLog2KeyCKEY);
        compLog2.getChildren().addAll(compLog2Gate, compLog2Key);
        rootComp.getChildren().add(compLog2);

        // about: comp.arithmetic
        TreeItem<Label> compArith = newItem("Arithmetic");
        TreeItem<Label> compArithHalfAdder = newItem("Half Adder");
        TreeItem<Label> compArithFullAdder = newItem("Full Adder");
        compArith.getChildren().addAll(compArithHalfAdder, compArithFullAdder);
        rootComp.getChildren().add(compArith);

        // setup tree
        rootGlob.setExpanded(true);
        rootMenu.setExpanded(true);
        rootComp.setExpanded(true);
    }
    private TreeItem<Label> newItem(String name) {
        Label label = new Label(name);
        label.setOnMouseClicked(event -> onItemClick(name));
        return new TreeItem<>(label);
    }

    // events
    private void onItemClick(String name) {
        String path = "manuals/" + name + ".html";
        path = getClass().getResource(path).getRef();
        text.getEngine().load(path);
    }
    @FXML private void btnCloseClicked() {
        dialog.close();
    }

}
