package me.chironex.studentsystem;

import me.chironex.studentsystem.data.lang.LangSource;
import me.chironex.studentsystem.gui.StudentGUI;

import javax.swing.*;

/**
 * Main application class for the Student Administration System.
 * Provides entry point for creating and initializing the GUI application.
 * 
 * @author chmodxChironex
 * @since 1.0
 */
public final class StudentAdministrationApplication {

    private StudentAdministrationApplication() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(StudentAdministrationApplication::createGUI);
    }

    /**
     * Creates and displays the main GUI window for the student administration system.
     * Should be called from the Event Dispatch Thread for thread safety.
     */
    private static void createGUI() {
        LangSource langSource = new LangSource();
        new StudentGUI(langSource);
    }
}