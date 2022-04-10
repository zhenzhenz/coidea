package sse.tongji.coidea.view;

import javax.swing.*;
import java.awt.*;

public class CollaboratorListCellRenderer extends DefaultListCellRenderer {
        @Override

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            c.setBackground(DALAwarenessPrinter.generateColorFromUserName((String) value));

            return c;

        }

    }