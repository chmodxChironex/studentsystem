package me.chironex.studentsystem.gui;

import me.chironex.studentsystem.data.JdbcReconnectStrategy;
import me.chironex.studentsystem.data.ReconnectStrategy;
import me.chironex.studentsystem.data.SimplePersistenceExecutor;
import me.chironex.studentsystem.data.lang.LangEntry;
import me.chironex.studentsystem.data.lang.LangSource;
import me.chironex.studentsystem.data.student.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.io.*;
import java.util.List;

/**
 * Main GUI window for the Student Administration System.
 * Handles user interaction, table display, and database operations.
 */

public class StudentGUI extends JFrame {
    private static final String[] TABLE_COLUMNS = {
            "ID", "First Name", "Last Name", "Birth Year", "Type", "Average", "Skill"};
    private static final String JDBC_URL = "jdbc:sqlite:students.db";

    private final StudentRepository database;

    private final DefaultTableModel tableModel;
    private final JTable table;
    private final JLabel statusLabel;

    private final SimplePersistenceExecutor persistenceExecutor;

    private boolean changesMade = false;

    /**
     * Constructs the main GUI window for the student administration system.
     *
     * @param langSource the language source for translations
     */

    public StudentGUI(LangSource langSource) {
        this.persistenceExecutor = initPersistenceExecutor();
        this.database = new StudentRepository(persistenceExecutor, new StudentFactoryImpl());
        
        setTitle(langSource.getTranslation(LangEntry.GUI_TITLE));

        initAttributes();

        // ---
        createMenuBar();

        this.tableModel = createTableModel();
        this.table = createTable(tableModel);
        this.statusLabel = createStatusBar();
        // ---

        showWindow();

        loadTableData();
        updateStatus("Application started", Color.GREEN);
    }

    private SimplePersistenceExecutor initPersistenceExecutor() {
        ReconnectStrategy reconnectStrategy = new JdbcReconnectStrategy(JDBC_URL);

        SimplePersistenceExecutor persistenceExecutor = new SimplePersistenceExecutor(reconnectStrategy);
        persistenceExecutor.connect();

        return persistenceExecutor;
    }

    private void initAttributes() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }

    private void showWindow() {
        setVisible(true);
    }

    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        
        JMenu fileMenu = new JMenu("File");
        JMenuItem importItem = new JMenuItem("Import from TXT");
        JMenuItem exportItem = new JMenuItem("Export to TXT");
        JMenuItem exitItem = new JMenuItem("Exit");
        
        importItem.addActionListener(e -> importFromTxt());
        exportItem.addActionListener(e -> exportSelectedToTxt());
        exitItem.addActionListener(e -> {
            if (confirmExit()) {
                System.exit(0);
            }
        });
        
        fileMenu.add(importItem);
        fileMenu.add(exportItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    private DefaultTableModel createTableModel() {
        setLayout(new BorderLayout());

        return new DefaultTableModel(TABLE_COLUMNS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private JTable createTable(TableModel model) {
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(25);
        
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        createButtonPanels();

        return table;
    }

    private void createButtonPanels() {
        JPanel buttonPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        String[] group1Texts = {"Add Student", "Add Grade", "Delete Student"};
        Runnable[] group1Actions = {this::addStudent, this::addGrade, this::deleteStudent};
        buttonPanel.add(createButtonGroup("Student Management", group1Texts, group1Actions));
        
        String[] group2Texts = {"Show Skill", "Find Student", "Sort by Last Name"};
        Runnable[] group2Actions = {this::showSkill, this::showStudentById, this::sortByLastName};
        buttonPanel.add(createButtonGroup("Display", group2Texts, group2Actions));
        
        String[] group3Texts = {"Averages by Type", "Student Counts"};
        Runnable[] group3Actions = {this::showAverages, this::showCounts};
        buttonPanel.add(createButtonGroup("Statistics", group3Texts, group3Actions));
        
        String[] group4Texts = {"Save to DB", "Load from DB"};
        Runnable[] group4Actions = {this::saveDatabase, this::reloadFromDatabase};
        buttonPanel.add(createButtonGroup("Database", group4Texts, group4Actions));
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JPanel createButtonGroup(String title, String[] buttonTexts, Runnable[] actions) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        
        JPanel buttonsPanel = new JPanel(new GridLayout(buttonTexts.length, 1, 5, 5));
        
        for (int i = 0; i < buttonTexts.length; i++) {
            JButton button = new JButton(buttonTexts[i]);
            final int index = i;
            button.addActionListener(e -> actions[index].run());
            buttonsPanel.add(button);
        }
        
        panel.add(buttonsPanel, BorderLayout.CENTER);
        return panel;
    }

    private JLabel createStatusBar() {
        JLabel statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());

        add(statusLabel, BorderLayout.NORTH);

        return statusLabel;
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void markChangesMade() {
        changesMade = true;
    }

    private void resetChangesMade() {
        changesMade = false;
    }

    private boolean confirmExit() {
        if (changesMade) {
            int result = JOptionPane.showConfirmDialog(
                this,
                "You have unsaved changes. Do you want to exit?",
                "Confirm Exit",
                JOptionPane.YES_NO_OPTION
            );
            return result == JOptionPane.YES_OPTION;
        }
        return true;
    }

    private void loadTableData() {
        tableModel.setRowCount(0);
        
        for (Student student : database.getAllStudents()) {
            addStudentToTable(student);
        }
    }

    private void addStudentToTable(Student student) {
        String type = student instanceof TelecommunicationsStudent ? "Telecommunications" : "Cybersecurity";
        String skill = student.executeSkill();
        
        Object[] row = {
            student.getId(),
            student.getFirstName(),
            student.getLastName(),
            student.getBirthYear(),
            type,
            String.format("%.2f", student.getAverageGrade()),
            skill.length() > 50 ? skill.substring(0, 50) + "..." : skill
        };
        tableModel.addRow(row);
    }

    private void addStudent() {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField birthYearField = new JTextField();
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Telecommunications", "Cybersecurity"});
        
        panel.add(new JLabel("First Name:"));
        panel.add(firstNameField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lastNameField);
        panel.add(new JLabel("Birth Year:"));
        panel.add(birthYearField);
        panel.add(new JLabel("Study Type:"));
        panel.add(typeCombo);
        
        int result = JOptionPane.showConfirmDialog(this, panel, "Add Student", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
                String firstName = firstNameField.getText().trim();
                String lastName = lastNameField.getText().trim();
                int birthYear = Integer.parseInt(birthYearField.getText().trim());
                
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    showMessage("First name and last name cannot be empty!");
                    return;
                }
                
                int id;
                if (typeCombo.getSelectedIndex() == 0) {
                    id = database.addTelecommunicationsStudent(firstName, lastName, birthYear);
                } else {
                    id = database.addCybersecurityStudent(firstName, lastName, birthYear);
                }
                
                loadTableData();
                markChangesMade();
                updateStatus("Student added with ID: " + id, Color.GREEN);
                
            } catch (NumberFormatException e) {
                showMessage("Birth year must be a number!");
            }
        }
    }

    private void addGrade() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showMessage("Please select a student in the table first!");
            return;
        }
        
        int studentId = (Integer) tableModel.getValueAt(selectedRow, 0);
        
        String gradeStr = JOptionPane.showInputDialog(this, "Enter grade (1-5):", "Add Grade", JOptionPane.QUESTION_MESSAGE);
        
        if (gradeStr != null) {
            try {
                int grade = Integer.parseInt(gradeStr.trim());
                
                if (grade < 1 || grade > 5) {
                    showMessage("Grade must be between 1-5!");
                    return;
                }
                
                if (database.addGradeToStudent(studentId, grade)) {
                    loadTableData();
                    markChangesMade();
                    updateStatus("Grade added", Color.GREEN);
                } else {
                    showMessage("Error adding grade!");
                }
                
            } catch (NumberFormatException e) {
                showMessage("Grade must be a number!");
            }
        }
    }

    private void deleteStudent() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showMessage("Please select a student in the table first!");
            return;
        }
        
        int studentId = (Integer) tableModel.getValueAt(selectedRow, 0);
        String studentName = tableModel.getValueAt(selectedRow, 1) + " " + tableModel.getValueAt(selectedRow, 2);
        
        int result = JOptionPane.showConfirmDialog(
            this,
            "Do you really want to delete student " + studentName + "?",
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION
        );
        
        if (result == JOptionPane.YES_OPTION) {
            if (database.removeStudent(studentId)) {
                loadTableData();
                markChangesMade();
                updateStatus("Student deleted", Color.GREEN);
            } else {
                showMessage("Error deleting student!");
            }
        }
    }

    private void showSkill() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showMessage("Please select a student in the table first!");
            return;
        }
        
        int studentId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Student student = database.findStudentById(studentId);
        
        if (student != null) {
            String skill = student.executeSkill();
            String title = student instanceof TelecommunicationsStudent ? "Morse Code" : "SHA-256 Hash";
            
            JTextArea textArea = new JTextArea(skill);
            textArea.setEditable(false);
            textArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
            
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 200));
            
            JOptionPane.showMessageDialog(this, scrollPane, title, JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showStudentById() {
        String idStr = JOptionPane.showInputDialog(this, "Enter student ID:", "Find Student", JOptionPane.QUESTION_MESSAGE);
        
        if (idStr != null) {
            try {
                int id = Integer.parseInt(idStr.trim());
                Student student = database.findStudentById(id);
                
                if (student != null) {
                    String info = String.format("""
                            ID: %d
                            Name: %s %s
                            Birth Year: %d
                            Type: %s
                            Grades: %s
                            Average: %.2f""",
                        student.getId(),
                        student.getFirstName(),
                        student.getLastName(),
                        student.getBirthYear(),
                        student instanceof TelecommunicationsStudent ? "Telecommunications" : "Cybersecurity",
                        student.getGrades().toString(),
                        student.getAverageGrade()
                    );
                    
                    JOptionPane.showMessageDialog(this, info, "Student Information", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    showMessage("Student with ID " + id + " not found!");
                }
                
            } catch (NumberFormatException e) {
                showMessage("ID must be a number!");
            }
        }
    }

    private void sortByLastName() {
        List<Student> sortedStudents = database.getSortedStudentsByLastName();
        
        tableModel.setRowCount(0);
        for (Student student : sortedStudents) {
            addStudentToTable(student);
        }
        
        updateStatus("Students sorted by last name", Color.BLUE);
    }

    private void showAverages() {
        double telecomAvg = database.getAverageGradeByType(TelecommunicationsStudent.class);
        double cyberAvg = database.getAverageGradeByType(CybersecurityStudent.class);
        
        String message = String.format("""
                Average grades by type:
                
                Telecommunications: %.2f
                Cybersecurity: %.2f""",
            telecomAvg, cyberAvg
        );
        
        JOptionPane.showMessageDialog(this, message, "Averages by Type", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showCounts() {
        int telecomCount = database.getStudentsByType(TelecommunicationsStudent.class).size();
        int cyberCount = database.getStudentsByType(CybersecurityStudent.class).size();
        int totalCount = database.getAllStudents().size();
        
        String message = String.format("""
                Student counts:
                
                Telecommunications: %d
                Cybersecurity: %d
                Total: %d""",
            telecomCount, cyberCount, totalCount
        );
        
        JOptionPane.showMessageDialog(this, message, "Student Counts", JOptionPane.INFORMATION_MESSAGE);
    }

    private void saveDatabase() {
        database.saveToDatabase();
        resetChangesMade();

        updateStatus("Data saved to database", Color.GREEN);
    }

    private void reloadFromDatabase() {
        database.loadFromDatabase();
        loadTableData();
        resetChangesMade();

        updateStatus("Data loaded from database", Color.GREEN);
    }

    private void importFromTxt() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text files", "txt"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            importFromTxtFile(fileChooser.getSelectedFile().getAbsolutePath());
        }
    }

    /**
     * Imports student data from a specified text file.
     *
     * @param fileName the path to the text file to import from
     */

    private void importFromTxtFile(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            int importedCount = 0;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                
                String[] parts = line.split(";");
                if (parts.length >= 4) {
                    try {
                        String firstName = parts[0].trim();
                        String lastName = parts[1].trim();
                        int birthYear = Integer.parseInt(parts[2].trim());
                        String type = parts[3].trim().toLowerCase();
                        
                        if (type.contains("telekom") || type.contains("telecom")) {
                            database.addTelecommunicationsStudent(firstName, lastName, birthYear);
                        } else {
                            database.addCybersecurityStudent(firstName, lastName, birthYear);
                        }
                        importedCount++;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid line format: " + line);
                    }
                }
            }
            
            loadTableData();
            markChangesMade();
            updateStatus("Imported " + importedCount + " students", Color.GREEN);
            
        } catch (IOException e) {
            showMessage("Error importing from file: " + e.getMessage());
        }
    }

    private void exportSelectedToTxt() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            showMessage("Please select a student in the table first!");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text files", "txt"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (FileWriter writer = new FileWriter(fileChooser.getSelectedFile())) {
                int studentId = (Integer) tableModel.getValueAt(selectedRow, 0);
                Student student = database.findStudentById(studentId);
                
                if (student != null) {
                    writeStudentToFile(writer, student);
                    updateStatus("Student exported to file", Color.GREEN);
                }
                
            } catch (IOException e) {
                showMessage("Error exporting to file: " + e.getMessage());
            }
        }
    }

    /**
     * Writes a student's data to a file.
     *
     * @param writer the FileWriter to write to
     * @param s the student whose data will be written
     * @throws IOException if an I/O error occurs
     */

    private void writeStudentToFile(FileWriter writer, Student s) throws IOException {
        String type = s instanceof TelecommunicationsStudent ? "Telecommunications" : "Cybersecurity";
        
        writer.write(String.format("ID: %d%n", s.getId()));
        writer.write(String.format("Name: %s %s%n", s.getFirstName(), s.getLastName()));
        writer.write(String.format("Birth Year: %d%n", s.getBirthYear()));
        writer.write(String.format("Study Type: %s%n", type));
        writer.write(String.format("Grades: %s%n", s.getGrades().toString()));
        writer.write(String.format("Average: %.2f%n", s.getAverageGrade()));
        writer.write(String.format("Skill: %s%n", s.executeSkill()));
        writer.write("-----------------------------------\n");
    }

    private void showMessage(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    protected void processWindowEvent(java.awt.event.WindowEvent e) {
        if (e.getID() == java.awt.event.WindowEvent.WINDOW_CLOSING) {
            if (confirmExit()) {
                persistenceExecutor.close();
                System.exit(0);
            }
        } else {
            super.processWindowEvent(e);
        }
    }
}