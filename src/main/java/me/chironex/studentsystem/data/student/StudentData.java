package me.chironex.studentsystem.data.student;

/**
 * Record representing student data for creation and persistence.
 *
 * @param id the student ID
 * @param firstName the student's first name
 * @param lastName the student's last name
 * @param birthYear the student's birth year
 */
public record StudentData(int id, String firstName, String lastName, int birthYear) {
}