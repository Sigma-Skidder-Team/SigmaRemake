package com.skidders.sigma.util.client.modules;

import com.skidders.sigma.util.client.interfaces.IFonts;

import java.util.Comparator;

public class ModuleNameLengthComparator implements Comparator<com.skidders.sigma.module.Module> {
    @Override
    public int compare(com.skidders.sigma.module.Module module1, com.skidders.sigma.module.Module module2) {
        int length1 = IFonts.JelloLightFont20.getWidth(module1.getName());
        int length2 = IFonts.JelloLightFont20.getWidth(module2.getName());
        if (length1 <= length2) {
            return length1 != length2 ? 1 : 0;
        } else {
            return -1;
        }
    }
}