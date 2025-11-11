package net.reverend.fruit_tree;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * <h6>A simple extension of the {@link DefaultTreeCellRenderer}</h6>
 * <p>A framework for writing a custom renderer with some commonly overridden methods provided.</p>
 */
public class FruitRenderer extends DefaultTreeCellRenderer {
    private final boolean useDefaultRenderer;
    public FruitRenderer() {
        this(true);
    }
    public FruitRenderer(boolean useDefaultRenderer) {
        super();
        this.useDefaultRenderer = useDefaultRenderer;
    }
    @Override
    public Component getTreeCellRendererComponent(
                JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        JLabel defaultLabel = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
        if(useDefaultRenderer){
            return defaultLabel;
        }
        if(value instanceof Fruit fruit){
            if(!fruit.hasChildren()){
                defaultLabel.setIcon(getLeafIcon());
            }
            if(fruit.getDisplayName() != null){
                defaultLabel.setText(fruit.getDisplayName());
            }
        }
        return defaultLabel;
    }

    private boolean getIsDropCell(JTree tree, int row){
        JTree.DropLocation dropLocation = tree.getDropLocation();
        return dropLocation != null && dropLocation.getChildIndex() == -1
                && tree.getRowForPath(dropLocation.getPath()) == row;
    }

    @Override
    public void setOpenIcon(Icon newIcon) {
        super.setOpenIcon(newIcon);
    }

    @Override
    public void setClosedIcon(Icon newIcon) {
        super.setClosedIcon(newIcon);
    }

    @Override
    public void setLeafIcon(Icon newIcon) {
        super.setLeafIcon(newIcon);
    }

    @Override
    public void setTextSelectionColor(Color newColor) {
        super.setTextSelectionColor(newColor);
    }

    @Override
    public void setTextNonSelectionColor(Color newColor) {
        super.setTextNonSelectionColor(newColor);
    }

    @Override
    public void setBackgroundSelectionColor(Color newColor) {
        super.setBackgroundSelectionColor(newColor);
    }

    @Override
    public void setBackgroundNonSelectionColor(Color newColor) {
        super.setBackgroundNonSelectionColor(newColor);
    }

    @Override
    public void setBorderSelectionColor(Color newColor) {
        super.setBorderSelectionColor(newColor);
    }

    @Override
    public void setFont(Font font) {
        super.setFont(font);
    }

    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
    }
}
