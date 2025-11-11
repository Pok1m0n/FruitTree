package net.reverend.fruit_tree;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import javax.swing.tree.TreeCellEditor;
import javax.swing.undo.StateEdit;
import java.awt.*;

import static javax.swing.JOptionPane.WARNING_MESSAGE;

/**
 * <h6>An extension of {@link AbstractCellEditor}</h6>
 * <p>Has a slightly friendlier editor interface with the inclusion of;
 * <ul>
 * <li>Save and Cancel buttons, and</li>
 * <li>Formatted Text Field instead of straight Text Field to allow input verification, and</li>
 * <li>Clear button incorporated into the text field, and</li>
 * <li>Radio buttons that replace the text field for boolean user objects, and</li>
 * <li>Optional warning message when saving an empty text field.</li>
 * </ul>
 * Each node edit is also stored in a {@link StateEdit} field obtainable with {@link #getUndoableEdit()}
 * </p>
 */
public class FruitEditor extends AbstractCellEditor implements TreeCellEditor {
    private static final int VALUE_FTF_COLUMNS = 24;
    private static final int TEXT_FIELD_BUTTON_GAP = 4;
    private static final Border EDITOR_BORDER = UIManager.getDefaults().getBorder("FormattedTextField.border");
    private static final Border TREE_EDITOR_BORDER = UIManager.getDefaults().getBorder("Tree.editorBorder");
    private static final Color TREE_EDITOR_BACKGROUND = UIManager.getDefaults().getColor("Tree.dropCellBackground");
    private static final JButton CLEAR_VALUE_BUTTON = createClearButton();

    private static final JRadioButton FALSE_RADIO = new JRadioButton("False");
    private static final JRadioButton TRUE_RADIO = new JRadioButton("True");
    private static final JButton SAVE_BUTTON = new JButton("Save");
    private static final JButton CANCEL_BUTTON = new JButton("Cancel");
    private static final JLabel VALUE_LABEL = new JLabel("Value");
    private static final JFormattedTextField VALUE_FTF = new JFormattedTextField();
    private static final JPanel CONTAINER = new JPanel();
    private static final InputVerifier INPUT_VERIFIER = null;

    private JTree tree;
    private boolean isBoolean = false;
    private boolean warnOnEmptyValue = false;
    String warnOnEmptyValueText = "Value is blank. This may have unintended consequences.";
    private static Icon clearTextFieldIcon = null;
    private static Fruit fruit;
    private StateEdit edit = null;
    public FruitEditor() {
        initComponents();
    }
    public StateEdit getUndoableEdit(){
        return edit;
    }
    @Override
    public Object getCellEditorValue() {
        return fruit.getUserObject();
    }

    @Override
    public void cancelCellEditing() {
        edit = null;
        super.cancelCellEditing();
    }

    @Override
    public boolean stopCellEditing() {
        if (warnOnEmptyValue && !isBoolean && VALUE_FTF.getText().isBlank()) {
            int choice = JOptionPane.showConfirmDialog(tree, warnOnEmptyValueText, "", JOptionPane.OK_CANCEL_OPTION, WARNING_MESSAGE);
            if (choice != 0) {
                return false;
            }
        }
        if (isBoolean) {
            fruit.setUserObject(TRUE_RADIO.isSelected());
        } else {
            String text = VALUE_FTF.getText();
            if(text.isBlank()){
                fruit.setUserObject(null);
            } else if (fruit.getUserObject() != null && fruit.getUserObject().getClass().isPrimitive()) {
                try {
                    fruit.setUserObject(
                            fruit.getUserObject()
                                    .getClass().cast(text)
                    );
                } catch (ClassCastException e) {
                    fruit.setUserObject(text);
                }
            } else {
                fruit.setUserObject(text);
            }
        }
        edit.end();
        return super.stopCellEditing();
    }

    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value,
                                                boolean isSelected, boolean expanded,
                                                boolean leaf, int row) {
        this.tree = tree;
        if (value instanceof Fruit fruit_) {
            FruitEditor.fruit = fruit_;
            setContent(fruit_.getUserObject());
            edit = new StateEdit( fruit_, "Edit node");
            return CONTAINER;
        } else {
            throw new IllegalArgumentException("FruitEditor only works on fruit.");
        }
    }

    private void setContent(Object userObject) {
        if (userObject instanceof Boolean booleanValue) {
            isBoolean = true;
            VALUE_FTF.setVisible(false);
            FALSE_RADIO.setVisible(true);
            TRUE_RADIO.setVisible(true);
            if (booleanValue) {
                TRUE_RADIO.setSelected(true);
            } else {
                FALSE_RADIO.setSelected(true);
            }
        } else {
            isBoolean = false;
            VALUE_FTF.setVisible(true);
            if(userObject != null) {
                VALUE_FTF.setText(userObject.toString());
            }

            FALSE_RADIO.setVisible(false);
            TRUE_RADIO.setVisible(false);
        }
    }

    private void initComponents() {
        CANCEL_BUTTON.addActionListener(e -> this.cancelCellEditing());
        SAVE_BUTTON.addActionListener(e -> this.stopCellEditing());

        initContainer();

        // Radio buttons
        ButtonGroup group = new ButtonGroup();
        group.add(FALSE_RADIO);
        group.add(TRUE_RADIO);
        FALSE_RADIO.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        FALSE_RADIO.setOpaque(false);
        TRUE_RADIO.setOpaque(false);

        // Text field
        VALUE_LABEL.setLabelFor(VALUE_FTF);
        VALUE_FTF.setColumns(VALUE_FTF_COLUMNS);
        VALUE_FTF.setBorder(null);
        ComponentBorder cbValue = new ComponentBorder(CLEAR_VALUE_BUTTON);
        cbValue.setAdjustInsets(true);
        cbValue.setGap(TEXT_FIELD_BUTTON_GAP);
        cbValue.install(VALUE_FTF);
        VALUE_FTF.setBorder(BorderFactory.createCompoundBorder(EDITOR_BORDER, cbValue));
        VALUE_FTF.setInputVerifier(INPUT_VERIFIER);
    }


    private void initContainer() {
        GridBagLayout lm = new GridBagLayout();
        Insets insets = new Insets(1, 2, 2, 2);
        GridBagConstraints GBC_VALUE_LABEL = new GridBagConstraints();
        GridBagConstraints GBC_VALUE_FIELD = new GridBagConstraints();
        GridBagConstraints GBC_FALSE_RADIO = new GridBagConstraints();
        GridBagConstraints GBC_TRUE_RADIO = new GridBagConstraints();
        GridBagConstraints GBC_SAVE = new GridBagConstraints();
        GridBagConstraints GBC_CANCEL = new GridBagConstraints();
        GBC_VALUE_LABEL.gridx = 0;
        GBC_VALUE_LABEL.gridy = 0;
        GBC_VALUE_LABEL.insets = insets;
        GBC_VALUE_FIELD.gridx = 1;
        GBC_VALUE_FIELD.gridy = 0;
        GBC_VALUE_FIELD.gridwidth = 2;
        GBC_VALUE_FIELD.insets = insets;
        GBC_FALSE_RADIO.gridx = 1;
        GBC_FALSE_RADIO.gridy = 0;
        GBC_TRUE_RADIO.gridx = 2;
        GBC_TRUE_RADIO.gridy = 0;
        GBC_CANCEL.gridx = 1;
        GBC_CANCEL.gridy = 1;
        GBC_CANCEL.insets = insets;
        GBC_SAVE.gridx = 2;
        GBC_SAVE.gridy = 1;
        GBC_SAVE.insets = insets;
        GBC_VALUE_LABEL.anchor = GridBagConstraints.BASELINE_TRAILING;
        GBC_VALUE_FIELD.anchor = GridBagConstraints.BASELINE_LEADING;
        GBC_CANCEL.anchor = GridBagConstraints.BASELINE_LEADING;
        GBC_SAVE.anchor = GridBagConstraints.BASELINE_LEADING;
        GBC_FALSE_RADIO.anchor = GridBagConstraints.CENTER;
        GBC_TRUE_RADIO.anchor = GridBagConstraints.CENTER;
        CONTAINER.setLayout(lm);
        CONTAINER.add(VALUE_LABEL, GBC_VALUE_LABEL);
        CONTAINER.add(VALUE_FTF, GBC_VALUE_FIELD);
        CONTAINER.add(FALSE_RADIO, GBC_FALSE_RADIO);
        CONTAINER.add(TRUE_RADIO, GBC_TRUE_RADIO);
        CONTAINER.add(CANCEL_BUTTON, GBC_CANCEL);
        CONTAINER.add(SAVE_BUTTON, GBC_SAVE);
        CONTAINER.setBorder(BorderFactory.createCompoundBorder(TREE_EDITOR_BORDER, BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        CONTAINER.setBackground(TREE_EDITOR_BACKGROUND);
    }

    private static JButton createClearButton() {
        JButton button;
        if (clearTextFieldIcon == null) {
            button = new JButton("X");
            button.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
        } else {
            button = new JButton(clearTextFieldIcon);
            button.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        }
        button.setOpaque(false);
        button.setBackground(null);
        button.setContentAreaFilled(false);
        button.addActionListener(e -> ((JTextComponent) ((JComponent) e.getSource()).getParent()).setText(""));
        return button;
    }

    public void setWarnOnEmptyValue(boolean warnOnEmptyValue) {
        this.warnOnEmptyValue = warnOnEmptyValue;
    }

    public void setWarnOnEmptyValueText(String warnOnEmptyValueText) {
        this.warnOnEmptyValueText = warnOnEmptyValueText;
    }

    public void setClearTextFieldIcon(Icon textFieldClearIcon) {
        clearTextFieldIcon = textFieldClearIcon;
    }
}