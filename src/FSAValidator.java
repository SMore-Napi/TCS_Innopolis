import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Roman Soldatov BS19-02
 */
public class FSAValidator {
    static final String INPUT_FILE = "fsa.txt";
    static final String OUTPUT_FILE = "result.txt";
    static final int COUNT_LINES = 5;

    // The start of input lines.
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
    static ArrayList<String> readFile() {
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
     * FSA Validator.
     * Check for:
     * E1, E2, E3, E4, E5 errors;
     * is FSA complete/incomplete;
     * W1, W2, W3 warnings.
     */
    static void checkFSAValidator() {
        // Input strings from the file.
        ArrayList<String> input = readFile();

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
                    if (checkE2(states, transitionFunctions)) {
                        // Check for E4 error.
                        if (checkE4(initialState)) {

                            // Print is FSA complete.
                            if (isFSAComplete(states, alphabet, transitionFunctions)) {
                                printWriter.println("FSA is complete");
                            } else {
                                printWriter.println("FSA is incomplete");
                            }

                            // Print Warnings
                            boolean warning1 = !checkW1(finiteStates);
                            boolean warning2 = !checkW2(states, alphabet, initialState, transitionFunctions);
                            boolean warning3 = !checkW3(states, alphabet, transitionFunctions);
                            if (warning1 || warning2 || warning3) {
                                printWriter.println("Warning:");
                                // Check warning 1
                                if (warning1) {
                                    printWriter.println("W1: Accepting state is not defined");
                                }
                                // Check warning 2
                                if (warning2) {
                                    printWriter.println("W2: Some states are not reachable from the initial state");
                                }
                                // Check warning 3
                                if (warning3) {
                                    printWriter.println("W3: FSA is nondeterministic");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if FSA is complete.
     * Each cell of a matrix must contain at least one state.
     *
     * @return true if FSA is complete, false - otherwise.
     */
    static boolean isFSAComplete(ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> transitionFunctions) {

        ArrayList<String>[][] matrix = getStatesAlphabetMatrix(states, alphabet, transitionFunctions);

        for (ArrayList<String>[] row : matrix) {
            for (ArrayList<String> cell : row) {
                if (cell.size() == 0) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check for the Warning 1
     * Is accepting state is not defined.
     *
     * @return true if there is no warning.
     */
    static boolean checkW1(ArrayList<String> finiteStates) {
        return finiteStates.size() >= 1;
    }

    /**
     * Check for the Warning 2
     * If there are no all states in 'connectedStates' list,
     * then it means that not all states are reachable from the initial state.
     *
     * @return true if there is no warning.
     */
    static boolean checkW2(ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> initialState, ArrayList<String> transitionFunctions) {
        ArrayList<String> connectedStates = getConnectedStates(states.indexOf(initialState.get(0)), states, alphabet, transitionFunctions);
        return connectedStates.size() == states.size();
    }

    /**
     * Check for the Warning 3.
     * Is FSA is deterministic.
     * Each cell of a matrix must contain at most one state.
     *
     * @return true if there is no warning.
     */
    static boolean checkW3(ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> transitionFunctions) {

        ArrayList<String>[][] matrix = getStatesAlphabetMatrix(states, alphabet, transitionFunctions);

        for (ArrayList<String>[] row : matrix) {
            for (ArrayList<String> cell : row) {
                if (cell.size() > 1) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Checks for E1 error.
     * If some state from 'initialState', 'finiteStates' or 'transitionFunctions' lists is not defined.
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

    /**
     * Print a message about Error 1.
     *
     * @param state - the state to prtint about.
     */
    static void printE1(String state) {
        printWriter.println("Error:");
        printWriter.println("E1: A state '" + state + "' is not in the set of states");
    }

    /**
     * Checks for E2 error.
     * Is some states are disjoint.
     * <p>
     * If there are no all states in 'jointStates' list,
     * then it means that not all states are connected to each other.
     *
     * @return true if there is no error.
     */
    static boolean checkE2(ArrayList<String> states, ArrayList<String> transitionFunctions) {

        ArrayList<String> jointStates = getJointStates(states, transitionFunctions);

        if (jointStates.size() != states.size()) {
            printWriter.println("Error:");
            printWriter.println("E2: Some states are disjoint");
            return false;
        }

        return true;
    }

    /**
     * Create a list with states which are connected to each other.
     * It starts searching these states from a state with index = 0 in 'ArrayList states'
     *
     * @param states              - list of all states
     * @param transitionFunctions - functions which represent states connection.
     * @return list of linked states.
     */
    static ArrayList<String> getJointStates(ArrayList<String> states, ArrayList<String> transitionFunctions) {

        boolean[][] matrix = getIncidenceMatrix(states, transitionFunctions);

        int countCheckedStates = 0;
        boolean checkNewStates = true;
        int startState = 0;

        ArrayList<String> jointStates = new ArrayList<>(states.size());
        jointStates.add(states.get(startState));

        // Filling 'joinStates' list
        while (checkNewStates) {
            checkNewStates = false;

            // Find new states which are connected with states from 'jointStates' list.
            // Such new states are written in 'statesToVisit' list.
            ArrayList<String> statesToVisit = new ArrayList<>(states.size());
            for (int i = 0; i < matrix[startState].length; i++) {
                if (matrix[startState][i]) {
                    statesToVisit.add(states.get(i));
                }
            }

            // Analyse if new states to check are already checked, i.e. they present in 'jointStates' list.
            for (String state : statesToVisit) {
                if (!jointStates.contains(state)) {
                    checkNewStates = true;
                    jointStates.add(state);
                }
            }

            // If not all states in 'jointStates' are checked, then we start the loop again.
            if (countCheckedStates < jointStates.size() - 1) {
                checkNewStates = true;
                String stateToCheck = jointStates.get(++countCheckedStates);
                startState = states.indexOf(stateToCheck);
            }
        }

        return jointStates;
    }

    /**
     * Creating the table that looks like:
     * alpha.Element_1 alpha.Element_2 alpha.Element_3 ... alpha.Element_M
     * state_1
     * state_2
     * state_3
     * ...
     * state_N
     * <p>
     * Elements of this table – states.
     * Each cell contains a list of states which are reachable from state_i by alpha.Element_j transition.
     */
    static ArrayList<String>[][] getStatesAlphabetMatrix(ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> transitionFunctions) {

        // Initializing the table
        ArrayList<String>[][] table = new ArrayList[states.size()][alphabet.size()];
        for (int i = 0; i < states.size(); i++) {
            for (int j = 0; j < alphabet.size(); j++) {
                table[i][j] = new ArrayList<>();
            }
        }

        // Filling the table
        for (String transition : transitionFunctions) {
            String[] tokens = transition.split(">");
            int n = states.indexOf(tokens[0]);
            int m = alphabet.indexOf(tokens[1]);
            if (!table[n][m].contains(tokens[2])) {
                table[n][m].add(tokens[2]);
            }
        }

        return table;
    }

    /**
     * Creating the matrix that looks like:
     * state_1 state_2 state_3 ... state_N
     * state_1
     * state_2
     * state_3
     * ...
     * state_N
     * <p>
     * Each cell contains 'true' if state_i and state_j are linked, 'false' - otherwise
     */
    static boolean[][] getIncidenceMatrix(ArrayList<String> states, ArrayList<String> transitionFunctions) {

        boolean[][] matrix = new boolean[states.size()][states.size()];

        // Filling the matrix.
        for (String transition : transitionFunctions) {
            String[] tokens = transition.split(">");
            int n = states.indexOf(tokens[0]);
            int m = states.indexOf(tokens[2]);
            matrix[n][m] = true;
            matrix[m][n] = true;
        }

        return matrix;
    }

    /**
     * Create a list with states which are reachable to each other.
     * It starts searching these states from a initial state.
     *
     * @param states              - list of all states
     * @param alphabet            - transitions
     * @param transitionFunctions - functions which represent states connection.
     * @return list of connected states.
     */
    static ArrayList<String> getConnectedStates(int startState, ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> transitionFunctions) {

        ArrayList<String>[][] matrix = getStatesAlphabetMatrix(states, alphabet, transitionFunctions);

        int countCheckedStates = 0;
        boolean checkNewStates = true;

        ArrayList<String> connectedStates = new ArrayList<>(states.size());
        connectedStates.add(states.get(startState));

        // Filling 'connectedStates' list
        while (checkNewStates) {
            checkNewStates = false;

            // Find new states which are connected with states from 'connectedStates' list.
            // Such new states are written in 'statesToVisit' list.
            ArrayList<String> statesToVisit = new ArrayList<>(states.size());
            for (int i = 0; i < matrix[startState].length; i++) {
                statesToVisit.addAll(matrix[startState][i]);
            }

            // Analyse if new states to check are already checked, i.e. they present in 'connectedStates' list.
            for (String state : statesToVisit) {
                if (!connectedStates.contains(state)) {
                    checkNewStates = true;
                    connectedStates.add(state);
                }
            }

            // If not all states in 'connectedStates' are checked, then we start the loop again.
            if (countCheckedStates < connectedStates.size() - 1) {
                checkNewStates = true;
                String stateToCheck = connectedStates.get(++countCheckedStates);
                startState = states.indexOf(stateToCheck);
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
        boolean isCorrect = true;

        // Checks if the input contains exactly 5 lines.
        if (input.size() != COUNT_LINES) {
            isCorrect = false;
        }

        // Checks if the string with states is valid.
        if (isCorrect) {
            isCorrect = validateStatesString(input.get(0));
        }

        // Checks if the string with alphabet is valid.
        if (isCorrect) {
            isCorrect = validateAlphabetString(input.get(1));
        }

        // Checks if the string with initial state is valid.
        if (isCorrect) {
            isCorrect = validateInitialStateString(input.get(2));
        }

        // Checks if the string with finite states is valid.
        if (isCorrect) {
            isCorrect = validateFiniteStatesString(input.get(3));
        }

        // Checks if the string with transition functions is valid.
        if (isCorrect) {
            isCorrect = validateTransitionFunctionsString(input.get(4));
        }

        // Print message if there is an error.
        if (!isCorrect) {
            printWriter.println("Error:");
            printWriter.println("E5: Input file is malformed");
        }

        return isCorrect;
    }

    /**
     * Check if the 1st string is correct.
     *
     * @return true if it is correct.
     */
    static boolean validateStatesString(String states) {

        // If the beginning and the end of a string are correct.
        if (!validateBeginningAndEnd(states, STATES)) {
            return false;
        }

        // If parameters are correct.
        if (!validateParameters(states.substring(STATES.length(), states.length() - 1), false, false)) {
            return false;
        }

        return true;
    }

    /**
     * Check if the 2nd string is correct.
     *
     * @return true if it is correct.
     */
    static boolean validateAlphabetString(String alphabet) {

        // If the beginning and the end of a string are correct.
        if (!validateBeginningAndEnd(alphabet, ALPHABET)) {
            return false;
        }

        // If parameters are correct.
        if (!validateParameters(alphabet.substring(ALPHABET.length(), alphabet.length() - 1), true, false)) {
            return false;
        }

        return true;
    }

    /**
     * Check if the 3rd string is correct.
     *
     * @return true if it is correct.
     */
    static boolean validateInitialStateString(String initialState) {

        // If the beginning and the end of a string are correct.
        if (!validateBeginningAndEnd(initialState, INITIAL_STATE)) {
            return false;
        }

        // If parameters are correct.
        if (!validateParameters(initialState.substring(INITIAL_STATE.length(), initialState.length() - 1), false, false)) {
            return false;
        }

        return true;
    }

    /**
     * Check if the 4th string is correct.
     *
     * @return true if it is correct.
     */
    static boolean validateFiniteStatesString(String finiteStates) {

        // If the beginning and the end of a string are correct.
        if (!validateBeginningAndEnd(finiteStates, FINITE_STATES)) {
            return false;
        }

        // If parameters are correct.
        if (!validateParameters(finiteStates.substring(FINITE_STATES.length(), finiteStates.length() - 1), false, false)) {
            return false;
        }

        return true;
    }

    /**
     * Check if the 5th string is correct.
     *
     * @return true if it is correct.
     */
    static boolean validateTransitionFunctionsString(String transitionFunction) {

        // If the beginning and the end of a string are correct.
        if (!validateBeginningAndEnd(transitionFunction, TRANSITION_FUNCTION)) {
            return false;
        }

        // If parameters are correct.
        if (!validateParameters(transitionFunction.substring(TRANSITION_FUNCTION.length(), transitionFunction.length() - 1), true, true)) {
            return false;
        }

        return true;
    }

    /**
     * Compares the beginning of a 'string' with a 'sample' string.
     * The ending of a 'string' must end by ']'
     *
     * @param string - string to compare
     * @param sample - string compares with this 'sample'
     * @return true if the beginning and the end of a 'string' are correct.
     */
    static boolean validateBeginningAndEnd(String string, String sample) {
        // If the start of a sting is correct.
        if (!string.substring(0, sample.length()).equals(sample)) {
            return false;
        }

        // If the end of a sting is correct.
        if (string.charAt(string.length() - 1) != ']') {
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
        if (string.length() == 0) {
            return true;
        }
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
            if (!(Character.isDigit(x) || isLatinLetters(x))) {
                if (!(underscore && x == '_')) {
                    if (!(transition && x == '>' && i != 0 && i != parameter.length() - 1)) {
                        return false;
                    }
                }
            }
        }
        return true;
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
