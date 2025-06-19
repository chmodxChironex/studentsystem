package me.chironex.studentsystem.data.student;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Enum representing types of students.
 */
public enum StudentType {
    TELEKOM,
    CYBERSECURITY;

    /**
     * Converts a string to a StudentType, defaults to CYBERSECURITY if not recognized.
     *
     * @param type the string representation of the type
     * @return the corresponding StudentType
     */
    public static @NotNull StudentType fromString(@NotNull String type) {
        Objects.requireNonNull(type, "Student type cannot be null");

        try {
            return valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            return getDefaultType();
        }
    }

    /**
     * Returns the default student type.
     *
     * @return the default StudentType
     */
    public static @NotNull StudentType getDefaultType() {
        return CYBERSECURITY;
    }
}