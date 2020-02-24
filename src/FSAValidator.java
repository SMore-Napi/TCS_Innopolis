import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Roman Soldatov BS19-02
 */
/*
states=[p0,p1,p2,p3,p4]
alpha=[a,b,c]
init.st=[p0]
fin.st=[]
trans=[p0>b>p1,p0>a>p3,p3>a>p1,p2>c>p4,p4>b>p2,p4>c>p2,p1>c>p2]
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
        checkFSAValidator();
        printWriter.close();
    }

    /**
     * Input Lines from the file
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

    static void checkFSAValidator() {
        // Input strings from the file.
        ArrayList<String> input = input();

        // Check for E5 error.
        if (checkE5(input)) {
            // Get strings of states, alphabet, initial state, finite states and transition functions.
            ArrayList<String> states = getParameters(input.get(0).substring(STATES.length(), input.get(0).length() - 1));
            ArrayList<String> alphabet = getParameters(input.get(1).substring(ALPHABET.length(), input.get(1).length() - 1));
            ArrayList<String> initialState = getParameters(input.get(2).substring(INITIAL_STATE.length(), input.get(2).length() - 1));
            ArrayList<String> finiteStates = getParameters(input.get(3).substring(FINITE_STATES.length(), input.get(3).length() - 1));
            ArrayList<String> transitionFunctions = getParameters(input.get(4).substring(TRANSITION_FUNCTION.length(), input.get(4).length() - 1));

            // Check for E1 error.
            if (checkE1(states, initialState, finiteStates, transitionFunctions)) {
                // Check for E3 error.
                if (checkE3(alphabet, transitionFunctions)) {
                    // Check for E2 error.
                    if (checkE2(states, alphabet, transitionFunctions)) {
                        // Check for E4 error.
                        if (checkE4(initialState)) {
                            boolean isComplete = true;

                            if (checkW1(isComplete)) {
                                isComplete = false;
                            }
                            if (checkW2(isComplete)) {
                                isComplete = false;
                            }
                            if (checkW3(isComplete)) {
                                isComplete = false;
                            }

                            if (isComplete) {
                                printWriter.println("FSA is complete");
                            }

                        }
                    }
                }
            }
        }
    }

    static boolean checkW1(boolean isComplete) {
        //todo
        return true;
    }

    static boolean checkW2(boolean isComplete) {
        //todo
        return true;
    }

    static boolean checkW3(boolean isComplete) {
        //todo
        return true;
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
    static boolean checkE2(ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> transitionFunctions) {
        ArrayList<String> connectedStates = getConnectedStates(states, alphabet, transitionFunctions);
        if (connectedStates.size() != states.size()) {
            printWriter.println("Error:");
            printWriter.println("E2: Some states are disjoint");
            return false;
        }
        return true;
    }

    // Creating table that looks like.
    //          alphabet el1 alphabet el2 alphabet el3 ... alphabet elM
    // state1
    // state2
    // state3
    // ...
    // stateN

    /**
     * Elements of this table – states.
     */
    static String[][] getIncidenceMatrix(ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> transitionFunctions) {
        String[][] table = new String[states.size()][alphabet.size()];

        for (String transition : transitionFunctions) {
            String[] tokens = transition.split(">");
            int n = getIndex(tokens[0], states);
            int m = getIndex(tokens[1], alphabet);
            table[n][m] = tokens[2];
        }

        return table;
    }

    /**
     * Get the index of an element in ArrayList
     *
     * @param object - element which index is required to know.
     * @param list   - Array List of elements.
     * @return index of an object.
     */
    static int getIndex(String object, ArrayList<String> list) {
        for (int j = 0; j < list.size(); j++) {
            if (list.get(j).equals(object)) {
                return j;
            }
        }

        return -1;
    }

    static ArrayList<String> getConnectedStates(ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> transitionFunctions) {
        String[][] table = getIncidenceMatrix(states, alphabet, transitionFunctions);

        int startState = 0;
        int countCheckedStates = 0;
        boolean checkNewStates = true;

        ArrayList<String> connectedStates = new ArrayList<>(states.size());
        connectedStates.add(states.get(startState));


        while (checkNewStates) {
            checkNewStates = false;

            ArrayList<String> statesToVisit = new ArrayList<>(states.size());
            for (int i = 0; i < table[startState].length; i++) {
                if (table[startState][i] != null) {
                    statesToVisit.add(table[startState][i]);
                }
            }

            for (String state : statesToVisit) {
                if (!connectedStates.contains(state)) {
                    checkNewStates = true;
                    connectedStates.add(state);
                }
            }

            if (countCheckedStates < connectedStates.size() - 1) {
                checkNewStates = true;
                String stateToCheck = connectedStates.get(++countCheckedStates);
                startState = getIndex(stateToCheck, states);
            }
        }

        return connectedStates;
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

    class State {
        String name;
        ArrayList<String> transitions;

        State(String name) {
            this.name = name;
            transitions = new ArrayList<>();
        }
    }
}


