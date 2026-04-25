package io.github.sst.remake.util.system.io.audio.stream;

public interface AudioProcessor {
    void processBuffer(byte[] buffer, int offset, int length);
}