package me.chironex.studentsystem.data.student;

/**
 * Factory interface for creating different types of students.
 * Provides a unified way to instantiate student objects based on type specification.
 * 
 * @author chmodxChironex
 * @since 1.0
 */
public interface StudentFactory {

    /**
     * Creates a student instance of the specified type with given data.
     * 
     * @param type the type of student to create (e.g., "TELEKOM", "CYBER")
     * @param data the student data containing id, names, and birth year
     * @return a new Student instance of the appropriate type
     * @throws IllegalArgumentException if type is not recognized
     */
    Student createStudent(StudentType type, StudentData data);
}