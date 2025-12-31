package io.github.sst.remake.gui.element.impl.drop;

import io.github.sst.remake.gui.CustomGuiScreen;
import io.github.sst.remake.gui.interfaces.ICustomGuiScreenVisitor;

public class GridLayoutVisitor implements ICustomGuiScreenVisitor {
    public int columns;
    public int verticalGap;

    public GridLayoutVisitor(int columns) {
        this(columns, 0);
    }

    public GridLayoutVisitor(int columns, int verticalGap) {
        this.columns = columns;
        this.verticalGap = verticalGap;
    }

    private CustomGuiScreen findTallestChild(CustomGuiScreen[] children) {
        CustomGuiScreen tallest = children[0];

        for (CustomGuiScreen child : children) {
            if (child.getHeight() > tallest.getHeight()) {
                tallest = child;
            }
        }

        return tallest;
    }

    @Override
    public void visit(CustomGuiScreen container) {
        if (container.getChildren().size() > 0) {
            int i = 0;

            while (i < container.getChildren().size()) {
                CustomGuiScreen current = container.getChildren().get(i);
                if (i > 0 && i % this.columns == 0) {
                    CustomGuiScreen[] previousRow = new CustomGuiScreen[this.columns];

                    for (int j = 0; j < this.columns; j++) {
                        previousRow[j] = container.getChildren().get(i - this.columns + j);
                    }

                    CustomGuiScreen tallestInPreviousRow = this.findTallestChild(previousRow);
                    current.setSize((child, parent) -> child.setY(tallestInPreviousRow.getY() + child.getHeight() + this.verticalGap));
                }

                CustomGuiScreen[] currentRow = new CustomGuiScreen[this.columns];
                currentRow[0] = current;

                for (int j = 1; j < this.columns; j++) {
                    if (i + j >= container.getChildren().size()) continue;
                    currentRow[j] = container.getChildren().get(i + j);
                    if (currentRow[j] == null) continue;
                    this.findTallestChild(currentRow).setSize((child, parent) -> child.setY(current.getY() + current.getHeight() / 2));
                }

                i += this.columns;
            }
        }
    }
}
