package io.github.sst.remake.gui.framework.widget.internal;

public class AlertComponent {
    public ComponentType componentType;
    public String text;
    public int componentHeight;

    public AlertComponent(ComponentType componentType, String title, int componentHeight) {
        this.componentType = componentType;
        this.text = title;
        this.componentHeight = componentHeight;
    }
}