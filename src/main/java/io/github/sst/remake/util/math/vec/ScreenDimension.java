package io.github.sst.remake.util.math.vec;

import java.util.Objects;

public class ScreenDimension {
    public int width;
    public int height;

    public ScreenDimension(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public ScreenDimension add(ScreenDimension from) {
        return new ScreenDimension(this.width + from.width, this.height + from.height);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ScreenDimension)) return false;
        ScreenDimension that = (ScreenDimension) o;
        return width == that.width && height == that.height;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height);
    }
}