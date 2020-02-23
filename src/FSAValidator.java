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

        ArrayList<String> input = input();

        if (checkE5(input)) {
            ArrayList<String> states = getParameters(input.get(0).substring(STATES.length(), input.get(0).length() - 1));
            ArrayList<String> alphabet = getParameters(input.get(1).substring(ALPHABET.length(), input.get(1).length() - 1));
            ArrayList<String> initialState = getParameters(input.get(2).substring(INITIAL_STATE.length(), input.get(2).length() - 1));
            ArrayList<String> finiteStates = getParameters(input.get(3).substring(FINITE_STATES.length(), input.get(3).length() - 1));
            ArrayList<String> transitionFunction = getParameters(input.get(4).substring(TRANSITION_FUNCTION.length(), input.get(4).length() - 1));

            if (checkE1(states, initialState, finiteStates, transitionFunction)) {
                if (checkE2()) {
                    if (checkE3(alphabet, transitionFunction)) {
                        if (checkE4(initialState)) {

                        }
                    }
                }
            }
        }

        printWriter.close();
    }

    static ArrayList<String> input() throws FileNotFoundException {
        Scanner scanner = new Scanner(new File(INPUT_FILE));
        ArrayList<String> input = new ArrayList<>(COUNT_LINES);

        while (scanner.hasNext()) {
            input.add(scanner.nextLine());
        }
        scanner.close();

        return input;
    }

    static boolean checkE1(ArrayList<String> states, ArrayList<String> initialState, ArrayList<String> finiteStates, ArrayList<String> transitionFunction) throws FileNotFoundException {

        for (String state : initialState) {
            if (!states.contains(state)) {
                printE1(state);
                return false;
            }
        }


        for (String state : finiteStates) {
            if (!states.contains(state)) {
                printE1(state);
                return false;
            }
        }

        for (String string : transitionFunction) {
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

    static void printE1(String state) throws FileNotFoundException {
        printWriter.println("Error:");
        printWriter.println("E1: A state '" + state + "' is not in the set of states");
    }

    static boolean checkE2() {
        return true;
    }

    static boolean checkE3(ArrayList<String> alphabet, ArrayList<String> transitionFunction) throws FileNotFoundException {
        for (String string : transitionFunction) {
            String[] alpha = string.split(">");
            if (!alphabet.contains(alpha[1])) {
                printWriter.println("Error:");
                printWriter.println("E3: A transition '" + alpha[1] + "' is not represented in the alphabet");
                return false;
            }
        }

        return true;
    }

    static boolean checkE4(ArrayList<String> initialState) throws FileNotFoundException {
        if (initialState == null || initialState.size() != 1) {
            printWriter.println("Error:");
            printWriter.println("E4: Initial state is not defined");
            return false;
        }
        return true;
    }

    static boolean checkE5(ArrayList<String> input) throws FileNotFoundException {
        boolean exception = false;

        if (input.size() != COUNT_LINES) {
            exception = true;
        }

        if (!exception) {
            exception = !validateStatesString(input.get(0));
        }

        if (!exception) {
            exception = !validateAlphabetString(input.get(1));
        }

        if (!exception) {
            exception = !validateInitialStateString(input.get(2));
        }


        if (!exception) {
            exception = !validateFiniteStatesString(input.get(3));
        }

        if (!exception) {
            exception = !validateTransitionFunctionString(input.get(4));
        }

        if (exception) {
            printWriter.println("Error:");
            printWriter.println("E5: Input file is malformed");
        }

        return !exception;
    }

    static boolean validateStatesString(String states) {

        if (!states.substring(0, STATES.length()).equals(STATES)) {
            return false;
        }

        if (states.charAt(states.length() - 1) != ']') {
            return false;
        }

        if (!validateParameters(states.substring(STATES.length(), states.length() - 1), false, false)) {
            return false;
        }

        return true;
    }

    static boolean validateAlphabetString(String alphabet) {
        if (!alphabet.substring(0, ALPHABET.length()).equals(ALPHABET)) {
            return false;
        }

        if (alphabet.charAt(alphabet.length() - 1) != ']') {
            return false;
        }

        if (!validateParameters(alphabet.substring(ALPHABET.length(), alphabet.length() - 1), true, false)) {
            return false;
        }

        return true;
    }

    static boolean validateInitialStateString(String initialState) {
        if (!initialState.substring(0, INITIAL_STATE.length()).equals(INITIAL_STATE)) {
            return false;
        }

        if (initialState.charAt(initialState.length() - 1) != ']') {
            return false;
        }

        if (!validateParameters(initialState.substring(INITIAL_STATE.length(), initialState.length() - 1), false, false)) {
            return false;
        }

        return true;
    }

    static boolean validateFiniteStatesString(String finiteStates) {
        if (!finiteStates.substring(0, FINITE_STATES.length()).equals(FINITE_STATES)) {
            return false;
        }

        if (finiteStates.charAt(finiteStates.length() - 1) != ']') {
            return false;
        }

        if (!validateParameters(finiteStates.substring(FINITE_STATES.length(), finiteStates.length() - 1), false, false)) {
            return false;
        }

        return true;
    }

    static boolean validateTransitionFunctionString(String transitionFunction) {
        if (!transitionFunction.substring(0, TRANSITION_FUNCTION.length()).equals(TRANSITION_FUNCTION)) {
            return false;
        }

        if (transitionFunction.charAt(transitionFunction.length() - 1) != ']') {
            return false;
        }

        if (!validateParameters(transitionFunction.substring(TRANSITION_FUNCTION.length(), transitionFunction.length() - 1), true, true)) {
            return false;
        }

        return true;
    }

    static boolean validateParameters(String string, boolean underscore, boolean transition) {
        String[] parameters = string.split(",");
        for (String parameter : parameters) {
            if (!checkCharacters(parameter, underscore, transition)) {
                return false;
            }
        }
        return true;
    }

    static boolean checkCharacters(String string, boolean underscore, boolean transition) {
        if (transition) {
            if (string.split(">").length != 3) {
                return false;
            }
        }

        for (int i = 0; i < string.length(); i++) {
            char x = string.charAt(i);
            if (!(isDigit(x) || isLatinLetters(x))) {
                if (!(underscore && x == '_')) {
                    if (!(transition && x == '>' && i != 0 && i != string.length() - 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    static boolean isDigit(char x) {
        char[] digits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        for (char digit : digits) {
            if (x == digit) {
                return true;
            }
        }
        return false;
    }

    static boolean isLatinLetters(char x) {
        String letters = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
        for (int i = 0; i < letters.length(); i++) {
            if (x == letters.charAt(i)) {
                return true;
            }
        }
        return false;
    }

    static ArrayList<String> getParameters(String string) {
        if (string.length() != 0) {
            return new ArrayList<>(Arrays.asList(string.split(",")));
        }

        return new ArrayList<>();
    }

}


