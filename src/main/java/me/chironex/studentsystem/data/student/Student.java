package me.chironex.studentsystem.data.student;

import lombok.Getter;
import org.jetbrains.annotations.Unmodifiable;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class representing a university student.
 * All students have basic information (id, name, birth year) and can have grades.
 * Each student type implements their own specific skill execution.
 * 
 * @author chmodxChironex
 * @since 1.0
 */
@Getter
public abstract class Student implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String firstName;
    private final String lastName;
    private final int birthYear;
    private final List<Integer> grades;

    public Student(int id, String firstName, String lastName, int birthYear) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthYear = birthYear;
        this.grades = new ArrayList<>();
    }

    /**
     * Executes the specific skill associated with this student type.
     * Each student type implements this method to demonstrate their field expertise.
     * 
     * @return a string representation of the executed skill
     */
    public abstract String executeSkill();

    /**
     * Adds a grade to the student's record.
     * Only accepts grades in the valid range (1-5).
     * 
     * @param grade the grade to add, must be between 1 and 5 inclusive
     */
    public void addGrade(int grade) {
        if (grade >= 1 && grade <= 5) {
            grades.add(grade);
        }
    }

    /**
     * Returns a copy of the student's grades list.
     * 
     * @return a new list containing all grades, modifications won't affect the original
     */
    @Unmodifiable
    public List<Integer> getGrades() {
        return List.copyOf(grades);
    }

    /**
     * Calculates the average grade for this student.
     * 
     * @return the average grade as a double, or 0.0 if no grades exist
     */
    public double getAverageGrade() {
        return grades
                .stream()
                .mapToInt(Integer::intValue)
                .average().orElse(0.0);
    }

    @Override
    public String toString() {
        return String.format("ID: %d, %s %s, birth year: %d, average grade: %.2f",
                id, firstName, lastName, birthYear, getAverageGrade());
    }
}