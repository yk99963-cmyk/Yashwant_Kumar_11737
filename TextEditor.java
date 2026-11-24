import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.io.*;
import java.util.*;

public class TextEditor extends JFrame {

    JTextArea textArea;
    Stack<String> undoStack = new Stack<>();
    Stack<String> redoStack = new Stack<>();

    LinkedListText linkedList = new LinkedListText();
    HashMap<String, Integer> wordMap = new HashMap<>();

    String lastText = "";
    boolean suppress = false;

    public TextEditor() {

        setTitle("Text Editor with Undo/Redo");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        textArea = new JTextArea();
        textArea.setFont(new Font("Arial", Font.PLAIN, 18));

        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel panel = new JPanel();

        JButton undoButton = new JButton("Undo");
        JButton redoButton = new JButton("Redo");
        JButton searchButton = new JButton("Search");
        JButton saveButton = new JButton("Save");
        JButton openButton = new JButton("Open");
        JButton countButton = new JButton("Word Frequency");

        panel.add(undoButton);
        panel.add(redoButton);
        panel.add(searchButton);
        panel.add(saveButton);
        panel.add(openButton);
        panel.add(countButton);

        add(panel, BorderLayout.NORTH);

        // Track text changes
        textArea.getDocument().addDocumentListener(new DocumentListener() {

            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            public void changedUpdate(DocumentEvent e) {
                changed();
            }

            private void changed() {

                if (suppress)
                    return;

                String current = textArea.getText();

                undoStack.push(lastText);
                redoStack.clear();

                lastText = current;

                linkedList.build(current);
                updateHashMap(current);
            }
        });

        // Undo
        undoButton.addActionListener(e -> undo());

        // Redo
        redoButton.addActionListener(e -> redo());

        // Search
        searchButton.addActionListener(e -> search());

        // Save
        saveButton.addActionListener(e -> saveFile());

        // Open
        openButton.addActionListener(e -> openFile());

        // Word Frequency
        countButton.addActionListener(e -> showFrequency());

        setVisible(true);
    }

    // Undo operation
    void undo() {

        if (undoStack.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nothing to undo!");
            return;
        }

        redoStack.push(textArea.getText());

        String previous = undoStack.pop();

        restoreText(previous);
    }

    // Redo operation
    void redo() {

        if (redoStack.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nothing to redo!");
            return;
        }

        undoStack.push(textArea.getText());

        String next = redoStack.pop();

        restoreText(next);
    }

    // Restore text without triggering history
    void restoreText(String text) {

        suppress = true;

        textArea.setText(text);
        lastText = text;

        linkedList.build(text);
        updateHashMap(text);

        suppress = false;
    }

    // Search text
    void search() {

        String word = JOptionPane.showInputDialog(this, "Enter word to search:");

        if (word == null || word.isEmpty())
            return;

        String text = textArea.getText();

        int index = text.toLowerCase().indexOf(word.toLowerCase());

        if (index == -1) {
            JOptionPane.showMessageDialog(this, "Word not found!");
        } else {
            textArea.requestFocus();
            textArea.select(index, index + word.length());

            JOptionPane.showMessageDialog(
                this, "Word found at index: " + index
            );
        }
    }

    // Hashing using HashMap
    void updateHashMap(String text) {

        wordMap.clear();

        String[] words = text.toLowerCase().split("\\W+");

        for (String word : words) {

            if (!word.isEmpty()) {
                wordMap.put(word, wordMap.getOrDefault(word, 0) + 1);
            }
        }
    }

    // Show word frequency
    void showFrequency() {

        if (wordMap.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No words in the editor!");
            return;
        }

        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, Integer> entry : wordMap.entrySet()) {
            result.append(entry.getKey())
                  .append(" : ")
                  .append(entry.getValue())
                  .append("\n");
        }

        JOptionPane.showMessageDialog(
            this, result.toString(), "Word Frequency", JOptionPane.INFORMATION_MESSAGE
        );
    }

    // Save file
    void saveFile() {

        JFileChooser chooser = new JFileChooser();

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

            try (FileWriter writer = new FileWriter(chooser.getSelectedFile())) {
                writer.write(textArea.getText());

                JOptionPane.showMessageDialog(this, "File saved successfully!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving file!");
            }
        }
    }

    // Open file
    void openFile() {

        JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            try (FileReader reader = new FileReader(chooser.getSelectedFile())) {

                StringBuilder content = new StringBuilder();

                int ch;

                while ((ch = reader.read()) != -1) {
                    content.append((char) ch);
                }

                restoreText(content.toString());

                // Treat file opening as a new starting state
                undoStack.clear();
                redoStack.clear();

                JOptionPane.showMessageDialog(this, "File opened successfully!");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error opening file!");
            }
        }
    }

    // Custom Linked List
    static class LinkedListText {

        Node head;

        static class Node {
            char data;
            Node next;

            Node(char data) {
                this.data = data;
            }
        }

        // Build linked list from text
        void build(String text) {

            head = null;
            Node tail = null;

            for (char c : text.toCharArray()) {

                Node newNode = new Node(c);

                if (head == null) {
                    head = newNode;
                    tail = newNode;
                } else {
                    tail.next = newNode;
                    tail = newNode;
                }
            }
        }

        // Convert linked list to string
        String getText() {

            StringBuilder sb = new StringBuilder();

            Node current = head;

            while (current != null) {
                sb.append(current.data);
                current = current.next;
            }

            return sb.toString();
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> new TextEditor());
    }
}