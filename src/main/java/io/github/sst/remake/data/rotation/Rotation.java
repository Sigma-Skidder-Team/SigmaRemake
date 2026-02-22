package io.github.sst.remake.data.rotation;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Rotation {

    public float yaw, pitch;

    public float[] getPair() {
        return new float[]{yaw, pitch};
    }

}