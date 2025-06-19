package me.chironex.studentsystem.data.lang;

import java.io.*;
import java.util.Properties;

/**
 * Handles internationalization and localization of application text.
 * Manages loading translations from properties files with fallback to default values.
 *
 * @author chmodxChironex
 * @since 1.0
 */
public class LangSource {
    private Properties translations;
    private static final String DEFAULT_LANG_FILE = "lang.properties";

    public LangSource() {
        this.translations = new Properties();
        loadTranslations();
    }

    /**
     * Retrieves the translated text for the given language entry.
     * Falls back to the default value if no translation is found.
     *
     * @param entry the language entry to get translation for
     * @return the translated text, or default value if translation not found
     */
    public String getTranslation(LangEntry entry) {
        String key = entry.name();
        String translation = translations.getProperty(key);
        return translation != null ? translation : entry.getDefaultValue();
    }

    private void loadTranslations() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(DEFAULT_LANG_FILE)) {
            if (input != null) {
                translations.load(input);
            } else {
                System.err.println("Translation file not found in resources, creating default.");
                createDefaultLanguageFile();
            }
        } catch (IOException e) {
            createDefaultLanguageFile();
        }
    }

    private void createDefaultLanguageFile() {
        Properties defaultProps = new Properties();

        for (LangEntry entry : LangEntry.values()) {
            defaultProps.setProperty(entry.name(), entry.getDefaultValue());
        }

        try (OutputStream output = new FileOutputStream(DEFAULT_LANG_FILE)) {
            defaultProps.store(output, "Default language file");
            this.translations = defaultProps;
        } catch (IOException e) {
            System.err.println("Could not create default language file: " + e.getMessage());
            this.translations = new Properties();
        }
    }
}
