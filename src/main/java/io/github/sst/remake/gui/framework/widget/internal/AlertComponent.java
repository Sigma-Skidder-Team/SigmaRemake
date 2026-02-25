package io.github.sst.remake.gui.framework.widget.internal;

public class AlertComponent {
    public ComponentType type;
    public String text;
    public int height;

    public AlertComponent(ComponentType type, String text, int height) {
        this.type = type;
        this.text = text;
        this.height = height;
    }
}