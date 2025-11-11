package net.reverend.fruit_tree;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;

@SuppressWarnings("unused")
public class ClearableFormattedTextField extends JFormattedTextField implements PropertyChangeListener{
    private static final Border EDITOR_BORDER = UIManager.getDefaults().getBorder("FormattedTextField.border");
    private static Color editorBackgroundValid = UIManager.getDefaults().getColor("FormattedTextField.background");
    private static Color editorBackgroundInvalid = Color.red.brighter().brighter().brighter();

    private static int textFieldButtonGap = 4;
    private static Icon clearIcon = null;
    protected final JButton CLEAR_BUTTON;
    private AbstractFormatter formatter;
    protected ClearableFormattedTextField() {
        super();
        CLEAR_BUTTON = createClearButton();
        ComponentBorder componentBorder = new ComponentBorder(CLEAR_BUTTON);
        componentBorder.setAdjustInsets(true);
        componentBorder.setGap(textFieldButtonGap);
        componentBorder.install(this);
        this.setBorder(BorderFactory.createCompoundBorder(EDITOR_BORDER, componentBorder));
        this.setInputVerifier(null);
        addPropertyChangeListener(this);
    }

    @Override
    public void setInputVerifier(InputVerifier inputVerifier) {
        if (inputVerifier != null) {
            inputVerifier.verifyTarget(this);
        }
        super.setInputVerifier(inputVerifier);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        setBackground(enabled ?
                editorBackgroundValid :
                editorBackgroundValid.darker());
        CLEAR_BUTTON.setEnabled(enabled);
    }

    @Override
    public boolean isEditValid() {
        return super.isEditValid();
    }

    protected void updateAppearance() {
        if (getFormatter() != null) {
            removePropertyChangeListener(this);
            try {
                getFormatter().valueToString(this.getText());
            } catch (ParseException ignored){}
            addPropertyChangeListener(this);
        }
        if (isEnabled() && getInputVerifier() != null) {
            setBackground(getInputVerifier().verify(this) ? editorBackgroundValid : editorBackgroundInvalid);
        } else {
            setBackground(editorBackgroundValid);
        }
    }

    private static JButton createClearButton() {
        JButton button;
        if (clearIcon == null) {
            button = new JButton("X");
        } else {
            button = new JButton(clearIcon);
        }
        button.setOpaque(false);
        button.setBackground(null);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        button.addActionListener(e -> ((JTextComponent) ((JComponent) e.getSource()).getParent()).setText(""));
        return button;
    }

    public static Icon getClearIcon() {
        return clearIcon;
    }

    public static void setClearIcon(Icon clearIcon) {
        ClearableFormattedTextField.clearIcon = clearIcon;
    }

    public static void setEditorBackgroundValid(Color editorBackgroundValid) {
        ClearableFormattedTextField.editorBackgroundValid = editorBackgroundValid;
    }

    public static void setEditorBackgroundInvalid(Color editorBackgroundInvalid) {
        ClearableFormattedTextField.editorBackgroundInvalid = editorBackgroundInvalid;
    }

    public static void setTextFieldButtonGap(int textFieldButtonGap) {
        ClearableFormattedTextField.textFieldButtonGap = textFieldButtonGap;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("value")) {
            System.out.println(ClearableFormattedTextField.this.getName() + ":" + evt.getNewValue());
            ClearableFormattedTextField.this.updateAppearance();
        }
    }
}

