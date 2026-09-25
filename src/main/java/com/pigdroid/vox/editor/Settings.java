package com.pigdroid.vox.editor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Settings {
    private static Settings instance;
    private Color viewBackgroundColor = Color.DARK_GRAY;
    private List<SettingsListener> listeners = new ArrayList<>();

    private Settings() {}

    public static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }

    public Color getViewBackgroundColor() {
        return viewBackgroundColor;
    }

    public void setViewBackgroundColor(Color color) {
        if (!this.viewBackgroundColor.equals(color)) {
            this.viewBackgroundColor = color;
            fireSettingsChanged();
        }
    }

    public void addSettingsListener(SettingsListener listener) {
        listeners.add(listener);
    }

    public void removeSettingsListener(SettingsListener listener) {
        listeners.remove(listener);
    }

    private void fireSettingsChanged() {
        for (SettingsListener listener : listeners) {
            listener.onSettingsChanged(this);
        }
    }

    public interface SettingsListener {
        void onSettingsChanged(Settings settings);
    }
}
