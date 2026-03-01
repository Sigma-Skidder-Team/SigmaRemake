package io.github.sst.remake.gui.framework.layout;

import io.github.sst.remake.gui.framework.core.GuiComponent;

public class FlowWrapLayoutVisitor implements GuiComponentVisitor {
    public int spacing;

    public FlowWrapLayoutVisitor(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void visit(GuiComponent container) {
        if (container.getChildren().isEmpty()) {
            return;
        }

        int x = 0;
        int y = 0;
        int rowHeight = 0;

        for (int i = 0; i < container.getChildren().size(); i++) {
            GuiComponent child = container.getChildren().get(i);

            if (x + child.getWidth() + this.spacing > container.getWidth()) {
                x = 0;
                y += rowHeight;
                // TODO: Consider resetting rowHeight to 0
            }

            child.setY(y);
            child.setX(x);

            x += child.getWidth() + this.spacing;
            rowHeight = Math.max(child.getHeight(), rowHeight);
        }
    }
}
