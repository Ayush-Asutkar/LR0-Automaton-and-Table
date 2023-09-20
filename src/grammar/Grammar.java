package grammar;

import model.ProductionRule;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static constants.StringConstants.END_OF_LINE_SYMBOL;
import static constants.StringConstants.EPSILON;

public class Grammar {

    private String firstSymbol;
    private final List<ProductionRule> productionRules;

    //store the non-terminal symbols and terminal symbols
    private final Set<String> terminalSymbols;
    private final Set<String> nonTerminalSymbols;

    //First set and follow set of all Non-Terminals
    private final Map<String, Set<String>> firstSet;
    private final Map<String, Set<String>> followSet;

    public Grammar() {
        this.productionRules = new ArrayList<>();
        this.terminalSymbols = new HashSet<>();
        this.terminalSymbols.add(END_OF_LINE_SYMBOL);
        this.nonTerminalSymbols = new HashSet<>();
        this.firstSet = new HashMap<>();
        this.followSet = new HashMap<>();
    }

    public String getFirstSymbol() {
        return firstSymbol;
    }

    public void setFirstSymbol(String firstSymbol) {
        this.firstSymbol = firstSymbol;
    }

    public List<ProductionRule> getProductionRules() {
        return Collections.unmodifiableList(productionRules);
    }
    
    private ProductionRule getProductionRuleBasedOnNonTerminal(String symbol) {
        if(this.productionRules.contains(new ProductionRule(symbol))) {
            int index = this.productionRules.indexOf(new ProductionRule(symbol));
            return this.productionRules.get(index);
        } else {
            return null;
        }
    }
    
    public void addRule (String leftHandSide, Set<List<String>> rightHandSide) {
        ProductionRule alreadyExistingProductionRule = this.getProductionRuleBasedOnNonTerminal(leftHandSide);
        
        if (alreadyExistingProductionRule == null) {
            ProductionRule newProductionRule = new ProductionRule(leftHandSide);
            newProductionRule.addAllRightHandSide(rightHandSide);
            this.productionRules.add(newProductionRule);
        } else {
            alreadyExistingProductionRule.addAllRightHandSide(rightHandSide);
        }
    }

    /**
     * To add the production rule to the grammar
     * @param rule is of format: A -> B | C | D
     */
    public void addRule (String rule) {
//        System.out.println(rule);


        String[] ruleSplit = rule.split("->");
        String leftSide = ruleSplit[0];
        leftSide = leftSide.trim();
//        System.out.println(leftSide);


        String[] rightSide = ruleSplit[1].split("\\|");

//        System.out.println(Arrays.toString(rightSide));

        Set<List<String>> rightFinal = new HashSet<>();

        for (String right: rightSide) {
            right = right.trim();
            String[] symbols = right.split(" ");
            rightFinal.add(new ArrayList<>(List.of(symbols)));
        }
        this.addRule(leftSide, rightFinal);
    }

    public Set<String> getTerminalSymbols() {
        return Collections.unmodifiableSet(terminalSymbols);
    }
    
    public void addTerminalSymbol(String str) {
        this.terminalSymbols.add(str);
    }

    public void addAllTerminalSymbolFromIterator(Iterator<String> iterator) {
        while(iterator.hasNext()) {
            this.addTerminalSymbol(iterator.next());
        }
    }

    public boolean isTerminalSymbol(String str) {
        return this.terminalSymbols.contains(str);
    }

    public Set<String> getNonTerminalSymbols() {
        return Collections.unmodifiableSet(nonTerminalSymbols);
    }
    
    public void addNonTerminalSymbol(String str) {
        this.nonTerminalSymbols.add(str);
    }

    public void addAllNonTerminalSymbolFromIterator(Iterator<String> iterator) {
        while(iterator.hasNext()) {
            this.addNonTerminalSymbol(iterator.next());
        }
    }

    public boolean isNonTerminalSymbol(String str) {
        return this.nonTerminalSymbols.contains(str);
    }
    
    public void addFirstSet(String symbol, String firstSetSymbol) {
        if (this.firstSet.containsKey(symbol)) {
            this.firstSet.get(symbol).add(firstSetSymbol);
        } else {
            Set<String> toAdd = new HashSet<>();
            toAdd.add(firstSetSymbol);
            this.firstSet.put(symbol, toAdd);
        }
    }

    public void addAllFirstSet(String symbol, Set<String> firstSet) {
        for(String first: firstSet) {
            this.addFirstSet(symbol, first);
        }
    }

    public Set<String> getFirstSet(String symbol) {
        if (!this.firstSet.containsKey(symbol)) {
            return null;
        }
        return Collections.unmodifiableSet(this.firstSet.get(symbol));
    }
    
    private void computeFirstSetForAllTerminalSymbols() {
        for (String terminalSymbol: this.terminalSymbols) {
            if(terminalSymbol.equals(END_OF_LINE_SYMBOL)) {
                continue;
            }
            this.addFirstSet(terminalSymbol, terminalSymbol);
        }
    }
    
    private Set<String> computeFirstSetForParticularSymbol(String symbol) {
        if (this.isTerminalSymbol(symbol)) {
            Set<String> result = new HashSet<>();
            result.add(symbol);
            return result;
        }
        
        ProductionRule productionRule = this.getProductionRuleBasedOnNonTerminal(symbol);
        assert productionRule != null;

        Set<String> result = new HashSet<>();
        for (List<String> rightSide: productionRule.getRightHandSide()) {
            if(rightSide.get(0).equals(EPSILON)) {
                result.add(EPSILON);
            } else {
                boolean toAddEpsilon = true;
                for(String symbolForRightSide: rightSide) {
                    Set<String> toAdd = new HashSet<>(this.computeFirstSetForParticularSymbol(symbolForRightSide));
                    if(toAdd.contains(EPSILON)) {
                        toAdd.remove(EPSILON);
                        result.addAll(toAdd);
                    } else {
                        toAddEpsilon = false;
                        result.addAll(toAdd);
                        break;
                    }
                }
                
                if(toAddEpsilon) {
                    result.add(EPSILON);
                }
            }
        }
        
        return result;
    }
    
    private void computeFirstSetForAllNonTerminalSymbols() {
        for (String nonTerminal: this.nonTerminalSymbols) {
            Set<String> toAdd = this.computeFirstSetForParticularSymbol(nonTerminal);
            this.addAllFirstSet(nonTerminal, toAdd);
        }
    }
    
    public void computeFirstSetForAllSymbols() {
        this.computeFirstSetForAllTerminalSymbols();
        
        this.computeFirstSetForAllNonTerminalSymbols();
    }

    public void addFollowSet(String symbol, String followSetSymbol) {
        if (this.followSet.containsKey(symbol)) {
            this.followSet.get(symbol).add(followSetSymbol);
        } else {
            Set<String> toAdd = new HashSet<>();
            toAdd.add(followSetSymbol);
            this.followSet.put(symbol, toAdd);
        }
    }

    public void addAllFollowSet(String nonTerminal, Set<String> followSet) {
        for(String follow: followSet) {
            this.addFollowSet(nonTerminal, follow);
        }
    }

    public Set<String> getFollowSet(String symbol) {
        if (!this.followSet.containsKey(symbol)) {
            return null;
        }

        return Collections.unmodifiableSet(this.followSet.get(symbol));
    }

    private Set<String> computeFollowSetForNonTerminalSymbol(String nonTerminal) {
        Set<String> result = new HashSet<>();

        if(nonTerminal.equals(this.firstSymbol)) {
            result.add(END_OF_LINE_SYMBOL);
        }

        for(ProductionRule productionRule: this.productionRules) {
            String leftHandSide = productionRule.getLeftHandSide();

            for(List<String> right: productionRule.getRightHandSide()) {
                for(int i=0; i<right.size(); i++) {
                    String symbol = right.get(i);

                    if(symbol.equals(nonTerminal)) {
                        if(i == (right.size() - 1)) {
                            if(leftHandSide.equals(nonTerminal)) {
                                continue;
                            } else {
                                result.addAll(this.computeFollowSetForNonTerminalSymbol(leftHandSide));
                            }
                        } else {
                            Set<String> firstOfNext = new HashSet<>(this.getFirstSet(right.get(i+1)));
                            if(firstOfNext.contains(EPSILON)) {
                                firstOfNext.remove(EPSILON);
                                result.addAll(firstOfNext);
                                result.addAll(this.computeFollowSetForNonTerminalSymbol(leftHandSide));
                            } else {
                                result.addAll(firstOfNext);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    public void computeFollowSetForAllSymbols() {
        for(String nonTerminal: this.nonTerminalSymbols) {
            Set<String> toAdd = computeFollowSetForNonTerminalSymbol(nonTerminal);
            this.addAllFollowSet(nonTerminal, toAdd);
        }
    }

    public void computeFirstAndFollowForAllSymbols() {
        this.computeFirstSetForAllSymbols();
        this.computeFollowSetForAllSymbols();
    }

    /**
     * Prints the production rules of the grammar and the information about terminal symbol and non terminal symbol
     */
    public void printGrammar() {
        System.out.println("Following is the set of terminal symbols: " + this.terminalSymbols.toString());
        System.out.println("Following is the set of non-terminal symbols: " + this.nonTerminalSymbols.toString());
        System.out.println("Following are the rules in the given grammar:");
        for (ProductionRule productionRule: this.productionRules) {
            System.out.println(productionRule);
        }
        System.out.println();
    }

    public void printGrammarToFile(String pathToDirectory, String note) throws IOException {
        String pathToFile = pathToDirectory + "\\" + note.replace(" ", "") + ".txt";
//        System.out.println(pathToFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(pathToFile));
        writer.write(note + "\n");
        writer.write("Following is the set of terminal symbols:" + this.terminalSymbols.toString() + "\n");
        writer.write("Following is the set of non-terminal symbols: " + this.nonTerminalSymbols.toString() + "\n");
        writer.write("Following are the rules in the given grammar: \n");
        for (ProductionRule productionRule: this.productionRules) {
            writer.write(productionRule.toString() + "\n");
        }
        writer.close();
    }

    public void printFirstAndFollowSet() {
        System.out.println("Following is the first set:");
        for(Map.Entry<String, Set<String>> elem: firstSet.entrySet()) {
            System.out.println(elem.getKey() + " => " + elem.getValue());
        }

        System.out.println("Following is the follow set:");
        for(Map.Entry<String, Set<String>> elem: followSet.entrySet()) {
            System.out.println(elem.getKey() + " => " + elem.getValue());
        }
    }

    public void printFirstAndFollowSetToFile(String pathToDirectory, String note) throws IOException {
        String pathToFile = pathToDirectory + "\\" + note.replace(" ", "") + ".txt";
//        System.out.println(pathToFile);
        BufferedWriter writer = new BufferedWriter(new FileWriter(pathToFile));
        writer.write("Following is the first set:\n");
        for(Map.Entry<String, Set<String>> elem: firstSet.entrySet()) {
            writer.write(elem.getKey() + " => " + elem.getValue() + "\n");
        }

        writer.write("\nFollowing is the follow set:\n");
        for(Map.Entry<String, Set<String>> elem: followSet.entrySet()) {
            writer.write(elem.getKey() + " => " + elem.getValue() + "\n");
        }
        writer.close();
    }

    protected String findNewName(String leftHandSide) {
        String newName = leftHandSide + "'";
        boolean notUnique = true;
        while (notUnique) {
            notUnique = false;

            for(ProductionRule rule: this.productionRules) {
                if (rule.equals(new ProductionRule(newName))) {
                    newName += "'";
                    notUnique = true;
                }
            }
        }

        return newName;
    }

    private void solveNonImmediateLR(ProductionRule first, ProductionRule second) {
        String secondLeftHandSide = second.getLeftHandSide();

        Set<List<String>> newRightHandSideOfFirst = new HashSet<>();

        for (List<String> firstRightHandSide: first.getRightHandSide()) {
            if (firstRightHandSide.get(0).equals(secondLeftHandSide)) {
                for (List<String> secondRightHandSide: second.getRightHandSide()) {

                    List<String> newCurrFirstRule = new ArrayList<>(secondRightHandSide);
                    List<String> remainingOfFirst = new ArrayList<>(firstRightHandSide);

                    remainingOfFirst.remove(0);
                    newCurrFirstRule.addAll(remainingOfFirst);

                    newRightHandSideOfFirst.add(newCurrFirstRule);
                }
            } else {
                newRightHandSideOfFirst.add(firstRightHandSide);
            }
        }
        first.setNewRightHandSide(newRightHandSideOfFirst);
    }

    private void solveImmediateLR(ProductionRule first) {
        String leftHandSide = first.getLeftHandSide();
        String newName = this.findNewName(leftHandSide);

        Set<List<String>> leftRecursiveOne = new HashSet<>();
        Set<List<String>> nonLeftRecursiveOne = new HashSet<>();

        // Check if there is left recursion
        for (List<String> rule: first.getRightHandSide()) {
            if (rule.get(0).equals(leftHandSide)) {
                List<String> newRule = new ArrayList<>(rule);
                newRule.remove(0);
                leftRecursiveOne.add(newRule);
            } else {
                nonLeftRecursiveOne.add(rule);
            }
        }

        // if no left recursion exists
        if (leftRecursiveOne.isEmpty()) {
            return;
        }

        // add the new name to Non-Terminal Symbols
        this.addNonTerminalSymbol(newName);

        Set<List<String>> changeRuleForFirst = new HashSet<>();
        Set<List<String>> newRuleForNewName = new HashSet<>();

        if (nonLeftRecursiveOne.isEmpty()) {
            List<String> whenEmpty = new ArrayList<>();
            whenEmpty.add(newName);
            changeRuleForFirst.add(whenEmpty);
        }

        for (List<String> beta: nonLeftRecursiveOne) {
            List<String> forNonRecursive = new ArrayList<>(beta);
            forNonRecursive.add(newName);
            changeRuleForFirst.add(forNonRecursive);
        }

        for (List<String> alpha: leftRecursiveOne) {
            List<String> forRecursive = new ArrayList<>(alpha);
            forRecursive.add(newName);
            newRuleForNewName.add(forRecursive);
        }

        //Amend the original rule
        first.setNewRightHandSide(changeRuleForFirst);

        List<String> forEpsilon = new ArrayList<>();
        forEpsilon.add(EPSILON);
        //add new production rule
        newRuleForNewName.add(forEpsilon);

        ProductionRule newProductionRule = new ProductionRule(newName);
        newProductionRule.setNewRightHandSide(newRuleForNewName);
        this.productionRules.add(newProductionRule);
    }

    public void applyAlgorithmForRemovalOfLeftRecursion() {
        int size = productionRules.size();

        for (int i=0; i<size; i++) {
            for (int j=0; j<i; j++) {
                this.solveNonImmediateLR(productionRules.get(i), productionRules.get(j));
            }
            solveImmediateLR(productionRules.get(i));
        }
    }

    private int findCommonPrefixForTwoListOfString(List<String> first, List<String> second) {
        if(first.isEmpty()  ||  second.isEmpty()  ||  !(first.get(0).equals(second.get(0)))) {
            return -1;
        }

        List<String> small = first;
        List<String> large = second;
        if (small.size() > large.size()) {
            small = second;
            large = first;
        }

        int index = 0;
        for (String largeString: large) {
            if (index == small.size()) {
                break;
            }
            if (!largeString.equals(small.get(index))) {
                break;
            }
            index++;
        }

        //for 0-based indexing
        index--;

        return index;
    }

    private List<String> findStringWhichIsLongestCommonPrefixForArray(List<List<String>> rightHandSide) {
        int indexWithCommonPref = -1;
        int outerCommonPrefixIndex = Integer.MAX_VALUE;

        for (int i=0; i<rightHandSide.size(); i++) {
            int commonPrefixIndex = Integer.MAX_VALUE;
            for (int j=i+1; j<rightHandSide.size(); j++) {
//                System.out.println("i -> " + rightHandSide.get(i));
//                System.out.println("j -> " + rightHandSide.get(j));

                // Check if this two has a common prefix
                int currCommonPrefixIndex = findCommonPrefixForTwoListOfString(rightHandSide.get(i), rightHandSide.get(j));
//                System.out.println(currCommonPrefixIndex);
//                System.out.println(currCommonPrefixIndex + " : " + rightHandSide.get(i).subList(0, currCommonPrefixIndex+1));

                if (currCommonPrefixIndex >= 0) {
                    commonPrefixIndex = Math.min(currCommonPrefixIndex, commonPrefixIndex);
                }
            }
//            System.out.println("Checking for : " + rightHandSide.get(i));
            if (commonPrefixIndex == Integer.MAX_VALUE) {
                continue;
            }
//            System.out.println("commonPrefixIndex = " + commonPrefixIndex + " :-> " + rightHandSide.get(i).substring(0, commonPrefixIndex));

            if (outerCommonPrefixIndex > commonPrefixIndex) {
                outerCommonPrefixIndex = commonPrefixIndex;
                indexWithCommonPref = i;
//                System.out.println("outerCommonPrefixIndex = " + outerCommonPrefixIndex);
            }
        }

        if (indexWithCommonPref == -1) {
            // no common prefix for any string
//            System.out.println("No common prefix");
            return null;
        }
//        System.out.println("indexWithCommonPref = " + indexWithCommonPref + ", and the string corresponding to it is: " + rightHandSide.get(indexWithCommonPref));
//        System.out.println("String = " + rightHandSide.get(indexWithCommonPref).substring(0, outerCommonPrefixIndex));

        List<String> result = new ArrayList<>();
        for (int i=0; i<=outerCommonPrefixIndex; i++) {
            result.add(rightHandSide.get(indexWithCommonPref).get(i));
        }
        return result;
//        return rightHandSide.get(indexWithCommonPref).substring(0, outerCommonPrefixIndex);
//        return null;
    }

    private boolean checkRuleStartsWithCommonPrefix(List<String> rule, List<String> longestCommonPrefix) {
        for(int i=0; i<longestCommonPrefix.size(); i++) {
            if(rule.size() == i) {
                // rule was shorter
                return false;
            }

            if (!rule.get(i).equals(longestCommonPrefix.get(i))) {
                return false;
            }
        }

        return true;
    }

    private boolean applyAlgorithmForProducingAnEquivalentLeftFactoredOnParticularRule(ProductionRule productionRule){
//        System.out.println("To apply rule on: " + productionRule);

        List<String> longestCommonPrefix = this.findStringWhichIsLongestCommonPrefixForArray(new ArrayList<>(productionRule.getRightHandSide()));
        if(longestCommonPrefix == null) {
//            System.out.println("No common prefix");
            return false;
        }
//        System.out.println("longestCommonPrefix = " + longestCommonPrefix);

        String leftHandSide = productionRule.getLeftHandSide();
        String newName = findNewName(leftHandSide);

        // add the new name to set of non-terminal symbols
        this.addNonTerminalSymbol(newName);

        Set<List<String>> amendRules = new HashSet<>();
        Set<List<String>> newRulesForNewName = new HashSet<>();

        for (List<String> rule: productionRule.getRightHandSide()) {
            if (this.checkRuleStartsWithCommonPrefix(rule, longestCommonPrefix)) {
                if (rule.size() == longestCommonPrefix.size()) {
                    List<String> forEpsilon = new ArrayList<>();
                    forEpsilon.add(EPSILON);
                    newRulesForNewName.add(forEpsilon);
                } else {
                    List<String> toAdd = new ArrayList<>();
                    for (int i=longestCommonPrefix.size(); i <rule.size(); i++) {
                        toAdd.add(rule.get(i));
                    }
                    newRulesForNewName.add(toAdd);
                }
            } else {
                amendRules.add(rule);
            }
        }

        List<String> forNewName = new ArrayList<>();
        forNewName.addAll(longestCommonPrefix);
        forNewName.add(newName);
        amendRules.add(forNewName);
//
////        System.out.println("newRulesForNewName = " + newRulesForNewName);
////        System.out.println("amendRules = " + amendRules);
//
        //amend the rules
        productionRule.setNewRightHandSide(amendRules);

        ProductionRule newProductionRule = new ProductionRule(newName);
        newProductionRule.addAllRightHandSide(newRulesForNewName);
        //ConcurrentModificationException
//        this.productionRules.add(newProductionRule);
        this.toStoreNewRules.add(newProductionRule);
        return true;
    }

    private final List<ProductionRule> toStoreNewRules = new ArrayList<>();

    public void applyAlgorithmForProducingAnEquivalentLeftFactored() {
        boolean value = true;

        //applying the algorithm continuously
        while(value) {
            value = false;
            this.toStoreNewRules.clear();

            for (ProductionRule productionRule : this.productionRules) {
                boolean check = applyAlgorithmForProducingAnEquivalentLeftFactoredOnParticularRule(productionRule);
                value = value | check;
            }
            this.productionRules.addAll(toStoreNewRules);
        }
    }

    // for testing
    public static void main(String[] args) {
        Grammar grammar = new Grammar();
//        grammar.addRule("A -> aAB | aBc | aAc");
//        grammar.addRule("E -> b");
//        grammar.addTerminalSymbol("a");
//        grammar.addTerminalSymbol("b");
//        grammar.addNonTerminalSymbol("S");
//        grammar.addRule("S -> S a | S b | c | d");

//        grammar.addRule("A -> B a | A a | c");
//        grammar.addRule("B -> B b | A b | d");

//        grammar.addRule("X -> X S b | S a | b");
//        grammar.addRule("S -> S b | X a | a");

//        grammar.addRule("S -> A a | b");
//        grammar.addRule("A -> A c | A a d | b d | ε");
//        grammar.printGrammar();

//        System.out.println("After removal of left recursion");
//        grammar.applyAlgorithmForRemovalOfLeftRecursion();
//        grammar.printGrammar();

//        grammar.addRule("S -> b S S a a S | b S S a S b | b S b | a");
//        grammar.addRule("A -> a A B | a B c | a A c");
//        grammar.addRule("S -> a S S b S | a S a S b | a b b | b");
//        grammar.addRule("S -> a | a b | a b c | a b c d");

//        grammar.addRule("S -> a A d | a B");
//        grammar.addRule("A -> a | a b");
//        grammar.addRule("B -> c c d | d d c");
//        grammar.printGrammar();
//
//        System.out.println("After finding equivalent left factored grammar");
//        grammar.applyAlgorithmForProducingAnEquivalentLeftFactored();
//        grammar.printGrammar();


//        grammar.setFirstSymbol("E");
//
//        grammar.addTerminalSymbol("(");
//        grammar.addTerminalSymbol(")");
//        grammar.addTerminalSymbol("+");
//        grammar.addTerminalSymbol("*");
//        grammar.addTerminalSymbol("id");
//
//        grammar.addNonTerminalSymbol("E");
//        grammar.addNonTerminalSymbol("E'");
//        grammar.addNonTerminalSymbol("T");
//        grammar.addNonTerminalSymbol("T'");
//        grammar.addNonTerminalSymbol("F");
//
//        grammar.addRule("E -> T E'");
//        grammar.addRule("E' -> + T E' | ε");
//        grammar.addRule("T -> F T'");
//        grammar.addRule("T' -> * F T' | ε");
//        grammar.addRule("F -> ( E ) | id");

//        grammar.setFirstSymbol("S");
//        grammar.addNonTerminalSymbol("S");
//        grammar.addNonTerminalSymbol("A");
//        grammar.addTerminalSymbol("a");
//        grammar.addRule("S -> A | a");
//        grammar.addRule("A -> a");
//        grammar.printGrammar();

//        grammar.setFirstSymbol("S");
//        grammar.addNonTerminalSymbol("S");
//        grammar.addNonTerminalSymbol("L");
//        grammar.addNonTerminalSymbol("L'");
//        grammar.addTerminalSymbol("(");
//        grammar.addTerminalSymbol(")");
//        grammar.addTerminalSymbol("a");
//        grammar.addRule("S -> ( L ) | a");
//        grammar.addRule("L -> S L'");
//        grammar.addRule("L' -> ) S L' | ε");

//        grammar.setFirstSymbol("S");
//        grammar.addNonTerminalSymbol("S");
//        grammar.addTerminalSymbol("a");
//        grammar.addTerminalSymbol("+");
//        grammar.addTerminalSymbol("*");
//        grammar.addRule("S -> S S + | S S * | a");

        grammar.addRule("A -> B a | A a | c");
        grammar.addRule("B -> B b | A b | d");

        grammar.printGrammar();

        grammar.applyAlgorithmForRemovalOfLeftRecursion();

        grammar.printGrammar();

//        grammar.computeFirstAndFollowForAllSymbols();

//        grammar.printFirstAndFollowSet();
    }
}
