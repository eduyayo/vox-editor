package com.pigdroid.vox.editor;

import javax.swing.JPanel;
import java.awt.event.MouseEvent;

public interface Tool {
    String getName();
    JPanel getOptionsPanel();
    void onMousePressed(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel);
    void onMouseDragged(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel);
    void onMouseReleased(MouseEvent e);
}
