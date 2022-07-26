package io.d2a.dhbw.uk2.lib;

import io.d2a.dhbw.uk2.CoronaWarnTerm;
import io.d2a.dhbw.uk2.JPhone;
import io.d2a.dhbw.uk2.Token;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import javax.swing.UIManager;

public class CoronaWarn {

    /**
     * Application entry point for CoronaWarn
     *
     * @param args command line arguments, not used here
     */
    public static void main(String[] args) {
        try {
            // Only necessary on MacOS to prevent color issues with standard look and feel.
            // Redundant on other operation systems - they use this look and feel by default.
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (final Exception e) {
        }

        JPhone phone1 = new JPhone("0123-4567", "Markus");
        JPhone phone2 = new JPhone("9876-5432", "Angela");
        JPhone phone3 = new JPhone("4711-0815", "Olaf");

        CoronaWarnTerm client1 = new CoronaWarnTerm(phone1);
        CoronaWarnTerm client2 = new CoronaWarnTerm(phone2);
        CoronaWarnTerm client3 = new CoronaWarnTerm(phone3);

        CoronaWarnAPI.registerClients(client1, client2, client3);
    }


    /**
     * Clear token store for phone
     *
     * @param phone phone to clear store for
     */
    public static void clearTokenStore(JPhone phone) {
        if (!phone.getTokenFile().delete()) {
            System.out.println("cannot delete token file for phone " + phone.id());
        }
        // ADD CODE HERE
        System.out.println("[CLEAR] Tokens for " + phone);
    }

    /**
     * Load tokens for phone
     *
     * @param phone phone to load tokens for
     * @return loaded tokens
     */
    public static List<Token> loadTokens(JPhone phone) {
        final List<Token> tokens = new LinkedList<>();

        final File file = phone.getTokenFile();
        if (!file.exists()) {
            return tokens; // empty list
        }

        try {
            for (final String line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                if (line.isBlank()) {
                    continue;
                }
                final Token token = parseToken(line);
                System.out.println("Read token " + token + " for " + phone);
                tokens.add(token);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return tokens;
    }

    /**
     * Save token for provided phone
     *
     * @param phone phone to save token for
     * @param token token to save
     */
    public static void saveToken(JPhone phone, Token token) {
        String line = token.value + ";" + token.date.getTime() + '\n';

        final File file = phone.getTokenFile();
        try {
            if (!file.exists() && !file.createNewFile()) {
                System.out.println("WARN: cannot create file");
            }
            Files.writeString(file.toPath(), line, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parse a token line
     *
     * @param line token line to parse
     * @return parsed token instance
     */
    private static Token parseToken(String line) {
        String[] parts = line.split("[;]");
        if (parts.length == 2) {
            try {
                return new Token(parts[0], new Date(Long.parseLong(parts[1])));
            } catch (Exception e) {
                System.err.println("Error parsing token line: " + line);
                e.printStackTrace();
            }
        }
        return null;
    }
}
