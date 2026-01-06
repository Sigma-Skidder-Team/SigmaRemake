package io.github.sst.remake.util.math;

public interface Transform {

    /**
     * Performs transform on real values.
     *
     * @param real input array of real numbers
     * @return array with the real and the imaginary part of the transform, the third float array contains
     * the normalized frequencies, i.e. 1.0 is equal to the sample rate of the input
     * @throws UnsupportedOperationException should the implementation not support this operation
     */
    public float[][] transform(float[] real) throws UnsupportedOperationException;

    /**
     * Performs a complex transform.
     *
     * @param real input array of floats
     * @param imaginary input array of floats
     * @return array with the real and the imaginary part of the transform, the third float array contains
     * the normalized frequencies, i.e. 1.0 is equal to the sample rate of the input
     * @throws UnsupportedOperationException should the implementation not support this operation
     */
    public float[][] transform(float[] real, float[] imaginary) throws UnsupportedOperationException;


    /**
     * Performs an inverse transform.
     *
     * @param real input array of floats
     * @param imaginary input array of floats
     * @return array with the real and the imaginary part of the transform
     * @throws UnsupportedOperationException should the implementation not support this operation
     */
    public float[][] inverseTransform(float[] real, float[] imaginary) throws UnsupportedOperationException;

}