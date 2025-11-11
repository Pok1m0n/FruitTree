package net.reverend.fruit_tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.undo.StateEditable;
import java.util.Hashtable;

/**
 * <h6>Extension of {@link DefaultMutableTreeNode} implementing {@link StateEditable}.</h6>
 * <p>Contains optional fields:
 * <ul>
 *     <li>displayName - for over-riding display of user-object as string</li>
 *     <li>For over-riding default icons:<ul>
 *         <li>leafIcon</li>
 *         <li>branchIcon</li>
 *     </ul></li>
 * </ul></p>
 */
@SuppressWarnings("unused")
public class Fruit extends DefaultMutableTreeNode implements StateEditable {
    private String displayName = null;
    private Icon leafIcon = null;
    private Icon branchIcon = null;

    public Fruit() {
        super();
    }

    public Fruit(Fruit fruit) {
        this(
                fruit.getUserObject(),
                fruit.getAllowsChildren(),
                fruit.getDisplayName(),
                fruit.getLeafIcon(),
                fruit.getBranchIcon()
        );
        fruit.children().asIterator().forEachRemaining(child -> {
            if (child instanceof Fruit fruit_) {
                add(new Fruit(fruit_));
            }
        });
    }

    public Fruit(DefaultMutableTreeNode mutableTreeNode) {
        this(
                mutableTreeNode.getUserObject(),
                mutableTreeNode.getAllowsChildren()
        );
        if (mutableTreeNode instanceof Fruit fruit) {
            setDisplayName(fruit.getDisplayName());
            setLeafIcon(fruit.getLeafIcon());
            setBranchIcon(fruit.getBranchIcon());
        }
        mutableTreeNode.children().asIterator().forEachRemaining(child -> {
            if (child instanceof DefaultMutableTreeNode node) {
                add(new Fruit(node));
            }
        });
    }

    public Fruit(Object userObject) {
        super(userObject);
    }

    public Fruit(Object userObject, boolean allowsChildren) {
        super(userObject, allowsChildren);
    }

    public Fruit(String displayName) {
        super();
        this.displayName = displayName;
    }

    public Fruit(Object userObject, String displayName) {
        super(userObject);
        this.displayName = displayName;
    }

    public Fruit(Object userObject, boolean allowsChildren, String displayName) {
        super(userObject, allowsChildren);
        this.displayName = displayName;
    }

    public Fruit(Object userObject, boolean allowsChildren, String displayName, Icon leafIcon, Icon branchIcon) {
        super(userObject, allowsChildren);
        this.displayName = displayName;
        this.branchIcon = branchIcon;
        this.leafIcon = leafIcon;
    }

    public boolean hasChildren() {
        return getChildCount() != 0;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Icon getLeafIcon() {
        return leafIcon;
    }

    public void setLeafIcon(Icon leafIcon) {
        this.leafIcon = leafIcon;
    }

    public Icon getBranchIcon() {
        return branchIcon;
    }

    public void setBranchIcon(Icon branchIcon) {
        this.branchIcon = branchIcon;
    }

    @Override
    public void storeState(Hashtable<Object, Object> state) {
        if (getAllowsChildren()) {
            state.put("allowsChildren", true);
            if (children != null && !children.isEmpty()) {
                state.put("children", children);
            }
        } else {
            state.put("allowsChildren", false);
        }
        if (userObject != null) {
            state.put("userObject", userObject);
        }
        if (parent != null) {
            state.put("parent", getParent());
            state.put("index", parent.getIndex(this));
        }
    }

    @Override
    public void restoreState(Hashtable<?, ?> state) {
        if (state.containsKey("parent") && state.get("parent") instanceof Fruit fruit) {
            fruit.insert(this, (int) state.get("index"));
        }
        if (state.containsKey("allowsChildren") && state.get("allowsChildren") instanceof Boolean fecund) {
            setAllowsChildren(fecund);
        }
        if (state.containsKey("children") && state.get("children") instanceof java.util.Vector<?> vector) {
            vector.forEach(child -> {
                if (child instanceof Fruit fruit) {
                    add(fruit);
                }
            });
        }
        if (state.containsKey("userObject")) {
            setUserObject(state.get("userObject"));
        }
    }
}
