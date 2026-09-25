package com.pigdroid.vox.editor;

import java.util.Stack;

public class UndoManager {
    private final Stack<VoxelModel> undoStack;
    private final Stack<VoxelModel> redoStack;

    public UndoManager() {
        this.undoStack = new Stack<>();
        this.redoStack = new Stack<>();
    }

    public void recordState(VoxelModel snapshot) {
        undoStack.push(snapshot);
        redoStack.clear();
    }

    public VoxelModel undo(VoxelModel current) {
        if (undoStack.isEmpty()) {
            return null;
        }
        redoStack.push(current);
        return undoStack.pop();
    }

    public VoxelModel redo(VoxelModel current) {
        if (redoStack.isEmpty()) {
            return null;
        }
        undoStack.push(current);
        return redoStack.pop();
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
