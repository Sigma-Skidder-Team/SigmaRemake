package io.github.sst.remake.util.java;

import java.util.ArrayList;
import java.util.List;

public final class ListUtils {
    public static <T> List<T> reverse(List<T> list) {
        final ArrayList<T> var3 = new ArrayList<>();

        for (int i = list.size() - 1; i >= 0; i--) {
            var3.add(list.get(i));
        }

        return var3;
    }

}
