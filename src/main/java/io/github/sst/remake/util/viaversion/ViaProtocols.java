package io.github.sst.remake.util.viaversion;

import java.util.LinkedHashMap;
import java.util.Map;

public enum ViaProtocols {
    R1_7_6(5, "1.7.6 - 1.7.10"),
    R1_8_X(47, "1.8.x"),
    R1_9(107, "1.9"),
    R1_9_1(108, "1.9.1"),
    R1_9_2(109, "1.9.2"),
    R1_9_3(110, "1.9.3 - 1.9.4"),
    R1_10(210, "1.10.x"),
    R1_11(315, "1.11"),
    R1_11_1(316, "1.11.1 - 1.11.2"),
    R1_12(335, "1.12"),
    R1_12_1(338, "1.12.1"),
    R1_12_2(340, "1.12.2"),
    R1_13(393, "1.13"),
    R1_13_1(401, "1.13.1"),
    R1_13_2(404, "1.13.2"),
    R1_14(477, "1.14"),
    R1_14_1(480, "1.14.1"),
    R1_14_2(485, "1.14.2"),
    R1_14_3(490, "1.14.3"),
    R1_14_4(498, "1.14.4"),
    R1_15(573, "1.15"),
    R1_15_1(575, "1.15.1"),
    R1_15_2(578, "1.15.2"),
    R1_16(735, "1.16"),
    R1_16_1(736, "1.16.1"),
    R1_16_2(751, "1.16.2"),
    R1_16_3(753, "1.16.3"),
    R1_16_4(754, "1.16.4 - 1.16.5"),
    R1_17(755, "1.17"),
    R1_17_1(756, "1.17.1"),
    R1_18(757, "1.18 - 1.18.1"),
    R1_18_2(758, "1.18.2"),
    R1_19(759, "1.19"),
    R1_19_1(760, "1.19.1 - 1.19.2"),
    R1_19_3(761, "1.19.3"),
    R1_19_4(762, "1.19.4"),
    R1_20(763, "1.20 - 1.20.1"),
    R1_20_2(764, "1.20.2"),
    R1_20_3(765, "1.20.3 - 1.20.4"),
    R1_20_5(766, "1.20.5 - 1.20.6"),
    R1_21(767, "1.21 - 1.21.1"),
    R1_21_2(768, "1.21.2 - 1.21.3"),
    R1_21_4(769, "1.21.4"),
    R1_21_5(770, "1.21.5"),
    R1_21_6(771, "1.21.6"),
    R1_21_7(772, "1.21.7 - 1.21.8"),
    R1_21_9(773, "1.21.9"),
    R1_21_11(774, "1.21.10 - 1.21.11");

    private static final Map<Integer, ViaProtocols> LOOKUP = new LinkedHashMap<>();
    public final int protocol;
    public final String name;

    ViaProtocols(int protocol, String name) {
        this.protocol = protocol;
        this.name = name;
    }

    ViaProtocols(int protocol) {
        this(protocol, "Unknown");
    }

    static {
        for (ViaProtocols value : values()) {
            LOOKUP.put(value.protocol, value);
        }
    }

    public static ViaProtocols getByProtocol(int protocol) {
        if (protocol == -1 || !ViaInstance.VIAVERSION_EXISTS) {
            return R1_16_4;
        }

        return LOOKUP.getOrDefault(protocol, R1_16_4);
    }

    public static ViaProtocols getByIndex(int index) {
        ViaProtocols[] values = values();
        if (index < 0 || index >= values.length) return R1_16_4;
        return values[index];
    }
}