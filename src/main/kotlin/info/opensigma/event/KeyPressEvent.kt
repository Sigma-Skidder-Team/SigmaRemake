package info.opensigma.event;

public class KeyPressEvent {

    public final int key, modifiers, keyAction;

    public KeyPressEvent(int key, int modifiers, int keyAction) {
        this.key = key;
        this.modifiers = modifiers;
        this.keyAction = keyAction;
    }

}
