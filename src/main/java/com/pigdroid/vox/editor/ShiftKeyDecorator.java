package com.pigdroid.vox.editor;

import javax.swing.JPanel;
import java.awt.event.MouseEvent;

public class ShiftKeyDecorator implements Tool {
    private final Tool wrapped;
    private Integer startX;
    private Integer startY;
    private GridPanel lastGridPanel;

    private enum Direction { NONE, HORIZONTAL, VERTICAL, DIAGONAL }
    private Direction lockedDirection = Direction.NONE;

    public ShiftKeyDecorator(Tool wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public String getName() {
        return wrapped.getName();
    }

    @Override
    public JPanel getOptionsPanel() {
        return wrapped.getOptionsPanel();
    }

    @Override
    public void onMousePressed(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        startX = e.getX();
        startY = e.getY();
        lastGridPanel = gridPanel;
        lockedDirection = Direction.NONE;
        wrapped.onMousePressed(e, viewName, model, gridPanel);
    }

    @Override
    public void onMouseDragged(MouseEvent e, String viewName, VoxelModel model, GridPanel gridPanel) {
        MouseEvent eventToPass = e;
        if (e.isShiftDown() && startX != null && startY != null) {
            eventToPass = constrainEvent(e, gridPanel);
        }
        wrapped.onMouseDragged(eventToPass, viewName, model, gridPanel);
    }

    @Override
    public void onMouseReleased(MouseEvent e) {
        MouseEvent eventToPass = e;
        if (e.isShiftDown() && startX != null && startY != null && lastGridPanel != null) {
            eventToPass = constrainEvent(e, lastGridPanel);
        }
        wrapped.onMouseReleased(eventToPass);
        startX = null;
        startY = null;
        lockedDirection = Direction.NONE;
        lastGridPanel = null;
    }

    private MouseEvent constrainEvent(MouseEvent e, GridPanel gridPanel) {
        int gridSize = gridPanel.getGridSize();
        int originX = gridPanel.getWidth() / 2 + gridPanel.getPanX();
        int originY = gridPanel.getHeight() / 2 + gridPanel.getPanY();

        int startGridX = Math.floorDiv(startX - originX, gridSize);
        int startGridY = Math.floorDiv(originY - startY, gridSize);

        int currGridX = Math.floorDiv(e.getX() - originX, gridSize);
        int currGridY = Math.floorDiv(originY - e.getY(), gridSize);

        int dGridX = currGridX - startGridX;
        int dGridY = currGridY - startGridY;
        int absDGridX = Math.abs(dGridX);
        int absDGridY = Math.abs(dGridY);

        int newX = e.getX();
        int newY = e.getY();

        if (wrapped.getName().equals("Brush") || wrapped.getName().equals("Paint")) {
            if (lockedDirection == Direction.NONE) {
                if (absDGridX > 0 || absDGridY > 0) {
                    if (absDGridX > absDGridY * 2) {
                        lockedDirection = Direction.HORIZONTAL;
                    } else if (absDGridY > absDGridX * 2) {
                        lockedDirection = Direction.VERTICAL;
                    } else {
                        lockedDirection = Direction.DIAGONAL;
                    }
                }
            }

            if (lockedDirection == Direction.HORIZONTAL) {
                newY = originY - startGridY * gridSize - gridSize / 2;
            } else if (lockedDirection == Direction.VERTICAL) {
                newX = originX + startGridX * gridSize + gridSize / 2;
            } else if (lockedDirection == Direction.DIAGONAL) {
                int maxGrid = Math.max(absDGridX, absDGridY);
                int signX = dGridX < 0 ? -1 : 1;
                int signY = dGridY < 0 ? -1 : 1;

                int targetGridX = startGridX + maxGrid * signX;
                int targetGridY = startGridY + maxGrid * signY;

                newX = originX + targetGridX * gridSize + gridSize / 2;
                newY = originY - targetGridY * gridSize - gridSize / 2;
            } else {
                newX = startX;
                newY = startY;
            }
        } else if (wrapped.getName().equals("Circle") || wrapped.getName().equals("Square") || wrapped.getName().equals("Freeform/scale") || wrapped.getName().equals("Select")) {
            int maxGrid = Math.max(absDGridX, absDGridY);
            int signX = dGridX < 0 ? -1 : 1;
            int signY = dGridY < 0 ? -1 : 1;

            int targetGridX = startGridX + maxGrid * signX;
            int targetGridY = startGridY + maxGrid * signY;

            newX = originX + targetGridX * gridSize + gridSize / 2;
            newY = originY - targetGridY * gridSize - gridSize / 2;
        }

        return new MouseEvent(
            e.getComponent(),
            e.getID(),
            e.getWhen(),
            e.getModifiersEx(),
            newX,
            newY,
            e.getClickCount(),
            e.isPopupTrigger(),
            e.getButton()
        );
    }
}
