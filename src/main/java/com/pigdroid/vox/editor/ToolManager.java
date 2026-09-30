package com.pigdroid.vox.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ToolManager {
    private final Map<String, Tool> tools = new HashMap<>();
    private Tool activeTool;
    private final List<Runnable> listeners = new ArrayList<>();

    public void addTool(Tool tool) {
        tools.put(tool.getName(), tool);
        if (activeTool == null) {
            setActiveTool(tool.getName());
        }
    }

    public void setActiveTool(String name) {
        if (tools.containsKey(name)) {
            activeTool = tools.get(name);
            notifyListeners();
        }
    }

    public Tool getActiveTool() {
        return activeTool;
    }

    private boolean eyedropperMode = false;
    private java.util.function.Consumer<Integer> eyedropperCallback;

    public void startEyedropper(java.util.function.Consumer<Integer> callback) {
        this.eyedropperMode = true;
        this.eyedropperCallback = callback;
        notifyListeners();
    }

    public boolean isEyedropperMode() {
        return eyedropperMode;
    }

    public void onColorPicked(int rgb) {
        if (eyedropperMode && eyedropperCallback != null) {
            eyedropperCallback.accept(rgb);
            this.eyedropperMode = false;
            this.eyedropperCallback = null;
            notifyListeners();
        }
    }

    public void cancelEyedropper() {
        this.eyedropperMode = false;
        this.eyedropperCallback = null;
        notifyListeners();
    }

    public void addChangeListener(Runnable listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (Runnable listener : listeners) {
            listener.run();
        }
    }

    public List<Tool> getTools() {
        return new ArrayList<>(tools.values());
    }
}
