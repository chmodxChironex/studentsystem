package me.chironex.studentsystem.data.lang;

import lombok.Getter;

/**
 * Enum representing language entries for translations.
 */
public enum LangEntry {

    GUI_TITLE(Translation.GUI_TITLE);

    @Getter
    private final String defaultValue;

    /**
     * Constructs a language entry with a default value.
     *
     * @param defaultValue the default translation value
     */
    LangEntry(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}