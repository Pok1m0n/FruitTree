package net.reverend.fruit_tree;

import javax.swing.*;

public class FruitBar extends JToolBar {
    public FruitBar(FruitTree tree) {
        super();
        setRollover(true);
        ActionMap am = tree.getActionMap();
        add(am.get("cut"));
        add(am.get("copy"));
        add(am.get("clone"));
        add(am.get("paste"));
        addSeparator();
        add(am.get("edit"));
        addSeparator();
        add(am.get("delete"));
        addSeparator();
        add(am.get("undo"));
        add(am.get("redo"));
        add(am.get("refresh"));
    }
}
