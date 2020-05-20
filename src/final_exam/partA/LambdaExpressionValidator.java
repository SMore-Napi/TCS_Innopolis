package final_exam.partA;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * @author Roman Soldatov BS19-02
 */
public class LambdaExpressionValidator {
    static final String INPUT_FILE = "input.txt";
    static final String OUTPUT_FILE = "output.txt";

    public static void main(String[] args) throws FileNotFoundException {
        int numberRedexes = -1;

        // Input from file.
        String expression = input(INPUT_FILE);

        // Calculate result.
        boolean isValid = LambdaExpression.validateExpression(expression);
        if (isValid) {
            numberRedexes = LambdaExpression.getNumberRedexes(expression);
        }

        // Write result.
        String result;
        if (isValid) {
            result = "YES\n" + numberRedexes + '\n';
        } else {
            result = "NO\n";
        }

        // Output the result to file.
        output(result, OUTPUT_FILE);
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
}

class LambdaExpression {

    /**
     * Calculate the number of beta-redexes of given lambda expression.
     * Lambda expression must be valid!
     * To check it use 'validateExpression' method.
     *
     * @param expression - valid lambda expression
     * @return - number of beta-redexes.
     */
    public static int getNumberRedexes(String expression) {
        int numberRedexes = 0;

        // Each redex might happen with (\V.Λ)Λ term
        // In this case such term requires to have a lambda sign '\'.
        // Also, beta-redexes happen according to some variable outside of parenthesis.
        // That's why we should consider '(\' substring.
        for (int i = 0; i < expression.length(); i++) {
            if ((expression.charAt(i) == '(') && (expression.charAt(i + 1) == '\\')) {
                numberRedexes++;
            }
        }

        return numberRedexes;
    }

    /**
     * Validate lambda expression
     * It's BNF: Λ ::= V | (Λ)Λ | \V.Λ
     *
     * @param string - string to validate.
     * @return true if this is a valid expression, false - otherwise.
     */

    public static boolean validateExpression(String string) {
        // In case it is 'V'
        if (isVariable(string)) {
            return true;
        }
        // In case it is (Λ)Λ
        if (isParenthesisTerm(string)) {
            return true;
        }
        // In case it is \V.Λ
        if (isLambdaTerm(string)) {
            return true;
        }

        // In case it is not valid.
        return false;
    }

    /**
     * Check the first lambda term 'V' from BNF:
     * Λ ::= V | (Λ)Λ | \V.Λ
     *
     * @param string - string to validate.
     * @return true if this is a valid variable, false - otherwise.
     */
    private static boolean isVariable(String string) {
        // 'V' must be non-empty.
        if (string.length() == 0) {
            return false;
        }

        // 'V' builds from latin letters and digits.
        for (int i = 0; i < string.length(); i++) {
            char symbol = string.charAt(i);
            if (!(Character.isDigit(symbol) || isLatinLetter(symbol))) {
                return false;
            }
        }

        // In case 'V' is valid.
        return true;
    }

    /**
     * Check the second lambda term '(Λ)Λ' from BNF:
     * Λ ::= V | (Λ)Λ | \V.Λ
     *
     * @param string - string to validate.
     * @return true if this is a valid lambda term with parenthesis, false - otherwise.
     */
    private static boolean isParenthesisTerm(String string) {

        // This term must contain at least two parenthesis.
        if (string.length() == 0) {
            return false;
        }
        if (string.charAt(0) != '(') {
            return false;
        }

        int numberOfOpenParenthesis = 1;
        int indexClosedParenthesis = -1;

        // Find the index of the closed parenthesis ')'
        // which belongs to pair with first open parenthesis '('
        for (int i = 1; i < string.length(); i++) {
            if (string.charAt(i) == '(') {
                numberOfOpenParenthesis++;
            } else if (string.charAt(i) == ')') {
                numberOfOpenParenthesis--;
                if (numberOfOpenParenthesis == 0) {
                    indexClosedParenthesis = i;
                    break;
                }
            }
        }

        // In case there is no closed parenthesis ')' from pair.
        if (indexClosedParenthesis == -1) {
            return false;
        }

        // Validate both terms.
        String firstExpression = string.substring(1, indexClosedParenthesis);
        String secondExpression = string.substring(indexClosedParenthesis + 1);

        if (validateExpression(firstExpression) && validateExpression(secondExpression)) {
            return true;
        }

        // In case one of the term is not valid.
        return false;
    }

    /**
     * Check the third lambda term '\V.Λ' from BNF:
     * Λ ::= V | (Λ)Λ | \V.Λ
     *
     * @param string - string to validate.
     * @return true if this is a valid lambda term with symbol '\', false - otherwise.
     */
    private static boolean isLambdaTerm(String string) {
        // This term must contain '\' and '.' symbols.
        if (string.length() == 0) {
            return false;
        }
        if (string.charAt(0) != '\\') {
            return false;
        }
        if (!string.contains(".")) {
            return false;
        }

        // Separate this term by two terms.
        int indexSeparator = string.indexOf(".");
        String vTerm = string.substring(1, indexSeparator);
        String recursiveTerm = string.substring(indexSeparator + 1);
        if (isVariable(vTerm) && validateExpression(recursiveTerm)) {
            return true;
        }

        // In case the first term is not variable
        // or the second term is not the valid lambda expression.
        return false;
    }

    /**
     * Helper method to determine if the symbol is latin letter.
     *
     * @param x - symbol to check
     * @return true if x - is a latin letter, false - otherwise.
     */
    private static boolean isLatinLetter(char x) {
        String letters = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM";
        for (int i = 0; i < letters.length(); i++) {
            if (x == letters.charAt(i)) {
                return true;
            }
        }
        return false;
    }
}
