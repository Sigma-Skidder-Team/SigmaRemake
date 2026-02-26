package io.github.sst.remake.data.setting;

import java.util.List;

public interface DropdownSetting {
    String getName();

    int getModeIndex();

    void setModeByIndex(int index);

    List<String> getModeLabels();
}