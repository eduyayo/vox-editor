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
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

public class EditorFrame extends JFrame {

    private JPanel centerPanel;
    private VoxelModel voxelModel;

    public EditorFrame() {
        this.voxelModel = new VoxelModel();
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

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));

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

        JMenuItem redoItem = new JMenuItem("Redo");
        redoItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK));

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

        setJMenuBar(menuBar);
    }

    private void initToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        toolBar.add(new JButton("Open"));
        toolBar.add(new JButton("Save"));
        toolBar.addSeparator();
        toolBar.add(new JButton("Undo"));
        toolBar.add(new JButton("Redo"));

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

        JPanel leftPanel = new JPanel();
        // Placeholder for tool details, buttons etc
        leftPanel.add(new JButton("Tool 1"));
        leftPanel.add(new JButton("Tool 2"));
        mainPanel.add(leftPanel, BorderLayout.WEST);

        centerPanel = new JPanel(new GridLayout(2, 2));

        // Top Left: Front
        ViewPanel frontPanel = new ViewPanel("Front", voxelModel);
        centerPanel.add(frontPanel);

        // Top Right: Left
        ViewPanel leftViewPanel = new ViewPanel("Left", voxelModel);
        centerPanel.add(leftViewPanel);

        // Bottom Left: Top
        ViewPanel topViewPanel = new ViewPanel("Top", voxelModel);
        centerPanel.add(topViewPanel);

        // Bottom Right: Preview
        PreviewPanel previewPanel = new PreviewPanel(voxelModel);
        previewPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
        centerPanel.add(previewPanel);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel, BorderLayout.CENTER);
    }

    // Package-private or protected so we can access it from tests if needed
    JPanel getCenterPanel() {
        return centerPanel;
    }
}
