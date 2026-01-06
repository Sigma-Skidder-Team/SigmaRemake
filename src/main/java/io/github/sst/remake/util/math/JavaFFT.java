package io.github.sst.remake.util.math;

import javax.sound.sampled.AudioFormat;

public class JavaFFT implements Transform {

    private static final int MAX_FAST_BITS = 16;
    private static final int[][] FFT_BIT_TABLE = new int[MAX_FAST_BITS][];
    private final int numberOfSamples;
    private final int[] reverseIndices;
    private final float[] frequencies;

    static {
        int len = 2;
        for (int b = 1; b <= MAX_FAST_BITS; b++) {
            FFT_BIT_TABLE[b - 1] = new int[len];
            for (int i = 0; i < len; i++) {
                FFT_BIT_TABLE[b - 1][i] = reverseBits(i, b);
            }
            len <<= 1;
        }
    }

    public JavaFFT(final int numberOfSamples) {
        if (!isPowerOfTwo(numberOfSamples)) throw new IllegalArgumentException("N is not a power of 2");
        if (numberOfSamples <= 0) throw new IllegalArgumentException("N must be greater than 0");
        this.numberOfSamples = numberOfSamples;
        final int numberOfBits = getNumberOfNeededBits(numberOfSamples);
        this.reverseIndices = new int[numberOfSamples];
        for (int i = 0; i < numberOfSamples; i++) {
            final int j = fastReverseBits(i, numberOfBits);
            this.reverseIndices[i] = j;
        }
        this.frequencies = new float[numberOfSamples];
        for (int index = 0; index < numberOfSamples; index++) {
            if (index <= numberOfSamples / 2) {
                this.frequencies[index] = (float) index / numberOfSamples;
            } else {
                this.frequencies[index] = -((numberOfSamples - index) / (float) numberOfSamples);
            }
        }
    }

    public float[][] inverseTransform(final float[] real, final float[] imaginary) throws UnsupportedOperationException {
        final float[][] out = new float[2][real.length];
        transform(true, real, imaginary, out[0], out[1]);
        return out;
    }

    public float[][] transform(final float[] real) throws UnsupportedOperationException {
        final float[][] out = new float[3][real.length];
        transform(false, real, null, out[0], out[1]);
        out[2] = frequencies.clone();
        return out;
    }

    public float[][] transform(final float[] real, final float[] imaginary) throws UnsupportedOperationException {
        final float[][] out = new float[3][real.length];
        transform(false, real, imaginary, out[0], out[1]);
        out[2] = frequencies.clone();
        return out;
    }

    /**
     * Actual fast Fourier transform implementation.
     *
     * @param inverse      inverse or not
     * @param realIn       real portion input
     * @param imaginaryIn  imaginary in
     * @param realOut      real out
     * @param imaginaryOut imaginary out
     */
    public void transform(final boolean inverse,
                          final float[] realIn,
                          final float[] imaginaryIn,
                          final float[] realOut,
                          final float[] imaginaryOut) {
        if (realIn.length != numberOfSamples) {
            throw new IllegalArgumentException("Number of samples must be " + numberOfSamples + " for this instance of JavaFFT");
        }

        for (int i = 0; i < numberOfSamples; i++) {
            realOut[this.reverseIndices[i]] = realIn[i];
        }
        if (imaginaryIn != null) {
            for (int i = 0; i < numberOfSamples; i++) {
                imaginaryOut[this.reverseIndices[i]] = imaginaryIn[i];
            }
        }

        int blockEnd = 1;
        final double angleNumerator;
        if (inverse) angleNumerator = -2.0 * Math.PI;
        else angleNumerator = 2.0 * Math.PI;
        for (int blockSize = 2; blockSize <= numberOfSamples; blockSize <<= 1) {
            final double deltaAngle = angleNumerator / (float) blockSize;
            final double sm2 = (-Math.sin(-2 * deltaAngle));
            final double sm1 = (-Math.sin(-deltaAngle));
            final double cm2 = (Math.cos(-2 * deltaAngle));
            final double cm1 = (Math.cos(-deltaAngle));
            final double w = 2 * cm1;
            double ar1;
            double ai1;
            double ar2;
            double ai2;

            for (int i = 0; i < numberOfSamples; i += blockSize) {
                ar2 = cm2;
                ar1 = cm1;

                ai2 = sm2;
                ai1 = sm1;

                for (int j = i, n = 0; n < blockEnd; j++, n++) {
                    final double ar0 = w * ar1 - ar2;
                    ar2 = ar1;
                    ar1 = ar0;

                    final double ai0 = w * ai1 - ai2;
                    ai2 = ai1;
                    ai1 = ai0;

                    final int k = j + blockEnd;
                    /* temp real, temp imaginary */
                    final double tr = ar0 * realOut[k] - ai0 * imaginaryOut[k];
                    final double ti = ar0 * imaginaryOut[k] + ai0 * realOut[k];

                    realOut[k] = (float) (realOut[j] - tr);
                    imaginaryOut[k] = (float) (imaginaryOut[j] - ti);

                    realOut[j] += (float) tr;
                    imaginaryOut[j] += (float) ti;
                }
            }

            blockEnd = blockSize;
        }

        // normalize, if inverse transform
        if (inverse) {
            for (int i = 0; i < numberOfSamples; i++) {
                realOut[i] /= (float) numberOfSamples;
                imaginaryOut[i] /= (float) numberOfSamples;
            }
        }
    }

    private static int getNumberOfNeededBits(final int powerOfTwo) {
        for (int i = 0; true; i++) {
            final int j = powerOfTwo & 1 << i;
            if (j != 0) return i;
        }
    }

    private static int reverseBits(final int index, final int numberOfBits) {
        int ind = index;
        int rev = 0;
        for (int i = 0; i < numberOfBits; i++) {
            rev = rev << 1 | ind & 1;
            ind >>= 1;
        }
        return rev;
    }

    private static int fastReverseBits(final int index, final int numberOfBits) {
        if (numberOfBits <= MAX_FAST_BITS)
            return FFT_BIT_TABLE[numberOfBits - 1][index];
        else
            return reverseBits(index, numberOfBits);
    }

    private static boolean isPowerOfTwo(final int number) {
        return (number & (number - 1)) == 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final JavaFFT javaFFT = (JavaFFT) o;
        return numberOfSamples == javaFFT.numberOfSamples;
    }

    @Override
    public int hashCode() {
        return numberOfSamples;
    }

    @Override
    public String toString() {
        return "JavaFFT{" +
                "N=" + numberOfSamples +
                '}';
    }

    public static float[] convertToPCMFloatArray(byte[] audioBytes, AudioFormat audioFormat) {
        float[] pcmValues = new float[audioBytes.length / audioFormat.getFrameSize()];

        for (int i = 0; i < audioBytes.length; i += audioFormat.getFrameSize()) {
            int sample = !audioFormat.isBigEndian() ? bytesToIntLE(audioBytes, i, audioFormat.getFrameSize()) : bytesToIntBE(audioBytes, i, audioFormat.getFrameSize());
            pcmValues[i / audioFormat.getFrameSize()] = (float) sample / 32768.0F;
        }

        return pcmValues;
    }

    public static double[] calculateAmplitudes(float[] realPart, float[] imaginaryPart) {
        double[] amplitudes = new double[realPart.length / 2];

        for (int i = 0; i < amplitudes.length; i++) {
            // Calculate magnitude using the Pythagorean theorem
            amplitudes[i] = Math.sqrt(realPart[i] * realPart[i] + imaginaryPart[i] * imaginaryPart[i]);
        }

        return amplitudes;
    }

    private static int bytesToIntLE(byte[] byteArray, int startIndex, int length) {
        int result = 0;

        for (int i = 0; i < length; i++) {
            // Extract the byte and shift it to its correct position
            int currentByte = byteArray[startIndex + i] & 0xFF;
            result += currentByte << (8 * i);
        }

        return result;
    }

    private static int bytesToIntBE(byte[] byteArray, int startIndex, int length) {
        int result = 0;

        for (int i = 0; i < length; i++) {
            // Extract the byte and shift it to its correct position
            int currentByte = byteArray[startIndex + i] & 0xFF;
            result += currentByte << (8 * (length - i - 1));
        }

        return result;
    }
}
