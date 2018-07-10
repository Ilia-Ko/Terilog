package gui.control;

public interface HistoricalEvent {

    void undo();
    void redo();

    static HistoricalEvent invert(HistoricalEvent event) {
        return new HistoricalEvent() {
            @Override public void undo() {
                event.redo();
            }
            @Override public void redo() {
                event.undo();
            }
        };
    }

}
