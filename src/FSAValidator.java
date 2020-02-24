import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Roman Soldatov BS19-02
 */
public class FSAValidator {
    static final String INPUT_FILE = "fsa.txt";
    static final String OUTPUT_FILE = "result.txt";
    static final int COUNT_LINES = 5;

    static final String STATES = "states=[";
    static final String ALPHABET = "alpha=[";
    static final String INITIAL_STATE = "init.st=[";
    static final String FINITE_STATES = "fin.st=[";
    static final String TRANSITION_FUNCTION = "trans=[";

    static PrintWriter printWriter;

    public static void main(String[] args) throws FileNotFoundException {

        printWriter = new PrintWriter(new File(OUTPUT_FILE));

        // Input strings from the file
        ArrayList<String> input = input();

        // Check if Input file is malformed.
        if (checkE5(input)) {
            // Get strings of states, alphabet, initial state, finite states and transition functions.
            ArrayList<String> states = getParameters(input.get(0).substring(STATES.length(), input.get(0).length() - 1));
            ArrayList<String> alphabet = getParameters(input.get(1).substring(ALPHABET.length(), input.get(1).length() - 1));
            ArrayList<String> initialState = getParameters(input.get(2).substring(INITIAL_STATE.length(), input.get(2).length() - 1));
            ArrayList<String> finiteStates = getParameters(input.get(3).substring(FINITE_STATES.length(), input.get(3).length() - 1));
            ArrayList<String> transitionFunctions = getParameters(input.get(4).substring(TRANSITION_FUNCTION.length(), input.get(4).length() - 1));

            // Check for E1 error
            if (checkE1(states, initialState, finiteStates, transitionFunctions)) {
                // Check for E2 error
                if (checkE2()) {
                    // Check for E3 error
                    if (checkE3(alphabet, transitionFunctions)) {
                        // Check for E4 error
                        if (checkE4(initialState)) {

                        }
                    }
                }
            }
        }

        printWriter.close();
    }

    /**
     * Inout Lines from the file
     *
     * @return Array List of strings
     */
    static ArrayList<String> input() {
        ArrayList<String> input = new ArrayList<>(COUNT_LINES);
        Scanner scanner;
        try {
            scanner = new Scanner(new File(INPUT_FILE));
            while (scanner.hasNext()) {
                input.add(scanner.nextLine());
            }
            scanner.close();
        } // If file has not been found then we do nothing.
        catch (FileNotFoundException e) {
        }

        return input;
    }

    /**
     * Checks for E1 error.
     *
     * @return true if there is no error.
     */
    static boolean checkE1(ArrayList<String> states, ArrayList<String> initialState, ArrayList<String> finiteStates, ArrayList<String> transitionFunctions) {

        // Check initial state
        for (String state : initialState) {
            if (!states.contains(state)) {
                printE1(state);
                return false;
            }
        }

        // Check finite states
        for (String state : finiteStates) {
            if (!states.contains(state)) {
                printE1(state);
                return false;
            }
        }

        // Check states from transition functions
        for (String string : transitionFunctions) {
            String[] st = string.split(">");
            if (!states.contains(st[0])) {
                printE1(st[0]);
                return false;
            }

            if (!states.contains(st[2])) {
                printE1(st[2]);
                return false;
            }
        }

        return true;
    }

    static void printE1(String state) {
        printWriter.println("Error:");
        printWriter.println("E1: A state '" + state + "' is not in the set of states");
    }

    /**
     * Checks for E2 error.
     *
     * @return true if there is no error.
     */
    static boolean checkE2() {
        return true;
    }

    /**
     * Checks for E3 error.
     *
     * @return true if there is no error.
     */
    static boolean checkE3(ArrayList<String> alphabet, ArrayList<String> transitionFunctions) {
        for (String string : transitionFunctions) {
            String[] alpha = string.split(">");
            if (!alphabet.contains(alpha[1])) {
                printWriter.println("Error:");
                printWriter.println("E3: A transition '" + alpha[1] + "' is not represented in the alphabet");
                return false;
            }
        }

        return true;
    }

    /**
     * Checks for E4 error.
     *
     * @return true if there is no error.
     */
    static boolean checkE4(ArrayList<String> initialState) {
        if (initialState == null || initialState.size() != 1) {
            printWriter.println("Error:");
            printWriter.println("E4: Initial state is not defined");
            return false;
        }
        return true;
    }

    /**
     * Checks for E5 error.
     *
     * @return true if there is no error.
     */
    static boolean checkE5(ArrayList<String> input) {
        boolean exception = false;

        // Checks if the input contains exactly 5 lines.
        if (input.size() != COUNT_LINES) {
            exception = true;
        }

        // Checks if the string with states is valid.
        if (!exception) {
            exception = !validateStatesString(input.get(0));
        }

        // Checks if the string with alphabet is valid.
        if (!exception) {
            exception = !validateAlphabetString(input.get(1));
        }

        // Checks if the string with initial state is valid.
        if (!exception) {
            exception = !validateInitialStateString(input.get(2));
        }

        // Checks if the string with finite states is valid.
        if (!exception) {
            exception = !validateFiniteStatesString(input.get(3));
        }

        // Checks if the string with transition functions is valid.
        if (!exception) {
            exception = !validateTransitionFunctionsString(input.get(4));
        }

        // Print message if there is an error.
        if (exception) {
            printWriter.println("Error:");
            printWriter.println("E5: Input file is malformed");
        }

        return !exception;
    }

    static boolean validateStatesString(String states) {

        // If the start of a sting is correct.
        if (!states.substring(0, STATES.length()).equals(STATES)) {
            return false;
        }

        // If the end of a sting is correct.
        if (states.charAt(states.length() - 1) != ']') {
            return false;
        }

        // If parameters are correct.
        if (!validateParameters(states.substring(STATES.length(), states.length() - 1), false, false)) {
            return false;
        }

        return true;
    }

    static boolean validateAlphabetString(String alphabet) {

        // If the start of a sting is correct.
        if (!alphabet.substring(0, ALPHABET.length()).equals(ALPHABET)) {
            return false;
        }

        // If the end of a sting is correct.
        if (alphabet.charAt(alphabet.length() - 1) != ']') {
            return false;
        }

        // If parameters are correct.
        if (!validateParameters(alphabet.substring(ALPHABET.length(), alphabet.length() - 1), true, false)) {
            return false;
        }

        return true;
    }

    static boolean validateInitialStateString(String initialState) {

        // If the start of a sting is correct.
        if (!initialState.substring(0, INITIAL_STATE.length()).equals(INITIAL_STATE)) {
            return false;
        }

        // If the end of a sting is correct.
        if (initialState.charAt(initialState.length() - 1) != ']') {
            return false;
        }

        // If parameters are correct.
        if (!validateParameters(initialState.substring(INITIAL_STATE.length(), initialState.length() - 1), false, false)) {
            return false;
        }

        return true;
    }

    static boolean validateFiniteStatesString(String finiteStates) {

        // If the start of a sting is correct.
        if (!finiteStates.substring(0, FINITE_STATES.length()).equals(FINITE_STATES)) {
            return false;
        }

        // If the end of a sting is correct.
        if (finiteStates.charAt(finiteStates.length() - 1) != ']') {
            return false;
        }

        // If parameters are correct.
        if (!validateParameters(finiteStates.substring(FINITE_STATES.length(), finiteStates.length() - 1), false, false)) {
            return false;
        }

        return true;
    }

    static boolean validateTransitionFunctionsString(String transitionFunction) {

        // If the start of a sting is correct.
        if (!transitionFunction.substring(0, TRANSITION_FUNCTION.length()).equals(TRANSITION_FUNCTION)) {
            return false;
        }

        // If the end of a sting is correct.
        if (transitionFunction.charAt(transitionFunction.length() - 1) != ']') {
            return false;
        }

        // If parameters are correct.
        if (!validateParameters(transitionFunction.substring(TRANSITION_FUNCTION.length(), transitionFunction.length() - 1), true, true)) {
            return false;
        }

        return true;
    }

    /**
     * Analyse each parameter for correctness.
     *
     * @param string     contains parameters seperated by comma.
     * @param underscore - if symbol '_' is allowed.
     * @param transition - if symbol '>' is allowed.
     * @return true if all parameters are correct, false - otherwise.
     */
    static boolean validateParameters(String string, boolean underscore, boolean transition) {

        String[] parameters = string.split(",");
        for (String parameter : parameters) {
            if (!checkCharacters(parameter, underscore, transition)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check each symbol of a parameter.
     *
     * @param underscore - if symbol '_' is allowed.
     * @param transition - if symbol '>' is allowed.
     * @return true if a parameter is correct.
     */
    static boolean checkCharacters(String parameter, boolean underscore, boolean transition) {
        // Transition function must contain exactly 3 parameters
        if (transition) {
            if (parameter.split(">").length != 3) {
                return false;
            }
        }

        for (int i = 0; i < parameter.length(); i++) {
            char x = parameter.charAt(i);
            if (!(isDigit(x) || isLatinLetters(x))) {
                if (!(underscore && x == '_')) {
                    if (!(transition && x == '>' && i != 0 && i != parameter.length() - 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Check if this symbol is a digit.
     *
     * @param x - symbol to check
     * @return true if x is digit
     */
    static boolean isDigit(char x) {
        char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        for (char digit : digits) {
            if (x == digit) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if this symbol is a latin letter.
     *
     * @param x - symbol to check
     * @return true of x is a latin letter.
     */
    static boolean isLatinLetters(char x) {
        String letters = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
        for (int i = 0; i < letters.length(); i++) {
            if (x == letters.charAt(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param string - parameters separated by comma.
     * @return Array List of parameters.
     */
    static ArrayList<String> getParameters(String string) {
        if (string.length() != 0) {
            return new ArrayList<>(Arrays.asList(string.split(",")));
        }

        return new ArrayList<>();
    }
}


