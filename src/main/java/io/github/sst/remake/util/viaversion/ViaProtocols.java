package io.github.sst.remake.util.viaversion;

import java.util.HashMap;
import java.util.Map;

public enum ViaProtocols {
    R1_7_6(5),
    R1_8_X(47),
    R1_9(107),
    R1_9_1(108),
    R1_9_2(109),
    R1_9_3(110),
    R1_10(210),
    R1_11(315),
    R1_11_1(316),
    R1_12(335),
    R1_12_1(338),
    R1_12_2(340),
    R1_13(393),
    R1_13_1(401),
    R1_13_2(404),
    R1_14(477),
    R1_14_1(480),
    R1_14_2(485),
    R1_14_3(490),
    R1_14_4(498),
    R1_15(573),
    R1_15_1(575),
    R1_15_2(578),
    R1_16(735),
    R1_16_1(736),
    R1_16_2(751),
    R1_16_3(753),
    R1_16_4(754),
    R1_17(755),
    R1_17_1(756),
    R1_18(757),
    R1_18_2(758),
    R1_19(759),
    R1_19_1(760),
    R1_19_3(761),
    R1_19_4(762),
    R1_20(763),
    R1_20_2(764),
    R1_20_3(765),
    R1_20_5(766),
    R1_21(767),
    R1_21_2(768),
    R1_21_4(769),
    R1_21_5(770),
    R1_21_6(771),
    R1_21_7(772),
    R1_21_9(773),
    R1_21_11(774);

    private static final Map<Integer, ViaProtocols> LOOKUP = new HashMap<>();
    public final int protocol;

    ViaProtocols(int protocol) {
        this.protocol = protocol;
    }

    static {
        for (ViaProtocols value : values()) {
            LOOKUP.put(value.protocol, value);
        }
    }

    public static ViaProtocols getByProtocol(int protocol) {
        if (protocol == -1 || !ClassUtils.VIA_FABRIC_EXISTS) {
            return R1_16_4;
        }

        return LOOKUP.getOrDefault(protocol, R1_16_4);
    }

}