package net.reverend.fruit_tree;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.undo.StateEdit;
import javax.swing.undo.StateEditable;
import javax.swing.undo.UndoManager;
import java.awt.event.ActionEvent;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FruitTree extends JTree implements StateEditable {
    private static final Logger log = LoggerFactory.getLogger(FruitTree.class);
    private static final FruitRenderer FRUIT_RENDERER = new FruitRenderer(false);
    private final UndoManager undoManager = new UndoManager();
    private final AtomicBoolean undoRedoing = new AtomicBoolean(false); // Toggle storing state
    private final FruitEditor fruitEditor = new FruitEditor();

    {
        fruitEditor.setWarnOnEmptyValue(true);
        fruitEditor.setWarnOnEmptyValueText("Blank values might be bad.");
    }

    private EditState stateEdit;
    private final JToolBar toolbar;

    public FruitTree(Fruit root) {
        super(root, true);
        setName("FruitTree");
        setCellRenderer(FRUIT_RENDERER);
        setCellEditor(fruitEditor);

        getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        setAutoscrolls(true);
        setBorder(BorderFactory.createCompoundBorder(
                super.getBorder(),
                BorderFactory.createEmptyBorder(4, 4, 8, 4)));

        setTransferHandler(new TreeTransferHandler());
        setDragEnabled(true);
        setDropMode(DropMode.ON_OR_INSERT);
        setEditable(true);

        ActionMap actionMap = this.getActionMap();
        actionMap.put(UNDO.getValue(Action.NAME), UNDO);
        actionMap.put(REDO.getValue(Action.NAME), REDO);
        actionMap.put(CLONE.getValue(Action.NAME), CLONE);
        actionMap.put(COPY.getValue(Action.NAME), COPY);
        actionMap.put(CUT.getValue(Action.NAME), CUT);
        actionMap.put(DELETE.getValue(Action.NAME), DELETE);
        actionMap.put(EDIT.getValue(Action.NAME), EDIT);
        actionMap.put(PASTE.getValue(Action.NAME), PASTE);
        actionMap.put(REFRESH.getValue(Action.NAME), REFRESH);

        InputMap imap = this.getInputMap();
        imap.put(KeyStroke.getKeyStroke("DELETE"), DELETE.getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke("ctrl E"), EDIT.getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke("ctrl X"), CUT.getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke("ctrl C"), COPY.getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke("ctrl shift C"), CLONE.getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke("ctrl V"), PASTE.getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke("ctrl Z"), UNDO.getValue(Action.NAME));
        imap.put(KeyStroke.getKeyStroke("ctrl Y"), REDO.getValue(Action.NAME));

        setExpandsSelectedPaths(true);
        setInheritsPopupMenu(true);
        setRootVisible(true);
        setShowsRootHandles(false);

        setSelectionRow(0);

        new UndoRedoListeners().addUndoRedoListeners();

        toolbar = new FruitBar(this);
        stateEdit = new EditState(this);
    }

    public JToolBar getToolbar() {
        return toolbar;
    }

    /**
     * Convenience class to hold the listeners that update the state and add it to the UndoManager
     */
    private class UndoRedoListeners {
        private void addUndoRedoListeners() {
            FruitTree.this.addTreeSelectionListener(treeSelectionListener);
            FruitTree.this.addTreeExpansionListener(treeExpansionListener);
            cellEditor.addCellEditorListener(editorListener);
            FruitTree.this.getModel().addTreeModelListener(treeModelListener);
        }

        private final CellEditorListener editorListener = new CellEditorListener() {
            @Override
            public void editingStopped(ChangeEvent e) {
                undoManager.addEdit(fruitEditor.getUndoableEdit());
            }

            @Override
            public void editingCanceled(ChangeEvent e) {
            }
        };
        private final TreeModelListener treeModelListener = new TreeModelListener() {
            @Override
            public void treeNodesChanged(TreeModelEvent e) {
                // ignore this, it is covered by the edit listener
            }

            @Override
            public void treeNodesInserted(TreeModelEvent e) {
                if (undoRedoing.get()) {
                    // don't store state if we are undoing/redoing
                    return;
                }
                stateEdit.setPresentationName("Node(s) added");
                stateEdit.end();
                undoManager.addEdit(stateEdit);
                stateEdit = new EditState(FruitTree.this);
            }

            @Override
            public void treeNodesRemoved(TreeModelEvent e) {
                if (undoRedoing.get()) {
                    // don't store state if we are undoing/redoing
                    return;
                }

                stateEdit.setPresentationName("Node(s) removed");
                stateEdit.end();
                undoManager.addEdit(stateEdit);
                stateEdit = new EditState(FruitTree.this);
            }

            @Override
            public void treeStructureChanged(TreeModelEvent e) {
                if (undoRedoing.get()) {
                    // don't store state if we are undoing/redoing
                    return;
                }

                stateEdit.setPresentationName("Node(s) moved");
                stateEdit.end();
                undoManager.addEdit(stateEdit);
                stateEdit = new EditState(FruitTree.this);
            }
        };
        @SuppressWarnings("FieldCanBeLocal")
        private final TreeSelectionListener treeSelectionListener = e -> {
            if (undoRedoing.get() || !e.isAddedPath()) {
                return;
            }

            stateEdit.setPresentationName("Change selection");
            stateEdit.end();
            undoManager.addEdit(stateEdit);
            stateEdit = new EditState(FruitTree.this);
        };

        private final TreeExpansionListener treeExpansionListener = new TreeExpansionListener() {
            @Override
            public void treeExpanded(TreeExpansionEvent event) {
                if (undoRedoing.get()) {
                    return;
                }

                stateEdit.setPresentationName("Expand row(s)");
                stateEdit.end();
                undoManager.addEdit(stateEdit);
                stateEdit = new EditState(FruitTree.this);
            }

            @Override
            public void treeCollapsed(TreeExpansionEvent event) {
                if (undoRedoing.get()) {
                    return;
                }

                stateEdit.setPresentationName("Collapse row(s)");
                stateEdit.end();
                undoManager.addEdit(stateEdit);
                stateEdit = new EditState(FruitTree.this);
            }
        };
    }

    /**
     * Convenience class that allows setting the display text after creation.
     */
    private static class EditState extends StateEdit {
        public EditState(StateEditable anObject) {
            super(anObject);
        }

        public void setPresentationName(String text) {
            this.undoRedoName = text;
        }
    }

    @Override
    public void storeState(Hashtable<Object, Object> state) {
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
        state.put("root", new Fruit(root));
        TreePath[] selection = getSelectionPaths();
        if (selection == null) {
            selection = new TreePath[0];
        }
        state.put("selection", selection);
        TreePath rootPath = new TreePath(root);
        Enumeration<TreePath> expanded = getExpandedDescendants(rootPath);
        if (expanded == null) {
            expanded = Collections.emptyEnumeration();
        }
        state.put("expanded", expanded);
    }

    @Override
    public void restoreState(Hashtable<?, ?> state) {
        undoRedoing.set(true);
        if (state.containsKey("root") && state.get("root") instanceof DefaultMutableTreeNode root) {
            setModel(new DefaultTreeModel(root));
        }
        if (state.containsKey("expanded") && state.get("expanded") instanceof Enumeration<?> enumeration) {
            while (enumeration.hasMoreElements()) {
                Object o = enumeration.nextElement();
                if (o instanceof TreePath path) {
                    setExpandedState(path, true);
                }
            }
        }
        if (state.containsKey("selection") && state.get("selection") instanceof TreePath[] paths) {
            setSelectionPaths(paths);
        }
        undoRedoing.set(false);
    }

    public final Action COPY = TransferHandler.getCopyAction();
    public final Action CUT = TransferHandler.getCutAction();
    public final Action PASTE = TransferHandler.getPasteAction();
    public final Action EDIT = new AbstractAction("edit", null) {
        @Override
        public void actionPerformed(ActionEvent e) {
            getUI().startEditingAtPath(FruitTree.this, getSelectionPath());
        }
    };
    public final Action REFRESH = new AbstractAction("refresh", null) {
        @Override
        public void actionPerformed(ActionEvent e) {
            undoRedoing.set(true);
            ((DefaultTreeModel) FruitTree.this.getModel()).reload();
            undoRedoing.set(false);
        }
    };
    public final Action UNDO = new AbstractAction("undo", null) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (undoManager.canUndo()) {
                undoRedoing.set(true);
                undoManager.undo();
                undoRedoing.set(false);
            }
            putValue(Action.SHORT_DESCRIPTION, undoManager.getUndoPresentationName());
        }
    };
    public final Action REDO = new AbstractAction("redo", null) {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (undoManager.canRedo()) {
                undoRedoing.set(true);
                undoManager.redo();
                undoRedoing.set(false);
            }
            putValue(Action.SHORT_DESCRIPTION, undoManager.getRedoPresentationName());
        }
    };
    private final Action DELETE = new AbstractAction("delete", null) {
        TreePath[] selectionPaths;

        @Override
        public boolean accept(Object sender) {
            selectionPaths = FruitTree.this.getSelectionPaths();
            if (selectionPaths == null || selectionPaths.length == 0) {
                return false;
            }
            for (TreePath tp : selectionPaths) {
                if (((DefaultMutableTreeNode) tp.getLastPathComponent()).isRoot()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (!accept(FruitTree.this)) {
                return;
            }
            DefaultTreeModel model = (DefaultTreeModel) FruitTree.this.getModel();
            for (TreePath tp : selectionPaths) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) tp.getLastPathComponent();
                TreeNode parent = node.getParent();
                model.removeNodeFromParent(node);
                undoRedoing.set(true);
                model.reload(parent);
                undoRedoing.set(false);
            }

        }
    };

    private final Action CLONE = new AbstractAction("clone", null) {
        TreePath[] selectionPaths;
        DefaultMutableTreeNode commonAncestor;

        @Override
        public boolean accept(Object sender) {
            selectionPaths = FruitTree.this.getSelectionPaths();
            if (selectionPaths == null || selectionPaths.length == 0) {
                return false;
            }
            List<DefaultMutableTreeNode> nodeList = new ArrayList<>();
            for (TreePath treePath : selectionPaths) {
                nodeList.add((DefaultMutableTreeNode) treePath.getLastPathComponent());
            }
            commonAncestor = (DefaultMutableTreeNode) nodeList.getFirst().getParent();
            if (commonAncestor == null) {
                return false;
            }
            for (int i = 1; i < nodeList.size(); i++) {
                commonAncestor = (DefaultMutableTreeNode) nodeList.get(i).getSharedAncestor(commonAncestor);
            }
            return commonAncestor != null;
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (!accept(e.getSource())) {
                return;
            }
            List<DefaultMutableTreeNode> transferNodes = new ArrayList<>();
            for (TreePath tp : selectionPaths) {
                transferNodes.add((DefaultMutableTreeNode) tp.getLastPathComponent());
            }
            DefaultTreeModel model = (DefaultTreeModel) FruitTree.this.getModel();
            int index = commonAncestor.getIndex(transferNodes.getFirst());
            for (DefaultMutableTreeNode node : transferNodes) {
                model.insertNodeInto(new Fruit(node), commonAncestor, index);
            }
            model.reload(commonAncestor);
            undoRedoing.set(true); // do not add to undo
            setSelectionPaths(selectionPaths);
            undoRedoing.set(false);
        }
    };

    {
        CLONE.putValue(Action.ACTION_COMMAND_KEY, "CLONE");
        CLONE.putValue(Action.SHORT_DESCRIPTION, "Clone selected nodes");
        DELETE.putValue(Action.ACTION_COMMAND_KEY, "DELETE");
        DELETE.putValue(Action.SHORT_DESCRIPTION, "Delete selected nodes");
        EDIT.putValue(Action.ACTION_COMMAND_KEY, "EDIT");
        EDIT.putValue(Action.SHORT_DESCRIPTION, "Edit selected nodes");
        REDO.putValue(Action.ACTION_COMMAND_KEY, "REDO");
        UNDO.putValue(Action.ACTION_COMMAND_KEY, "UNDO");
    }
}
