package net.reverend.fruit_tree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.*;

/**
 * <h6>Class for transferring {@link DefaultMutableTreeNode} data</h6>
 * <ul>
 * <li>Deals with drag and drop, and</li>
 * <li>Cut, Copy, and Paste</li>
 * </ul>
 * Modified version of code from <a href="https://stackoverflow.com/questions/4588109/drag-and-drop-nodes-in-jtree">Stack Overflow</a>
 * which was in turn code edited from <a href="https://coderanch.com/t/346509/java/JTree-drag-drop-tree-Java">coderanch</a>
 * by Craig Wood
 *
 */
class TreeTransferHandler extends TransferHandler {
    protected DataFlavor nodeFlavour;
    protected DataFlavor[] flavours = new DataFlavor[1];
    protected DefaultMutableTreeNode[] nodesToRemove;

    {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" +
                    DefaultMutableTreeNode[].class.getName() + "\"";
            nodeFlavour = new DataFlavor(mimeType);
            flavours[0] = nodeFlavour;
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
    }

    public boolean canImport(TransferSupport support) {
        if (!support.isDataFlavorSupported(nodeFlavour)) {
            return false;
        }
        JTree tree = (JTree) support.getComponent();
        int[] selRows = tree.getSelectionRows();
        if (selRows == null) {
            return false;
        }
        if (support.isDrop()) {
            support.setShowDropLocation(true);
            // Do not allow a drop on the drag source selections.
            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
            int dropRow = tree.getRowForPath(dl.getPath());
            for (int selRow : selRows) {
                if (selRow == dropRow) {
                    return false;
                }
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) tree.getPathForRow(selRow).getLastPathComponent();
                for (TreeNode offspring : Collections.list(treeNode.depthFirstEnumeration())) {
                    if (tree.getRowForPath(new TreePath(((DefaultMutableTreeNode) offspring).getPath())) == dropRow) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        TreePath[] paths = tree.getSelectionPaths();
        if (paths == null) {
            return null;
        }
        // Make up a node array of copies for transfer and
        // another for/of the nodes that will be removed in
        // exportDone after a successful drop.
        List<DefaultMutableTreeNode> copies = new ArrayList<>();
        List<DefaultMutableTreeNode> toRemove = new ArrayList<>();
        DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode) paths[0].getLastPathComponent();
        if (firstNode.getParent() != null) {
            toRemove.add(firstNode);
        }
        HashSet<TreeNode> doneItems = new LinkedHashSet<>(paths.length);
        DefaultMutableTreeNode copy = copy(firstNode, doneItems, tree);
        copies.add(copy);

        for (int i = 1; i < paths.length; i++) {
            DefaultMutableTreeNode next = (DefaultMutableTreeNode) paths[i].getLastPathComponent();
            if (doneItems.contains(next)) {
                continue;
            }
            // Do not allow higher level nodes to be added to list.
            if (next.getLevel() < firstNode.getLevel()) {
                break;
            } else if (next.getLevel() > firstNode.getLevel()) {  // child node
                copy.add(copy(next, doneItems, tree));
                // node already contains child
            } else {                                        // sibling
                copies.add(copy(next, doneItems, tree));
                toRemove.add(next);
            }
            doneItems.add(next);
        }
        DefaultMutableTreeNode[] nodes = copies.toArray(new DefaultMutableTreeNode[0]);
        nodesToRemove = toRemove.toArray(new DefaultMutableTreeNode[0]);
        return new NodesTransferable(nodes);
    }

    protected DefaultMutableTreeNode copy(DefaultMutableTreeNode node, HashSet<TreeNode> doneItems, JTree tree) {
        try {
            DefaultMutableTreeNode copy = new DefaultMutableTreeNode(node);
            doneItems.add(node);
            for (int i = 0; i < node.getChildCount(); i++) {
                copy.add(copy((DefaultMutableTreeNode) node.getChildAt(i), doneItems, tree));
            }
            int row = tree.getRowForPath(new TreePath(copy.getPath()));
            tree.expandRow(row);
            return copy;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void exportDone(JComponent source, Transferable data, int action) {
        if ((action & MOVE) == MOVE) {
            JTree tree = (JTree) source;
            DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
            // Remove nodes saved in nodesToRemove in createTransferable.
            for (DefaultMutableTreeNode defaultMutableTreeNode : nodesToRemove) {
                model.removeNodeFromParent(defaultMutableTreeNode);
            }
            model.reload();
        }
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    public boolean importData(TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        // Convert transfer data to nodes.
        Transferable t = support.getTransferable();
        DefaultMutableTreeNode[] transferNodes;
        try {
            transferNodes = (DefaultMutableTreeNode[]) t.getTransferData(nodeFlavour);
        } catch (UnsupportedFlavorException | IOException e) {
            System.out.printf("Exception %1s: %2s%n", e.getLocalizedMessage(), e.getCause());
            return false;
        }
        if (transferNodes.length == 0) {
            return false;
        }
        JTree tree = (JTree) support.getComponent();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        if (support.isDrop()) {
            // Get drop location info.
            JTree.DropLocation dl = (JTree.DropLocation) support.getDropLocation();
            int childIndex = dl.getChildIndex();
            TreePath destination = dl.getPath();
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode) destination.getLastPathComponent();
            // Configure for drop mode.
            int index = childIndex;    // DropMode.INSERT
            if (childIndex == -1) {     // DropMode.ON
                index = parent.getChildCount();
            }
            // Add data to model.
            for (DefaultMutableTreeNode node : transferNodes) {
                model.insertNodeInto(node, parent, index++);
            }
            return true;
        } else {
            TreePath[] selections = tree.getSelectionPaths();
            if (selections == null || selections.length == 0) {
                return false;
            }
            for (TreePath selected : selections) {
                DefaultMutableTreeNode destination = (DefaultMutableTreeNode) selected.getLastPathComponent();
                int index = destination.getChildCount();
                for (DefaultMutableTreeNode node : transferNodes) {
                    model.insertNodeInto(node, destination, index);
                    index++;
                }
                model.reload(destination);
            }
            return true;
        }
    }

    public String toString() {
        return getClass().getName();
    }

    /**
     * Data transport class
     */
    public class NodesTransferable implements Transferable {
        DefaultMutableTreeNode[] transferableNodes;

        public NodesTransferable(DefaultMutableTreeNode[] nodes) {
            this.transferableNodes = nodes;
        }


        public Object getTransferData(DataFlavor flavour) throws UnsupportedFlavorException {
            if (flavour == null || !isDataFlavorSupported(flavour)) {
                throw new UnsupportedFlavorException(flavour);
            }
            return transferableNodes;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return flavours;
        }

        public boolean isDataFlavorSupported(DataFlavor flavour) {
            return nodeFlavour.equals(flavour);
        }
    }
}
