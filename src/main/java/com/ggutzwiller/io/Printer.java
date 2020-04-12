package com.ggutzwiller.io;

/**
 * Prints details to output and errors.
 */
public class Printer {
    public static void log(String message) {
        System.err.println(message);
    }

    public static void error(String message) {
        System.err.println("ERROR - " + message);
    }

    public static void order(String message) {
        System.out.println(message);
    }
}
