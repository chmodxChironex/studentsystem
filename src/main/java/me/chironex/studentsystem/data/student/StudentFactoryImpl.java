package me.chironex.studentsystem.data.student;

/**
 * Implementation of StudentFactory for creating student instances.
 */
public class StudentFactoryImpl implements StudentFactory {

    @Override
    public Student createStudent(StudentType type, StudentData data) {
        int id = data.id();
        String firstName = data.firstName();
        String lastName = data.lastName();
        int birthYear = data.birthYear();

        Student student;
        if (type.equals(StudentType.TELEKOM)) {
            student = new TelecommunicationsStudent(id, firstName, lastName, birthYear);
        } else {
            student = new CybersecurityStudent(id, firstName, lastName, birthYear);
        }

        return student;
    }
}