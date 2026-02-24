package io.github.sst.remake.util.math.fft;

public interface Transform {
    float[][] transform(float[] real) throws UnsupportedOperationException;
}