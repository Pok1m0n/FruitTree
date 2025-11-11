package net.reverend;

import net.reverend.fruit_tree.Fruit;
import net.reverend.fruit_tree.FruitTree;

import javax.swing.*;
import java.awt.*;

public class Main {
    public static void main(String[] args) {
        Fruit rootFruit = new Fruit();
        rootFruit.add(new Fruit("orange",false));
        rootFruit.add(new Fruit("lemon"));
        rootFruit.add(new Fruit(false));

        FruitTree fruitTree = new FruitTree(rootFruit);
        JToolBar toolBar = fruitTree.getToolbar();

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();

            frame.setMinimumSize(new Dimension(400, 600));
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(toolBar, BorderLayout.NORTH);
            frame.add(fruitTree, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationByPlatform(true);
            frame.setVisible(true);

        });

    }
}