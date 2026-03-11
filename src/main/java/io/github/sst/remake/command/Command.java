package io.github.sst.remake.command;

import io.github.sst.remake.util.IMinecraft;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public abstract class Command implements IMinecraft {

    public final String name, description;
    public final String[] alias;

    public Command(String name, String description, String... alias) {
        this.name = name;
        this.description = description;
        this.alias = alias;
    }

    public abstract void execute(String[] args);

    public List<String> tab(String[] args) {
        return new ArrayList<>();
    }

    public String getPrimaryLiteral() {
        return name.toLowerCase(Locale.ROOT);
    }

    public List<String> getLiterals() {
        List<String> literals = new ArrayList<>();
        literals.add(getPrimaryLiteral());
        literals.addAll(Arrays.asList(alias));
        return literals;
    }

}
