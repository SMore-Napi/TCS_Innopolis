package assignment2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Roman Soldatov BS19-02
 * Home Assignment 2
 */
public class FSARegExpTranslator {
    static final String INPUT_FILE = "fsa.txt";
    static final String OUTPUT_FILE = "result.txt";

    public static void main(String[] args) throws FileNotFoundException {
        // Read the input from the file.
        ArrayList<String> input = readFile(INPUT_FILE);

        // Validate the input.
        String result = FSAValidator.checkFSAValidator(input);

        // If the FSA has passed the validation, then we find the Regular Expresion.
        if (result.isEmpty()) {
            result = RegularExpression.transformToRegularExpression(input);
        }

        // Print the result.
        PrintWriter printWriter = new PrintWriter(new File(OUTPUT_FILE));
        printWriter.print(result);
        printWriter.close();
    }

    /**
     * Input Lines from the file
     *
     * @return Array List of strings
     */
    static ArrayList<String> readFile(String inputFile) {
        ArrayList<String> input = new ArrayList<>();
        Scanner scanner;
        try {
            scanner = new Scanner(new File(inputFile));
            while (scanner.hasNext()) {
                input.add(scanner.nextLine());
            }
            scanner.close();
        } // If file has not been found then we do nothing.
        catch (FileNotFoundException e) {
        }

        return input;
    }
}

/**
 * This class transforms the automata to the
 * regular expression which the automata recognise.
 */
class RegularExpression {
    /**
     * The auxiliary class to store the Regular Expression.
     */
    static class RegExp {
        String from; // the start state.
        String to; // the end state.
        String value; // the regular expression itself.

        public RegExp(String from, String to, String value) {
            this.from = from;
            this.to = to;
            this.value = value;
        }
    }

    /**
     * The main method which calculates the regular expression of the given (D)FSA.
     * This method uses the 'Kleene’s Algorithm'.
     *
     * @param input - validated input strings of the (D)FSA.
     * @return the regular expression.
     */
    public static String transformToRegularExpression(ArrayList<String> input) {
        // Get strings of states, initial state, finite states and transition functions.
        ArrayList<String> states = FSAValidator.getStates(input);
        ArrayList<String> initialStates = FSAValidator.getInitialState(input);
        ArrayList<String> finiteStates = FSAValidator.getFiniteStates(input);
        ArrayList<String> transitionFunctions = FSAValidator.getTransitionFunctions(input);

        // Get the regular expressions for each step and each state.
        ArrayList<ArrayList<RegExp>> regularExpressions = getRegularExpressions(states, transitionFunctions);

        /* The regular expression represents the language accepted by (D)FSA
           via union all regular expressions from the initial state to the finite states.
           This expressions are stored in the last ArrayList in the 'regularExpressions'. */
        ArrayList<RegExp> lastStepExpressions = regularExpressions.get(regularExpressions.size() - 1);

        // This list will store the union of all accepted regular expressions.
        ArrayList<RegExp> regex = new ArrayList<>();

        // We are interesting in regular expressions from the last 'Kleene’s Algorithm' step
        // which represents the languages starting from the 'initialState' and ending in the 'finiteState'
        for (String finiteState : finiteStates) {
            for (String initialState : initialStates) {
                for (RegExp expression : lastStepExpressions) {
                    if (expression.from.equals(initialState) && expression.to.equals(finiteState)) {
                        regex.add(expression);
                    }
                }
            }
        }

        // Rewrite the 'regex' to the string.
        StringBuilder result = new StringBuilder();
        // In case there are no any possible regular expressions.
        if (regex.size() == 0) {
            result.append("{}");
        } // Rewriting into the string.
        else {
            result.append(regex.get(0).value);
            for (int i = 1; i < regex.size(); i++) {
                result.append('|').append(regex.get(i).value);
            }
        }

        return result.toString();
    }

    /**
     * This method calculates all regular expressions for each step and each state.
     *
     * @param states              - states of (D)FSA.
     * @param transitionFunctions - transitions of (D)FSA.
     * @return the Array List of regular expressions on each step.
     */
    private static ArrayList<ArrayList<RegExp>> getRegularExpressions(ArrayList<String> states, ArrayList<String> transitionFunctions) {

        // Each element of this array list is the array list of regular expressions on current step.
        ArrayList<ArrayList<RegExp>> expressions = new ArrayList<>(states.size() + 1);
        // The first expressions will be the initial expressions.
        expressions.add(getInitialRegularExpressions(states, transitionFunctions));

        // Calculate regular expressions on each step.
        for (int k = 0; k < states.size(); k++) {
            // The Array List of regular expressions to fill for the current step.
            ArrayList<RegExp> currentExpressions = new ArrayList<>(states.size() * states.size());
            // The Array List of regular expressions which was calculated on the previous step.
            ArrayList<RegExp> previousExpressions = expressions.get(k);

            // Observe all possible state combinations
            String k_state = states.get(k);
            for (int i = 0; i < states.size(); i++) {
                for (int j = 0; j < states.size(); j++) {
                    String i_state = states.get(i);
                    String j_state = states.get(j);

                    // Each regular expression can be calculated using 4 regExp from the previous step.
                    // R_ij = (R_ik) (R_kk)* (R_kj) | (R_ij)
                    String[] regs = new String[4];

                    for (RegExp regExp : previousExpressions) {
                        // In case this regular expression is R_ik
                        if (regExp.from.equals(i_state) && regExp.to.equals(k_state)) {
                            regs[0] = regExp.value;
                        } // In case this regular expression is R_kk
                        if (regExp.from.equals(k_state) && regExp.to.equals(k_state)) {
                            regs[1] = regExp.value;
                        } // In case this regular expression is R_kj
                        if (regExp.from.equals(k_state) && regExp.to.equals(j_state)) {
                            regs[2] = regExp.value;
                        } // In case this regular expression is R_ij
                        if (regExp.from.equals(i_state) && regExp.to.equals(j_state)) {
                            regs[3] = regExp.value;
                        }
                    }

                    // Rewrite the 'regs' as a string.
                    String value = '(' + regs[0] + ')' +
                            '(' + regs[1] + ")*" +
                            '(' + regs[2] + ")" +
                            "|(" + regs[3] + ')';
                    // Add new calculated regular expression.
                    currentExpressions.add(new RegExp(i_state, j_state, value));
                }
            }

            // Add all regular expressions calculated on this 'k' step.
            expressions.add(currentExpressions);
        }

        return expressions;
    }

    /**
     * Calculates the initial regular expressions.
     * It represents the step = -1 from the 'Kleene’s Algorithm'.
     *
     * @param states               - states of (D)FSA.
     * @param transitionFunctions- transitions of (D)FSA.
     * @return the Array List of regular expressions of the step = -1.
     */
    private static ArrayList<RegExp> getInitialRegularExpressions(ArrayList<String> states, ArrayList<String> transitionFunctions) {
        // Initial regular expressions.
        ArrayList<RegExp> expressions = new ArrayList<>(states.size() * states.size());

        // Observe all state combinations.
        for (int i = 0; i < states.size(); i++) {
            for (int j = 0; j < states.size(); j++) {
                String from = states.get(i);
                String to = states.get(j);

                // Fill the regular expression for 'from' and 'to' states.
                ArrayList<String> regExp = new ArrayList<>();

                // If there is a transition which represents
                // the connection between 'from' and 'to' states
                // then we add this transition to the 'reExp'
                for (String transition : transitionFunctions) {
                    String[] x = transition.split(">");
                    if (x[0].equals(from) && x[2].equals(to)) {
                        regExp.add(x[1]);
                    }
                }

                // Add epsilon if 'from' and 'to' are the same state.
                if (i == j) {
                    regExp.add("eps");
                }
                // Add empty set if there are no any possible transitions.
                if (regExp.size() == 0) {
                    regExp.add("{}");
                }

                // Rewrite 'regExp' to the string.
                StringBuilder value = new StringBuilder();
                value.append(regExp.get(0));
                for (int k = 1; k < regExp.size(); k++) {
                    value.append('|').append(regExp.get(k));
                }

                // Add new calculated regular expression.
                expressions.add(new RegExp(from, to, value.toString()));
            }
        }

        return expressions;
    }
}

/**
 * This class validates the Deterministic FSA.
 * It returns the string with validation report.
 * If the input is DFSA, then it returns an empty string.
 */
class FSAValidator {

    static final int COUNT_LINES = 5;

    // The start of input lines.
    static final String STATES = "states=[";
    static final String ALPHABET = "alpha=[";
    static final String INITIAL_STATE = "init.st=[";
    static final String FINITE_STATES = "fin.st=[";
    static final String TRANSITION_FUNCTION = "trans=[";

    static StringBuilder resultToReturn = new StringBuilder();

    /**
     * FSA Validator.
     * Check for:
     * E1, E2, E3, E4, E5 errors;
     * is FSA complete/incomplete;
     * W1, W2, W3 warnings.
     */
    public static String checkFSAValidator(ArrayList<String> input) {

        // Check for E5 error.
        if (checkE5(input)) {
            // Get strings of states, alphabet, initial state, finite states and transition functions.
            ArrayList<String> states = getStates(input);
            ArrayList<String> alphabet = getAlphabet(input);
            ArrayList<String> initialState = getInitialState(input);
            ArrayList<String> finiteStates = getFiniteStates(input);
            ArrayList<String> transitionFunctions = getTransitionFunctions(input);

            // Check for E1 error.
            if (checkE1(states, initialState, finiteStates, transitionFunctions)) {
                // Check for E3 error.
                if (checkE3(alphabet, transitionFunctions)) {
                    // Check for E2 error.
                    if (checkE2(states, transitionFunctions)) {
                        // Check for E4 error.
                        if (checkE4(initialState)) {
                            // Check for E6 error.
                            if (checkE6(states, alphabet, transitionFunctions)) {
                                return resultToReturn.toString();
                            }
                        }
                    }
                }
            }
        }

        return resultToReturn.toString();
    }

    /**
     * Checks for E1 error.
     * If some state from 'initialState', 'finiteStates' or 'transitionFunctions' lists is not defined.
     *
     * @return true if there is no error.
     */
    private static boolean checkE1(ArrayList<String> states, ArrayList<String> initialState, ArrayList<String> finiteStates, ArrayList<String> transitionFunctions) {

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
    private static void printE1(String state) {
        resultToReturn.append("Error:\n");
        resultToReturn.append("E1: A state '").append(state).append("' is not in the set of states\n");
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
    private static boolean checkE2(ArrayList<String> states, ArrayList<String> transitionFunctions) {

        ArrayList<String> jointStates = getJointStates(states, transitionFunctions);

        if (jointStates.size() != states.size()) {
            resultToReturn.append("Error:\n");
            resultToReturn.append("E2: Some states are disjoint\n");
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
    private static ArrayList<String> getJointStates(ArrayList<String> states, ArrayList<String> transitionFunctions) {

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
    private static ArrayList<String>[][] getStatesAlphabetMatrix(ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> transitionFunctions) {

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
    private static boolean[][] getIncidenceMatrix(ArrayList<String> states, ArrayList<String> transitionFunctions) {

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
    private static ArrayList<String> getConnectedStates(int startState, ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> transitionFunctions) {

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
    private static boolean checkE3(ArrayList<String> alphabet, ArrayList<String> transitionFunctions) {
        for (String string : transitionFunctions) {
            String[] alpha = string.split(">");
            if (!alphabet.contains(alpha[1])) {
                resultToReturn.append("Error:\n");
                resultToReturn.append("E3: A transition '").append(alpha[1]).append("' is not represented in the alphabet\n");
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
    private static boolean checkE4(ArrayList<String> initialState) {
        if (initialState == null || initialState.size() != 1) {
            resultToReturn.append("Error:\n");
            resultToReturn.append("E4: Initial state is not defined\n");
            return false;
        }
        return true;
    }

    /**
     * Checks for E5 error.
     *
     * @return true if there is no error.
     */
    private static boolean checkE5(ArrayList<String> input) {
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
            resultToReturn.append("Error:\n");
            resultToReturn.append("E5: Input file is malformed\n");
        }

        return isCorrect;
    }

    /**
     * Check for the Error 6.
     * Is FSA is deterministic.
     * Each cell of a matrix must contain at most one state.
     *
     * @return true if there is no error.
     */
    private static boolean checkE6(ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> transitionFunctions) {

        ArrayList<String>[][] matrix = getStatesAlphabetMatrix(states, alphabet, transitionFunctions);

        for (ArrayList<String>[] row : matrix) {
            for (ArrayList<String> cell : row) {
                if (cell.size() > 1) {
                    resultToReturn.append("Error:\n");
                    resultToReturn.append("E6: FSA is nondeterministic\n");
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Check if the 1st string is correct.
     *
     * @return true if it is correct.
     */
    private static boolean validateStatesString(String states) {

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
    private static boolean validateAlphabetString(String alphabet) {

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
    private static boolean validateInitialStateString(String initialState) {

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
    private static boolean validateFiniteStatesString(String finiteStates) {

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
    private static boolean validateTransitionFunctionsString(String transitionFunction) {

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
    private static boolean validateBeginningAndEnd(String string, String sample) {

        // To compare the beginning of a string we should compare the length of this string with the sample.
        if (string.length() <= sample.length()) {
            return false;
        }

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
    private static boolean validateParameters(String string, boolean underscore, boolean transition) {
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
    private static boolean checkCharacters(String parameter, boolean underscore, boolean transition) {
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

    private static boolean isLatinLetters(char x) {
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
    private static ArrayList<String> getParameters(String string) {
        if (string.length() != 0) {
            return new ArrayList<>(Arrays.asList(string.split(",")));
        }
        return new ArrayList<>();
    }

    /**
     * Method returns set of strings with states from the input.
     *
     * @param input - validated input.
     * @return Array list of strings. Each string represents one state.
     */
    public static ArrayList<String> getStates(ArrayList<String> input) {
        return getParameters(input.get(0).substring(STATES.length(), input.get(0).length() - 1));
    }

    /**
     * Method returns set of strings with alphabet from the input.
     *
     * @param input - validated input.
     * @return Array list of strings. Each string represents one alphabet element.
     */
    public static ArrayList<String> getAlphabet(ArrayList<String> input) {
        return getParameters(input.get(1).substring(ALPHABET.length(), input.get(1).length() - 1));
    }

    /**
     * Method returns set of strings with initial states from the input.
     *
     * @param input - validated input.
     * @return Array list of strings (usually only one string). Each string represents one initial state.
     */
    public static ArrayList<String> getInitialState(ArrayList<String> input) {
        return getParameters(input.get(2).substring(INITIAL_STATE.length(), input.get(2).length() - 1));
    }

    /**
     * Method returns set of strings with finite states from the input.
     *
     * @param input - validated input.
     * @return Array list of strings. Each string represents one finite state.
     */
    public static ArrayList<String> getFiniteStates(ArrayList<String> input) {
        return getParameters(input.get(3).substring(FINITE_STATES.length(), input.get(3).length() - 1));
    }

    /**
     * Method returns set of strings with transitions from the input.
     *
     * @param input - validated input.
     * @return Array list of strings. Each string represents one transition function.
     */
    public static ArrayList<String> getTransitionFunctions(ArrayList<String> input) {
        return getParameters(input.get(4).substring(TRANSITION_FUNCTION.length(), input.get(4).length() - 1));
    }

    /**
     * Check if FSA is complete.
     * Each cell of a matrix must contain at least one state.
     *
     * @return true if FSA is complete, false - otherwise.
     */
    private static boolean isFSAComplete(ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> transitionFunctions) {

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
    private static boolean checkW1(ArrayList<String> finiteStates) {
        return finiteStates.size() >= 1;
    }

    /**
     * Check for the Warning 2
     * If there are no all states in 'connectedStates' list,
     * then it means that not all states are reachable from the initial state.
     *
     * @return true if there is no warning.
     */
    private static boolean checkW2(ArrayList<String> states, ArrayList<String> alphabet, ArrayList<String> initialState, ArrayList<String> transitionFunctions) {
        ArrayList<String> connectedStates = getConnectedStates(states.indexOf(initialState.get(0)), states, alphabet, transitionFunctions);
        return connectedStates.size() == states.size();
    }
}
