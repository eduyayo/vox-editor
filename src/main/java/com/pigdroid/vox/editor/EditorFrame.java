package com.pigdroid.vox.editor;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JToggleButton;
import javax.swing.ButtonGroup;

import com.pigdroid.vox.editor.io.VoxFile;

public class EditorFrame extends JFrame {

    private JPanel centerPanel;
    private VoxelModel voxelModel;
    private ToolManager toolManager;
    private UndoManager undoManager;

    public EditorFrame() {
        this.voxelModel = new VoxelModel();
        this.toolManager = new ToolManager();
        this.undoManager = new UndoManager();
        this.toolManager.addTool(new ShiftKeyDecorator(new BrushTool()));
        this.toolManager.addTool(new ShiftKeyDecorator(new SquareTool()));
        this.toolManager.addTool(new ShiftKeyDecorator(new CircleTool()));
        this.toolManager.addTool(new ShiftKeyDecorator(new SelectTool()));

        setTitle("Swing Editor");
        setSize(800, 600);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initMenu();
        initToolBar();
        initMainPanels();
    }

    private void initMenu() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openItem = new JMenuItem("Open");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> openFile());

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> saveFile());

        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_DOWN_MASK));
        exitItem.addActionListener((ActionEvent e) -> System.exit(0));

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        // Edit Menu
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);

        JMenuItem undoItem = new JMenuItem("Undo");
        undoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK));
        undoItem.addActionListener(e -> performUndo());

        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));
        redoItem.addActionListener(e -> performRedo());

        JMenuItem cutItem = new JMenuItem("Cut");
        cutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_DOWN_MASK));

        JMenuItem copyItem = new JMenuItem("Copy");
        copyItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK));

        JMenuItem pasteItem = new JMenuItem("Paste");
        pasteItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK));

        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(cutItem);
        editMenu.add(copyItem);
        editMenu.add(pasteItem);

        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        // Window Menu
        JMenu windowMenu = new JMenu("Window");
        windowMenu.setMnemonic(KeyEvent.VK_W);

        JMenuItem settingsItem = new JMenuItem("Settings...");
        settingsItem.addActionListener(e -> {
            SettingsDialog dialog = new SettingsDialog(this);
            dialog.setVisible(true);
        });

        windowMenu.add(settingsItem);

        menuBar.add(windowMenu);

        setJMenuBar(menuBar);
    }

    private void initToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton openBtn = new JButton("Open");
        openBtn.addActionListener(e -> openFile());
        toolBar.add(openBtn);

        JButton saveBtn = new JButton("Save");
        saveBtn.addActionListener(e -> saveFile());
        toolBar.add(saveBtn);
        toolBar.addSeparator();
        JButton undoBtn = new JButton("Undo");
        undoBtn.addActionListener(e -> performUndo());
        toolBar.add(undoBtn);

        JButton redoBtn = new JButton("Redo");
        redoBtn.addActionListener(e -> performRedo());
        toolBar.add(redoBtn);

        toolBar.addSeparator();
        toolBar.add(new JLabel("Reference: "));
        JComboBox<String> referenceSelector = new JComboBox<>(new String[]{"European", "American"});
        referenceSelector.setSelectedItem("European");
        referenceSelector.addActionListener(e -> {
            String selected = (String) referenceSelector.getSelectedItem();
            if ("American".equals(selected)) {
                voxelModel.setReferenceSystem(ReferenceSystem.AMERICAN);
            } else {
                voxelModel.setReferenceSystem(ReferenceSystem.EUROPEAN);
            }
            repaint();
        });
        toolBar.add(referenceSelector);

        add(toolBar, BorderLayout.NORTH);
    }

    private void initMainPanels() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel leftPanel = new JPanel(new BorderLayout());
        JPanel toolsPanel = new JPanel(new GridLayout(0, 1));
        ButtonGroup toolGroup = new ButtonGroup();

        for (Tool tool : toolManager.getTools()) {
            JToggleButton toggleButton = new JToggleButton(tool.getName());
            toggleButton.addActionListener(e -> toolManager.setActiveTool(tool.getName()));
            toolGroup.add(toggleButton);
            toolsPanel.add(toggleButton);
            if (toolManager.getActiveTool() == tool) {
                toggleButton.setSelected(true);
            }
        }

        leftPanel.add(toolsPanel, BorderLayout.NORTH);

        JPanel optionsContainer = new JPanel(new BorderLayout());
        leftPanel.add(optionsContainer, BorderLayout.CENTER);

        toolManager.addChangeListener(() -> {
            optionsContainer.removeAll();
            Tool active = toolManager.getActiveTool();
            if (active != null && active.getOptionsPanel() != null) {
                optionsContainer.add(active.getOptionsPanel(), BorderLayout.NORTH);
            }
            optionsContainer.revalidate();
            optionsContainer.repaint();
        });

        // Trigger initial setup
        toolManager.setActiveTool(toolManager.getActiveTool() != null ? toolManager.getActiveTool().getName() : "");

        mainPanel.add(leftPanel, BorderLayout.WEST);

        centerPanel = new JPanel(new GridLayout(2, 2));

        // Top Left: Front
        ViewPanel frontPanel = new ViewPanel("Front", voxelModel, toolManager, undoManager);
        centerPanel.add(frontPanel);

        // Top Right: Left
        ViewPanel leftViewPanel = new ViewPanel("Left", voxelModel, toolManager, undoManager);
        centerPanel.add(leftViewPanel);

        // Bottom Left: Top
        ViewPanel topViewPanel = new ViewPanel("Top", voxelModel, toolManager, undoManager);
        centerPanel.add(topViewPanel);

        // Bottom Right: Preview
        PreviewPanel previewPanel = new PreviewPanel(voxelModel);
        previewPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
        centerPanel.add(previewPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        int ret = chooser.showOpenDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                VoxelModel newModel = VoxFile.read(file);
                // Clear existing and copy
                new ArrayList<>(this.voxelModel.getVoxels().keySet()).forEach(v -> this.voxelModel.removeVoxel(v.x(), v.y(), v.z()));
                newModel.getVoxels().forEach((v, c) -> this.voxelModel.setVoxel(v.x(), v.y(), v.z(), c));

                // Keep the current reference system, and clear projections
                new ArrayList<>(this.voxelModel.getProjections()).forEach(p -> this.voxelModel.deleteProjection(p.viewName(), p.u(), p.v()));

                repaint();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to open file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void saveFile() {
        JFileChooser chooser = new JFileChooser();
        int ret = chooser.showSaveDialog(this);
        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".vox")) {
                file = new File(file.getParentFile(), file.getName() + ".vox");
            }
            try {
                VoxFile.write(this.voxelModel, file);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Failed to save file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void performUndo() {
        VoxelModel prevState = undoManager.undo(voxelModel.clone());
        if (prevState != null) {
            voxelModel.copyFrom(prevState);
            repaint();
        }
    }

    private void performRedo() {
        VoxelModel nextState = undoManager.redo(voxelModel.clone());
        if (nextState != null) {
            voxelModel.copyFrom(nextState);
            repaint();
        }
    }

    // Package-private or protected so we can access it from tests if needed
    JPanel getCenterPanel() {
        return centerPanel;
    }
}
