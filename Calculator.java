import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Stack;

public class Calculator {
    HashMap<String,Double> variables = new HashMap<>();
    public Calculator() {
        variables.put("last",0.0);
    }

    public void processExpr(String line) {
        Pair postfixline = infixToPostfix(line);
        if (postfixline.expression.isEmpty() && postfixline.variable.isEmpty()) {
            System.out.println("ERROR");
        }
        else
        {
            Tuple result = compute(postfixline.expression, variables);

            if (result.okay) {
                if (!postfixline.variable.isEmpty()) {
                    variables.put(postfixline.variable, result.variables.get("last"));
                }
                System.out.printf("%.5f", result.variables.get("last"));
                System.out.println();
                variables = result.variables;
            }
            else {
                System.out.println("ERROR");
                variables.put("last",0.0);
            }
        }
    }

    enum Type { rbracket, lbracket, operator, varnum, none};
    private Tuple compute(ArrayList<String> expression, HashMap<String, Double> variables)
    {

        Tuple toReturn = new Tuple();
        toReturn.variables = variables;
        Stack<Double> stack = new Stack<>();
        for (String s:
             expression) {
            if(isOperator(s)) {
                if (stack.size() >= 2) {
                    double tryRes = calculate(s, stack.pop(), stack.pop());
                    if (Double.isFinite(tryRes))
                        stack.push(tryRes);
                    else
                        return new Tuple(false);
                }
                else
                {
                    return new Tuple(false);
                }
            }
            else if (isVariable(s, false)) {
                boolean startminus = false;
                if (s.startsWith("-")) {
                    s = s.replace("-", "");
                    startminus = true;
                }
                if(!toReturn.variables.containsKey(s))
                    toReturn.variables.put(s, 0.0);

                stack.push((startminus) ? -1*toReturn.variables.get(s) : toReturn.variables.get(s));
            }
            else
                stack.push(Double.parseDouble(s));
        }
        if (stack.size() > 1) {
            return new Tuple(false);
        }

        toReturn.variables.put("last",stack.pop());
        return toReturn;
    }

    private Double calculate(String s, Double pop, Double pop1) {
        switch(s) {
            case "+":
                return pop1 + pop;
            case "-":
                return pop1 - pop;
            case "*":
                return pop1 * pop;
            case "/":
                return pop1 / pop;
        }
        return 0.0;
    }


    private Pair infixToPostfix(String line) {
        Type last = Type.none;
        Stack<Character> stack = new Stack<>();
        Pair toReturn = new Pair();
        char[] splitline = line.toCharArray();
        String buffer = "";
        boolean possiblesign = false;
        for (int i = 0; i < splitline.length; i++) {
            char c = splitline[i];
            if (Character.isWhitespace(c)) {
                continue;
            }
            else if ((Character.isDigit(c)  || (checkForNextDigit(i+1, splitline) && (c == '-' || c == '+')) || (c=='0' && checkForNextLetter(i+1, splitline))) && last != Type.varnum) {
                buffer += c;
                if(i+1 < splitline.length) {
                    while (Character.isDigit(splitline[i + 1]) || hexaSymbol(Character.toLowerCase(splitline[i+1])) || splitline[i + 1] == '.' || (possiblesign && (splitline[i + 1] == '-' || splitline[i + 1] == '+')) || splitline[i + 1] == 'x') {
                        buffer += splitline[i + 1];
                        if (Character.toLowerCase(splitline[i + 1]) == 'e' || Character.toLowerCase(splitline[i + 1]) == 'p')
                            possiblesign = true;
                        else
                            possiblesign = false;
                        i++;
                        if (i + 1 >= splitline.length)
                            break;
                    }
                }
                possiblesign = false;
                try
                {
                    Double.parseDouble(buffer);
                }
                catch(NumberFormatException e)
                {
                    return new Pair();
                }
                toReturn.expression.add(buffer);
                buffer = "";
                last = Type.varnum;

            } else if ((Character.isLetter(c)|| (checkForNextLetter(i+1, splitline) && c == '-')) && last != Type.varnum)
            {
                buffer += c;
                if(i+1 < splitline.length)
                {
                    while (Character.isLetter(splitline[i+1])) {
                        buffer += splitline[i + 1];
                        i++;
                        if (i + 1 >= splitline.length)
                            break;
                    }
                }
                toReturn.expression.add(buffer);
                buffer = "";
                last = Type.varnum;
            }
            else if (isOperator(c + "") && (last == Type.varnum || last == Type.rbracket)) {
                if (stack.empty() || precedence(c) > precedence(stack.peek()))
                    stack.push(c);
                else {
                    while (!stack.empty() && precedence(c) <= precedence(stack.peek()))
                    {
                        toReturn.expression.add(stack.pop() + "");
                    }
                    stack.push(c);
                }
                last = Type.operator;
            }
            else if(c == '(' && last != Type.varnum) {
                stack.push(c);
                last = Type.lbracket;
            }
            else if (c == ')' && last != Type.operator) {
                while (!stack.empty() && stack.peek() != '(')
                    toReturn.expression.add(stack.pop() + "");

                if (stack.empty()) {
                    return new Pair();
                }
                stack.pop();

                last = Type.rbracket;
            }
            else if(c == '=' && toReturn.expression.size() == 1 && isVariable(toReturn.expression.get(0), true))
            {
                toReturn.variable = toReturn.expression.get(0);
                toReturn.expression.clear();
                last = Type.none;
            }
            else
            {
                return new Pair();
            }
        }

        while (!stack.empty()) {
            if (stack.peek() == '(')
            {
                return new Pair();
            }
            toReturn.expression.add(stack.pop() + "");
        }

        return toReturn;
    }

    private boolean hexaSymbol(char c) {
        if (c == 'e' || c == 'p' || c =='a' || c== 'b'|| c== 'c'|| c== 'd'|| c== 'f')
            return true;
        else
            return false;
    }

    private boolean checkForNextLetter(int i, char[] splitline) {
        if (i >= splitline.length)
            return false;
        else if (Character.isLetter(splitline[i]))
            return true;
        else
            return false;
    }

    private int precedence(Character peek) {
        switch(peek)
        {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
        }
        return -1;
    }

    private boolean isVariable(String s, boolean assign) {
        char[] splits = s.toCharArray();
        int start = 0;
        if (!assign && splits[0] == '-')
            start = 1;
        for (int i = start; i < splits.length; i++) {
            if (!Character.isLetter(splits[i]))
                return false;
        }
        return true;
    }

    private boolean checkForNextDigit(int i, char[] splitline) {
        if (i >= splitline.length)
            return false;
        else if (Character.isDigit(splitline[i]))
            return true;
        else
            return false;
    }

    private boolean isOperator(String c) {
        switch (c) {
            case "/":
                return true;
            case "+":
                return true;
            case "-":
                return true;
            case "*":
                return true;
            default:
                return false;
        }
    }
}
