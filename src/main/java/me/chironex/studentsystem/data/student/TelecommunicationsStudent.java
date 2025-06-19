package me.chironex.studentsystem.data.student;

import me.chironex.studentsystem.util.TelecomUtils;

import java.io.Serial;

/**
 * Represents a telecommunications student with Morse code skill.
 */
public class TelecommunicationsStudent extends Student {
    @Serial
    private static final long serialVersionUID = 2L;

    protected TelecommunicationsStudent(int id, String firstName, String lastName, int birthYear) {
        super(id, firstName, lastName, birthYear);
    }

    @Override
    public String executeSkill() {
        String fullName = getFirstName() + " " + getLastName();
        return TelecomUtils.convertToMorseCode(fullName);
    }
}