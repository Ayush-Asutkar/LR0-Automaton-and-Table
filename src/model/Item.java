package model;

import grammar.Grammar;

import java.util.*;

public class Item {
    public enum ItemType {
        NEW_ITEM, DERIVED_ITEM
    }

    private final static String DOTMARKER = String.valueOf('\u2022');
    private final String leftSide;
    private final List<String> rightSide;

    public Item(String left, List<String> rightSide, ItemType itemType) {
        this.leftSide = left;
        this.rightSide = new ArrayList<>(rightSide);
        if(itemType == ItemType.NEW_ITEM) {
            this.rightSide.add(0, DOTMARKER);
        }
    }

    public boolean isReductionItem() {
        return rightSide.indexOf(DOTMARKER) == (rightSide.size() - 1);
    }

    public String getSymbolNextOfDotMarker() {
        if(isReductionItem()) {
            return null;
        }

        int indexOfDotMarker = this.rightSide.indexOf(DOTMARKER);
        return this.rightSide.get(indexOfDotMarker + 1);
    }

    public Item moveDotMarkerAndReturnItem() {
        if(this.isReductionItem()) {
            System.out.println("Is a reduction item");
            return null;
        }

        List<String> newRightSide = new ArrayList<>(rightSide);

        int indexOfDotMarker = newRightSide.indexOf(DOTMARKER);
        newRightSide.remove(indexOfDotMarker);
        newRightSide.add(indexOfDotMarker + 1, DOTMARKER);
        return new Item(leftSide, newRightSide, ItemType.DERIVED_ITEM);
    }

    public Set<Item> closure(List<ProductionRule> productionRules) {
        Set<Item> closureItem = new HashSet<>();
        closureItem.add(this);

        boolean addedNew = true;
        while(addedNew) {
            addedNew = false;
            Set<Item> toAdd = new HashSet<>();

            for (Item currItem: closureItem) {
                String nextSymbolOfDotMarker = currItem.getSymbolNextOfDotMarker();

                int indexInProductionRule = productionRules.indexOf(new ProductionRule(nextSymbolOfDotMarker));

                //checking if production rule is present
                if(indexInProductionRule < 0) {
                    continue;
                }

                ProductionRule productionRule = productionRules.get(indexInProductionRule);
                String leftSideOfProductionRule = productionRule.getLeftHandSide();
                for(List<String> rightHandSideOfProductionRule: productionRule.getRightHandSide()) {
                    Item itemForParticularProductionRule = new Item(leftSideOfProductionRule, rightHandSideOfProductionRule, ItemType.NEW_ITEM);
                    if (!closureItem.contains(itemForParticularProductionRule)) {
                        addedNew = true;
                        toAdd.add(itemForParticularProductionRule);
                    }
                }
            }

            if (!toAdd.isEmpty()) {
                closureItem.addAll(toAdd);
            }
        }

        return closureItem;
    }

    @Override
    public String toString() {
        return leftSide + " -> " + rightSide;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Item item = (Item) o;
        return Objects.equals(leftSide, item.leftSide) && Objects.equals(rightSide, item.rightSide);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftSide, rightSide);
    }

    //testing
    public static void main(String[] args) {
//        List<String> rightSide = new ArrayList<>();
//        rightSide.add("S");
//        rightSide.add("a");
//        Item item = new Item("S", rightSide, ItemType.NEW_ITEM);
//        System.out.println("item: " + item);
//        System.out.println("item.symbolNextOfDotMarker(): " + item.getSymbolNextOfDotMarker());
//
//        Item newItem = item.moveDotMarkerAndReturnItem();
//        System.out.println("newItem: " + newItem);
//        System.out.println("newItem.symbolNextOfDotMarker(): " + newItem.getSymbolNextOfDotMarker());
//
//        Item newItem1 = newItem.moveDotMarkerAndReturnItem();
//        System.out.println("newItem1: " + newItem1);
//        System.out.println("newItem1.symbolNextOfDotMarker(): " + newItem1.getSymbolNextOfDotMarker());
//
//        Item newItem2 = newItem1.moveDotMarkerAndReturnItem();
//        System.out.println("newItem2: " + newItem2);
//        System.out.println("newItem2.symbolNextOfDotMarker(): " + newItem2.symbolNextOfDotMarker());

        Grammar grammar = new Grammar();
        grammar.addRule("E' -> E");
        grammar.addRule("E -> E + T | T");
        grammar.addRule("T -> T * F | F");
        grammar.addRule("F -> ( E ) | id");
        grammar.printGrammar();

//        List<String> right = new ArrayList<>();
//        right.add("E");
//        Item item = new Item("E'", right, ItemType.NEW_ITEM);
//
//        System.out.println("item: " + item);
//
//        Set<Item> closure =  item.closure(grammar.getProductionRules());
//        closure.forEach(System.out::println);

        List<String> right = new ArrayList<>();
        right.add("(");
        right.add(DOTMARKER);
        right.add("E");
        right.add(")");
        Item item = new Item("F", right, ItemType.DERIVED_ITEM);

        System.out.println("item: " + item);

        Set<Item> closure =  item.closure(grammar.getProductionRules());
        closure.forEach(System.out::println);
    }
}
