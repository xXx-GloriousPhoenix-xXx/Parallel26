package billiard;

import javax.swing.*;
import java.awt.*;

public class Styler {
    private final Color green = new Color(30, 109, 82);
    private final Color brown = new Color(119, 56, 38);
    public JButton StyleButton(JButton button) {
        button.setBackground(green);
        button.setForeground(brown);

        button.setContentAreaFilled(false);
        button.setOpaque(true);

        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(green, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        button.setFont(new Font("Times New Roman", Font.BOLD, 14));

        button.setFocusPainted(false);
        UIManager.put("Button.disabledText", brown.darker());

        return button;
    }
    public JLabel StyleLabel(JLabel label) {
        label.setOpaque(true);
        label.setBackground(green);
        label.setForeground(brown);

        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(green, 1),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));

        label.setFont(new Font("Times New Roman", Font.BOLD, 14));

        return label;
    }
}
