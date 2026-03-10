package ui.editor;

import models.Furniture;
import models.Room;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class PropertiesPanel extends JPanel {

    private Room room;
    private Furniture selectedFurniture;
    private Runnable onUpdate; // Callback to trigger repaint in parent

    // UI Components - Furniture
    private JPanel furniturePanel;
    private JTextField widthField, depthField, heightField, rotationField;
    private JPanel colorPreview;
    private JButton colorBtn;

    // UI Components - Room
    private JPanel roomPanel;
    private JPanel wallColorPreview, floorColorPreview;

    private boolean isUpdating = false; // Prevent loop

    public PropertiesPanel(Runnable onUpdate) {
        this.onUpdate = onUpdate;
        setLayout(new BorderLayout());
        setBackground(new Color(14, 20, 45));
        setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(40, 58, 95)));
        setPreferredSize(new Dimension(240, 0));

        JLabel title = new JLabel("Properties");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new CardLayout());

        // 1. Room Properties (Default)
        roomPanel = createRoomPanel();
        roomPanel.setOpaque(false);

        // 2. Furniture Properties
        furniturePanel = createFurniturePanel();
        furniturePanel.setOpaque(false);

        content.add(roomPanel, "ROOM");
        content.add(furniturePanel, "FURNITURE");
        add(content, BorderLayout.CENTER);
    }

    private JPanel createRoomPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel noSel = new JLabel("No selection");
        noSel.setForeground(new Color(130, 150, 185));
        p.add(noSel, gbc);
        gbc.gridy++;
        p.add(new JSeparator(), gbc);
        gbc.gridy++;

        JLabel roomColors = new JLabel("Room Colors");
        roomColors.setForeground(new Color(220, 230, 245));
        p.add(roomColors, gbc);
        gbc.gridy++;

        // Wall Color
        JButton wallBtn = new JButton("Wall Color");
        wallColorPreview = new JPanel();
        wallColorPreview.setPreferredSize(new Dimension(20, 20));
        wallColorPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JPanel wallRow = new JPanel(new BorderLayout(5, 0));
        wallRow.setOpaque(false);
        wallRow.add(wallColorPreview, BorderLayout.WEST);
        wallRow.add(wallBtn, BorderLayout.CENTER);

        wallBtn.addActionListener(e -> {
            if (room == null)
                return;
            Color newColor = JColorChooser.showDialog(this, "Choose Wall Color", room.getWallColor());
            if (newColor != null) {
                room.setWallColor(newColor);
                wallColorPreview.setBackground(newColor);
                if (onUpdate != null)
                    onUpdate.run();
            }
        });

        p.add(wallRow, gbc);
        gbc.gridy++;

        // Floor Color
        JButton floorBtn = new JButton("Floor Color");
        floorColorPreview = new JPanel();
        floorColorPreview.setPreferredSize(new Dimension(20, 20));
        floorColorPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JPanel floorRow = new JPanel(new BorderLayout(5, 0));
        floorRow.setOpaque(false);
        floorRow.add(floorColorPreview, BorderLayout.WEST);
        floorRow.add(floorBtn, BorderLayout.CENTER);

        floorBtn.addActionListener(e -> {
            if (room == null)
                return;
            Color newColor = JColorChooser.showDialog(this, "Choose Floor Color", room.getFloorColor());
            if (newColor != null) {
                room.setFloorColor(newColor);
                floorColorPreview.setBackground(newColor);
                if (onUpdate != null)
                    onUpdate.run();
            }
        });

        p.add(floorRow, gbc);

        // Spacer
        gbc.gridy++;
        gbc.weighty = 1.0;
        p.add(new JPanel() {
            {
                setOpaque(false);
            }
        }, gbc);

        return p;
    }

    private JPanel createFurniturePanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(Color.WHITE);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        // Dimensions
        p.add(new JLabel("Dimensions (cm)"), gbc);
        gbc.gridy++;

        widthField = addLabeledField(p, "Width:", gbc);
        depthField = addLabeledField(p, "Depth:", gbc);
        heightField = addLabeledField(p, "Height:", gbc);

        p.add(new JSeparator(), gbc);
        gbc.gridy++;

        // Rotation
        rotationField = addLabeledField(p, "Rotation:", gbc);

        p.add(new JSeparator(), gbc);
        gbc.gridy++;

        // Color
        colorBtn = new JButton("Change Color");
        colorPreview = new JPanel();
        colorPreview.setPreferredSize(new Dimension(20, 20));
        colorPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JPanel colorRow = new JPanel(new BorderLayout(5, 0));
        colorRow.setOpaque(false);
        colorRow.add(colorPreview, BorderLayout.WEST);
        colorRow.add(colorBtn, BorderLayout.CENTER);

        p.add(colorRow, gbc);
        gbc.gridy++;

        colorBtn.addActionListener(e -> {
            if (selectedFurniture == null)
                return;
            Color newColor = JColorChooser.showDialog(this, "Choose Color", selectedFurniture.getColor());
            if (newColor != null) {
                selectedFurniture.setColor(newColor);
                updateFields(); // Refresh preview
                if (onUpdate != null)
                    onUpdate.run();
            }
        });

        // Spacer
        gbc.weighty = 1.0;
        p.add(new JPanel() {
            {
                setOpaque(false);
            }
        }, gbc);

        // Listeners for text fields
        addChangeListeners();

        return p;
    }

    private void updateFields() {
        isUpdating = true;
        if (selectedFurniture != null) {
            widthField.setText(String.valueOf(selectedFurniture.getWidth()));
            depthField.setText(String.valueOf(selectedFurniture.getDepth()));
            heightField.setText(String.valueOf(selectedFurniture.getHeight()));
            rotationField.setText(String.valueOf(selectedFurniture.getRotation()));
            colorPreview.setBackground(selectedFurniture.getColor());
        }
        isUpdating = false;
    }

    private JTextField addLabeledField(JPanel p, String label, GridBagConstraints gbc) {
        JPanel row = new JPanel(new BorderLayout(5, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(label);
        lbl.setForeground(new Color(160, 174, 192));
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        row.add(lbl, BorderLayout.WEST);
        JTextField field = new JTextField();
        field.setBackground(new Color(28, 40, 72));
        field.setForeground(Color.WHITE);
        field.setCaretColor(new Color(99, 179, 237));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(40, 58, 95)),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        row.add(field, BorderLayout.CENTER);
        p.add(row, gbc);
        gbc.gridy++;
        return field;
    }

    private void addChangeListeners() {
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                applyChanges();
            }

            public void removeUpdate(DocumentEvent e) {
                applyChanges();
            }

            public void changedUpdate(DocumentEvent e) {
                applyChanges();
            }
        };

        widthField.getDocument().addDocumentListener(dl);
        depthField.getDocument().addDocumentListener(dl);
        heightField.getDocument().addDocumentListener(dl);
        rotationField.getDocument().addDocumentListener(dl);
    }

    private void applyChanges() {
        if (isUpdating || selectedFurniture == null)
            return;

        try {
            double w = Double.parseDouble(widthField.getText());
            double d = Double.parseDouble(depthField.getText());
            double h = Double.parseDouble(heightField.getText());
            double r = Double.parseDouble(rotationField.getText());

            selectedFurniture.setWidth(w);
            selectedFurniture.setDepth(d);
            selectedFurniture.setHeight(h);
            selectedFurniture.setRotation(r);

            if (onUpdate != null)
                onUpdate.run();

        } catch (NumberFormatException e) {
            // Ignore invalid input while typing
        }
    }

    public void setSelection(Room room, Furniture f) {
        this.room = room;
        this.selectedFurniture = f;

        isUpdating = true;

        if (room != null) {
            wallColorPreview.setBackground(room.getWallColor());
            floorColorPreview.setBackground(room.getFloorColor());
        }

        if (f != null) {
            widthField.setText(String.valueOf(f.getWidth()));
            depthField.setText(String.valueOf(f.getDepth()));
            heightField.setText(String.valueOf(f.getHeight()));
            rotationField.setText(String.valueOf(f.getRotation()));
            colorPreview.setBackground(f.getColor());

            ((CardLayout) ((JPanel) getComponent(1)).getLayout()).show(((JPanel) getComponent(1)), "FURNITURE");
        } else {
            ((CardLayout) ((JPanel) getComponent(1)).getLayout()).show(((JPanel) getComponent(1)), "ROOM");
        }

        isUpdating = false;
    }

    public void refresh() {
        // Just re-set selection to refresh values (e.g. if changed by toolbars)
        setSelection(room, selectedFurniture);
    }
}
