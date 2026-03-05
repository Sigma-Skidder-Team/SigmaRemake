package io.github.sst.remake.util.game;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Rotation {
    public float yaw, pitch;

    public float[] asArray() {
        return new float[] {yaw, pitch};
    }
}
