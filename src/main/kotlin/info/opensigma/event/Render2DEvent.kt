package info.opensigma.event;


public class Render2DEvent {
    public final float tickDelta;
    public final long startTime;

    public Render2DEvent() {
        this.startTime = 0L;
        this.tickDelta = 0.0F;
    }

    public Render2DEvent(float tickDelta, long startTime) {
        this.startTime = startTime;
        this.tickDelta = tickDelta;
    }
}