package com.pigdroid.vox.editor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Settings {
    private static Settings instance;
    private Color viewBackgroundColor = Color.DARK_GRAY;
    private Color axisColor = new Color(192, 192, 192); // Silver gray
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

    public Color getAxisColor() {
        return axisColor;
    }

    public void setAxisColor(Color color) {
        if (!this.axisColor.equals(color)) {
            this.axisColor = color;
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
