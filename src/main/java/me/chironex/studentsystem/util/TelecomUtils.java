package me.chironex.studentsystem.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for telecommunications-related operations.
 * Provides functionality for converting text to Morse code.
 * 
 * @author chmodxChironex
 * @since 1.0
 */
public final class TelecomUtils {

    private TelecomUtils() {
    }

    /**
     * Converts the given text to Morse code representation.
     * Supports uppercase letters A-Z, digits 0-9, and spaces.
     * Unknown characters are silently ignored.
     * 
     * @param text the text to convert to Morse code
     * @return the Morse code representation with dots and dashes, 
     *         spaces separate characters, double spaces separate words
     */
    public static String convertToMorseCode(String text) {
        Map<Character, String> morseMap = new HashMap<>();

        morseMap.put('A', ".-");
        morseMap.put('B', "-...");
        morseMap.put('C', "-.-.");
        morseMap.put('D', "-..");
        morseMap.put('E', ".");
        morseMap.put('F', "..-.");
        morseMap.put('G', "--.");
        morseMap.put('H', "....");
        morseMap.put('I', "..");
        morseMap.put('J', ".---");
        morseMap.put('K', "-.-");
        morseMap.put('L', ".-..");
        morseMap.put('M', "--");
        morseMap.put('N', "-.");
        morseMap.put('O', "---");
        morseMap.put('P', ".--.");
        morseMap.put('Q', "--.-");
        morseMap.put('R', ".-.");
        morseMap.put('S', "...");
        morseMap.put('T', "-");
        morseMap.put('U', "..-");
        morseMap.put('V', "...-");
        morseMap.put('W', ".--");
        morseMap.put('X', "-..-");
        morseMap.put('Y', "-.--");
        morseMap.put('Z', "--..");
        morseMap.put('0', "-----");
        morseMap.put('1', ".----");
        morseMap.put('2', "..---");
        morseMap.put('3', "...--");
        morseMap.put('4', "....-");
        morseMap.put('5', ".....");
        morseMap.put('6', "-....");
        morseMap.put('7', "--...");
        morseMap.put('8', "---..");
        morseMap.put('9', "----.");
        morseMap.put(' ', " ");

        StringBuilder result = new StringBuilder();

        for (char c : text.toUpperCase().toCharArray()) {
            if (c == ' ') {
                result.append("  ");
            } else if (morseMap.containsKey(c)) {
                result.append(morseMap.get(c)).append(" ");
            }
        }

        return result.toString().trim();
    }
}