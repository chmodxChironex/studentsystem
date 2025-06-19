package me.chironex.studentsystem.data.student;

import lombok.SneakyThrows;
import me.chironex.studentsystem.data.PersistenceExecutor;

import java.sql.*;
import java.util.*;

/**
 * Database management class for student records.
 * Handles in-memory storage and SQLite database persistence of student data.
 * Supports CRUD operations and various query methods.
 * 
 * @author chmodxChironex
 * @since 1.0
 */
@SuppressWarnings("SqlNoDataSourceInspection")
public class StudentRepository {
    private static final String SELECT_ALL_STUDENTS = "SELECT * FROM students";
    private static final String SELECT_GRADES_BY_STUDENT = "SELECT grade FROM grades WHERE student_id = ?";
    private static final String INSERT_STUDENT = "INSERT INTO students (id, first_name, last_name, birth_year, student_type) VALUES (?, ?, ?, ?, ?)";
    private static final String INSERT_GRADE = "INSERT INTO grades (student_id, grade) VALUES (?, ?)";
    private static final String DELETE_GRADES = "DELETE FROM grades";
    private static final String DELETE_STUDENTS = "DELETE FROM students";

    private static final String CREATE_STUDENTS_TABLE = """
            CREATE TABLE IF NOT EXISTS students (
            id INTEGER PRIMARY KEY,
            first_name TEXT,
            last_name TEXT,
            birth_year INTEGER,
            student_type TEXT)""";

    private static final String CREATE_GRADES_TABLE = """
            CREATE TABLE IF NOT EXISTS grades (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            student_id INTEGER,
            grade INTEGER,
            FOREIGN KEY (student_id) REFERENCES students(id))""";

    private final PersistenceExecutor persistenceExecutor;
    private final StudentFactory studentFactory;

    private final List<Student> students;

    private int nextId;

    public StudentRepository(PersistenceExecutor persistenceExecutor, StudentFactory studentFactory) {
        this.persistenceExecutor = persistenceExecutor;
        this.studentFactory = studentFactory;

        this.students = new ArrayList<>();

        this.nextId = 1;
    }

    /**
     * Adds a new telecommunications student to the database.
     * 
     * @param firstName the student's first name
     * @param lastName the student's last name
     * @param birthYear the student's birth year
     * @return the assigned student ID
     */
    public int addTelecommunicationsStudent(String firstName, String lastName, int birthYear) {
        int id = nextId++;
        students.add(new TelecommunicationsStudent(id, firstName, lastName, birthYear));
        return id;
    }

    /**
     * Adds a new cybersecurity student to the database.
     * 
     * @param firstName the student's first name
     * @param lastName the student's last name
     * @param birthYear the student's birth year
     * @return the assigned student ID
     */
    public int addCybersecurityStudent(String firstName, String lastName, int birthYear) {
        int id = nextId++;
        students.add(new CybersecurityStudent(id, firstName, lastName, birthYear));
        return id;
    }

    /**
     * Finds a student by their unique ID.
     * 
     * @param id the student ID to search for
     * @return the Student object if found, null otherwise
     */
    public Student findStudentById(int id) {
        return students
                .stream()
                .filter(student -> student.getId() == id)
                .findFirst().orElse(null);
    }

    /**
     * Adds a grade to a specific student's record.
     * 
     * @param studentId the ID of the student
     * @param grade the grade to add (1-5)
     * @return true if the grade was added successfully, false if a student was not found
     */
    public boolean addGradeToStudent(int studentId, int grade) {
        Student student = findStudentById(studentId);
        if (student != null) {
            student.addGrade(grade);
            return true;
        }
        return false;
    }

    /**
     * Removes a student from the database.
     * 
     * @param studentId the ID of the student to remove
     * @return true if the student was removed, false if not found
     */
    public boolean removeStudent(int studentId) {
        return students.removeIf(student -> student.getId() == studentId);
    }

    /**
     * Returns an immutable copy of all students in the database.
     * 
     * @return a list containing all students, modifications won't affect the original
     */
    public List<Student> getAllStudents() {
        return List.copyOf(students);
    }

    /**
     * Filters students by their class type.
     * 
     * @param type the class type to filter by
     * @return a list of students matching the specified type
     */
    public List<Student> getStudentsByType(Class<? extends Student> type) {
        return students.stream()
                .filter(type::isInstance)
                .toList();
    }

    /**
     * Returns all students sorted alphabetically by last name.
     * 
     * @return a new list of students sorted by last name
     */
    public List<Student> getSortedStudentsByLastName() {
        return students.stream()
                .sorted(Comparator.comparing(Student::getLastName))
                .toList();
    }

    /**
     * Calculates the average grade for all students of a specific type.
     * Only includes students who have at least one grade.
     * 
     * @param type the student class type to calculate average for
     * @return the average grade, or 0.0 if no students of this type have grades
     */
    public double getAverageGradeByType(Class<? extends Student> type) {
        List<Student> filteredStudents = getStudentsByType(type);
        if (filteredStudents.isEmpty()) {
            return 0.0;
        }

        return filteredStudents.stream()
                .filter(student -> !student.getGrades().isEmpty())
                .mapToDouble(Student::getAverageGrade)
                .average()
                .orElse(0.0);
    }

    /**
     * Saves all current student data to the database.
     * Creates tables if they don't exist and clears existing data before saving.
     */
    public void saveToDatabase() {
        createTablesIfNotExist();
        clearDatabase();

        for (Student student : students) {
            saveStudentToDatabase(student);
        }
    }

    /**
     * Loads student data from the database.
     * Replaces all current in-memory data with data from the database.
     */
    public void loadFromDatabase() {
        createTablesIfNotExist();

        students.clear();
        nextId = 1;

        persistenceExecutor.performOperation(this::doLoadStudent);

        for (Student student : students) {
            persistenceExecutor.performPreparedOperation(SELECT_GRADES_BY_STUDENT, preparedStatement -> {
                populateStudentGrades(student, preparedStatement);
            });
        }
    }

    @SneakyThrows
    private void doLoadStudent(Statement stmt) {
        try (ResultSet rs = stmt.executeQuery(SELECT_ALL_STUDENTS)) {
            while (rs.next()) {
                StudentData data = new StudentData(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("birth_year"));

                StudentType type = StudentType.fromString(rs.getString("student_type"));
                Student student = studentFactory.createStudent(type, data);

                students.add(student);

                int id = data.id();
                if (id >= nextId) {
                    nextId = id + 1;
                }
            }
        }
    }

    @SneakyThrows
    private static void populateStudentGrades(Student student, PreparedStatement preparedStatement) {
        preparedStatement.setInt(1, student.getId());

        try (ResultSet rs = preparedStatement.executeQuery()) {
            while (rs.next()) {
                int grade = rs.getInt("grade");
                student.addGrade(grade);
            }
        }
    }

    private void createTablesIfNotExist() {
        persistenceExecutor.performSimpleOperationsChain(CREATE_STUDENTS_TABLE, CREATE_GRADES_TABLE);
    }

    private void clearDatabase() {
        persistenceExecutor.performSimpleOperationsChain(DELETE_GRADES, DELETE_STUDENTS);
    }

    private void saveStudentToDatabase(Student student) {
        persistenceExecutor.performPreparedOperation(
                INSERT_STUDENT, preparedStatement -> doInsertStudent(student, preparedStatement));
        persistenceExecutor.performPreparedOperation(
                INSERT_GRADE, preparedStatement -> doInsertGrades(student, preparedStatement));
    }

    @SneakyThrows
    private static void doInsertGrades(Student student, PreparedStatement preparedStatement) {
        for (int grade : student.getGrades()) {
            preparedStatement.setInt(1, student.getId());
            preparedStatement.setInt(2, grade);
            preparedStatement.executeUpdate();
        }
    }

    @SneakyThrows
    private static void doInsertStudent(Student student, PreparedStatement preparedStatement) {
        preparedStatement.setInt(1, student.getId());
        preparedStatement.setString(2, student.getFirstName());
        preparedStatement.setString(3, student.getLastName());
        preparedStatement.setInt(4, student.getBirthYear());

        if (student instanceof TelecommunicationsStudent) {
            preparedStatement.setString(5, "TELEKOM");
        } else {
            preparedStatement.setString(5, "CYBER");
        }

        preparedStatement.executeUpdate();
    }
}