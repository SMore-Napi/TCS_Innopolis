package final_exam.partB;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Roman Soldatov BS19-02
 */
public class TuringMachineValidator {
    static final String INPUT_FILE = "input.txt";
    static final String OUTPUT_FILE = "output.txt";

    public static void main(String[] args) throws FileNotFoundException {
        // Input from file.
        String input = input(INPUT_FILE);

        if (!checkInput(input)) {
            output("Invalid input", OUTPUT_FILE);
        } else {
            TuringMachine tm = fillTM();

            String result = tm.validate(input);
            output(result, OUTPUT_FILE);
        }
    }

    /**
     * Input expression from file.
     * The file supposed to contain only one line.
     *
     * @param filePath - path to the file.
     * @return - string from input.
     */
    public static String input(String filePath) throws FileNotFoundException {
        String expression;
        Scanner scanner = new Scanner(new File(filePath));

        // In case we have an empty file.
        if (!scanner.hasNext()) {
            expression = "";
        } else {
            expression = scanner.nextLine();
        }

        scanner.close();

        return expression;
    }

    /**
     * Print the string to the file.
     *
     * @param result - string to print
     * @param path   -  path to the file.
     */
    public static void output(String result, String path) throws FileNotFoundException {
        PrintWriter printWriter = new PrintWriter(new File(path));
        printWriter.print(result);
        printWriter.close();
    }

    /**
     * Fill TM from the task.
     */
    public static TuringMachine fillTM() {
        TuringMachine tm = new TuringMachine(6, 0);

        // Transitions for q0
        tm.states.get(0).addTransition("0,Z/Z,<S,R>", 0);
        tm.states.get(0).addTransition("1,Z/Z,<S,R>", 0);
        tm.states.get(0).addTransition("0,_/0,<R,R>", 0);
        tm.states.get(0).addTransition("1,_/1,<R,R>", 0);
        tm.states.get(0).addTransition("#,_/_,<R,L>", 1);

        // Transitions for q1
        tm.states.get(1).addTransition("1,1/1,<R,L>", 1);
        tm.states.get(1).addTransition("0,0/0,<R,L>", 1);
        tm.states.get(1).addTransition("1,0/0,<R,L>", 1);
        tm.states.get(1).addTransition("0,1/1,<R,L>", 1);
        tm.states.get(1).addTransition("_,Z/Z,<L,S>", 2);
        tm.states.get(1).addTransition("_,1/1,<S,S>", 4);
        tm.states.get(1).addTransition("_,0/0,<S,S>", 4);

        // Transitions for q2
        tm.states.get(2).addTransition("0,Z/Z,<L,S>", 2);
        tm.states.get(2).addTransition("1,Z/Z,<L,S>", 2);
        tm.states.get(2).addTransition("#,Z/Z,<R,R>", 3);

        // Transitions for q3
        tm.states.get(3).addTransition("1,1/1,<R,R>", 3);
        tm.states.get(3).addTransition("0,0/0,<R,R>", 3);
        tm.states.get(3).addTransition("0,1/1,<S,S>", 4);

        tm.states.get(4).isFinal = true;

        return tm;
    }

    /**
     * Check if the input is valid.
     */
    public static boolean checkInput(String input) {
        int countSharps = 0;

        if (input.length() == 0) {
            return false;
        }

        // If there is one substring
        if ((input.charAt(0) == '#') || (input.charAt(input.length() - 1) == '#')) {
            return false;
        }

        // Check symbols and count '#'
        for (int i = 0; i < input.length(); i++) {
            if (input.charAt(i) == '#') {
                countSharps++;
            }
            if (input.charAt(i) != '0' && input.charAt(i) != '1' && input.charAt(i) != '#') {
                return false;
            }
        }

        if (countSharps != 1) {
            return false;
        }

        // Check leading zeros.
        String[] strings = input.split("#");
        if ((strings[0].charAt(0) == '0' && strings[0].length() > 1 && strings[0].charAt(1) == '1') || (strings[1].charAt(0) == '0' && strings[1].length() > 1 && strings[1].charAt(1) == '1')) {
            return false;
        }

        return true;
    }
}

class TuringMachine {
    class State {
        String name;
        ArrayList<String> commands;
        ArrayList<Integer> transitions;
        boolean isFinal;

        public State(String name) {
            isFinal = false;
            this.name = name;
            this.commands = new ArrayList<>();
            this.transitions = new ArrayList<>();
        }

        public void addTransition(String command, int state) {
            this.commands.add(command);
            this.transitions.add(state);
        }
    }

    ArrayList<State> states;
    StringBuilder tape;
    int indexTape;
    int indexState;

    public TuringMachine(int numberStates, int initialState) {
        tape = new StringBuilder();
        tape.append('Z');
        indexTape = 0;

        indexState = initialState;
        states = new ArrayList<>(numberStates);
        for (int i = 0; i < numberStates; i++) {
            states.add(new State("q" + i));
        }
    }

    public String validate(String input) {
        StringBuilder result = new StringBuilder();
        int indexInput = 0;
        boolean run = true;
        StringBuilder lastString = new StringBuilder();

        // Do while some command from state can be executed
        while (run) {
            // Output iteration
            String name = states.get(indexState).name;
            StringBuilder inputString = new StringBuilder(input);
            inputString.insert(indexInput, '^');

            StringBuilder tapeString = new StringBuilder(tape.toString());
            tapeString.insert(indexTape, '^');

            // Erase all possible '_' symbols
            int ind = tapeString.length();
            for (int i = 0; i < tapeString.length(); i++) {
                if (tapeString.charAt(i) == '_') {
                    ind = i;
                    break;
                }
            }
            tapeString.delete(ind, tapeString.length());

            lastString = new StringBuilder();
            lastString.append(name).append(", ").append(inputString.toString()).append(", ").append(tapeString.toString()).append("\n");
            result.append(lastString.toString());

            run = false;
            // Check each command from current state
            for (int i = 0; i < states.get(indexState).commands.size(); i++) {
                String command = states.get(indexState).commands.get(i);
                // Separate command by parts.
                char inputSymbol = command.charAt(0);
                char tapeSymbol = command.charAt(2);
                char tapeSymbolToSet = command.charAt(4);
                char inputCommand = command.charAt(7);
                char tapeCommand = command.charAt(9);

                // In case we found suitable command from state.
                // Both conditions satisfy.
                if (compare(inputSymbol, input, indexInput)) {
                    if (compare(tapeSymbol, tape.toString(), indexTape)) {
                        // Execute again next time
                        run = true;

                        // In case we are in the end of string
                        if (indexTape == tape.length()) {
                            tape.append(tapeSymbolToSet);
                        } else {
                            tape.setCharAt(indexTape, tapeSymbolToSet);
                        }

                        // Move input tape.
                        switch (inputCommand) {
                            case 'R':
                                indexInput++;
                                break;
                            case 'L':
                                indexInput--;
                                break;
                        }

                        // Move TM tape
                        switch (tapeCommand) {
                            case 'R':
                                indexTape++;
                                break;
                            case 'L':
                                indexTape--;
                                break;
                        }


                        // Move to the state according to command which has been executed.
                        indexState = states.get(indexState).transitions.get(i);

                        break;
                    }
                }
            }
        }

        if (states.get(indexState).isFinal) {
            result.append("YES\n");
        } else {
            result.append(lastString.toString());
            result.append("NO\n");
        }

        return result.toString();
    }

    /**
     * Method for comparing two symbols.
     * It handels the case when the index is out of bound.
     */
    private boolean compare(char symbol, String string, int index) {
        if (index == string.length()) {
            if (symbol == '_') {
                return true;
            } else {
                return false;
            }
        } else if (symbol == string.charAt(index)) {
            return true;
        } else {
            return false;
        }
    }
}
