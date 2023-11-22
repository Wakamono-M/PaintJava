import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class JavaPaintApp {
    private JFrame frame;
    private JPanel drawingPanel;
    private Color currentColor = Color.BLACK;
    private String currentTool = "pen"; // Default tool is pen
    private int currentBrushSize = 3; // Default brush size
    private ArrayList<Point> currentPath = new ArrayList<>();
    private BufferedImage bufferImage;
    private boolean isSpraying = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JavaPaintApp().createAndShowGUI());
    }

    private void createAndShowGUI() {
        frame = new JFrame("Java Paint App");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        drawingPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bufferImage, 0, 0, this);
            }
        };
        drawingPanel.setBackground(Color.WHITE);
        bufferImage = new BufferedImage(Toolkit.getDefaultToolkit().getScreenSize().width, Toolkit.getDefaultToolkit().getScreenSize().height, BufferedImage.TYPE_INT_ARGB);

        drawingPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                currentPath = new ArrayList<>();
                currentPath.add(e.getPoint());
                if ("pen".equals(currentTool) || "eraser".equals(currentTool)) {
                    drawPath(currentPath);
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // Continue using the selected tool
            }
        });

        drawingPanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                currentPath.add(e.getPoint());
                if ("pen".equals(currentTool)) {
                    drawPathSegment(currentPath);
                } else if ("eraser".equals(currentTool)) {
                    erasePathSegment(currentPath);
                } else if ("spray".equals(currentTool)) {
                    sprayPaint(e.getPoint());
                }
            }
        });

        JButton colorButton = new JButton("Choose Color");
        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color selectedColor = JColorChooser.showDialog(null, "Choose Color", currentColor);
                if (selectedColor != null) {
                    currentColor = selectedColor;
                }
            }
        });

        JButton saveButton = new JButton("Save Image");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                int userSelection = fileChooser.showSaveDialog(null);
                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    File fileToSave = fileChooser.getSelectedFile();
                    try {
                        ImageIO.write(bufferImage, "png", fileToSave);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        JButton clearButton = new JButton("Clear All");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearBufferImage();
            }
        });

        JComboBox<String> toolComboBox = new JComboBox<>(new String[]{"Pen", "Eraser", "Spray"});
        toolComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedTool = toolComboBox.getSelectedItem().toString().toLowerCase();
                currentTool = selectedTool;
                isSpraying = "spray".equals(currentTool);
            }
        });

        JSlider brushSizeSlider = new JSlider(1, 20, currentBrushSize);
        brushSizeSlider.setMajorTickSpacing(5);
        brushSizeSlider.setMinorTickSpacing(1);
        brushSizeSlider.setPaintTicks(true);
        brushSizeSlider.setPaintLabels(true);
        brushSizeSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                currentBrushSize = brushSizeSlider.getValue();
            }
        });

        JPanel controlPanel = new JPanel();
        controlPanel.add(colorButton);
        controlPanel.add(toolComboBox);
        controlPanel.add(new JLabel("Brush Size:"));
        controlPanel.add(brushSizeSlider);
        controlPanel.add(saveButton);
        controlPanel.add(clearButton);

        frame.add(drawingPanel, BorderLayout.CENTER);
        frame.add(controlPanel, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    private void drawPath(List<Point> path) {
        Graphics2D g = bufferImage.createGraphics();
        g.setColor(currentColor);
        g.setStroke(new BasicStroke(currentBrushSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i + 1);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
        g.dispose();
        drawingPanel.repaint();
    }

    private void drawPathSegment(List<Point> path) {
        drawPath(path);
    }

    private void erasePathSegment(List<Point> path) {
        Graphics2D g = bufferImage.createGraphics();
        g.setColor(Color.WHITE); // Set the color to white for erasing
        g.setStroke(new BasicStroke(currentBrushSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        for (int i = 0; i < path.size() - 1; i++) {
            Point p1 = path.get(i);
            Point p2 = path.get(i + 1);
            g.drawLine(p1.x, p1.y, p2.x, p2.y);
        }
        g.dispose();
        drawingPanel.repaint();
    }

    private void sprayPaint(Point point) {
        if (!isSpraying) return;
        int sprayRadius = currentBrushSize;
        int numSprayParticles = 100;
        Graphics2D g = bufferImage.createGraphics();
        g.setColor(currentColor);
        Random random = new Random();
        for (int i = 0; i < numSprayParticles; i++) {
            int offsetX = random.nextInt(sprayRadius * 2) - sprayRadius;
            int offsetY = random.nextInt(sprayRadius * 2) - sprayRadius;
            int x = point.x + offsetX;
            int y = point.y + offsetY;
            g.drawLine(x, y, x, y);
        }
        g.dispose();
        drawingPanel.repaint();
    }

    private void clearBufferImage() {
        Graphics2D g = bufferImage.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, bufferImage.getWidth(), bufferImage.getHeight());
        g.dispose();
        drawingPanel.repaint();
    }
}
