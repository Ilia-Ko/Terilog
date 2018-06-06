package gui;

public enum Tools {

    HARD_N("N", "images/tools_hard_n.png", "Hard N MOSFET. See About/Help for more details."),

    HARD_P("P", "images/tools_hard_n.png", "Hard P MOSFET. See About/Help for more details."),

    SOFT_N("n", "images/tools_hard_n.png", "Soft N MOSFET. See About/Help for more details."),

    SOFT_P("p", "images/tools_hard_n.png", "Soft P MOSFET. See About/Help for more details.");



    public static final int NUM_TOOLS = 4;

    String name, imgPath, toolTip;

    Tools(String name, String imgPath, String toolTip) {
        this.name = name;
        this.imgPath = imgPath;
        this.toolTip = toolTip;
    }

    public String getButtonName() {
        return name;
    }
    public String getImageSourcePath() {
        return imgPath;
    }
    public String getToolTipString() {
        return toolTip;
    }

}
