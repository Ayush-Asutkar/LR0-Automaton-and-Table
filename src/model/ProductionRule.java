package model;

import java.util.*;

public class ProductionRule {
    private final String leftHandSide;
    private final Set<List<String>> rightHandSide;

    public ProductionRule(String leftHandSide) {
        this.leftHandSide = leftHandSide;
        this.rightHandSide = new HashSet<>();
    }

    public String getLeftHandSide() {
        return leftHandSide;
    }

    public Set<List<String>> getRightHandSide() {
        return Collections.unmodifiableSet(this.rightHandSide);
    }

    public void setNewRightHandSide(Set<List<String>> rightHandSide) {
        this.rightHandSide.clear();
        this.addAllRightHandSide(rightHandSide);
    }

    public void addRightHandSide (List<String> rightHandSide) {
        this.rightHandSide.add(new ArrayList<>(rightHandSide));
    }

    public void addAllRightHandSide (Set<List<String>> rightHandSideAll) {
        for (List<String> elem : rightHandSideAll) {
            this.addRightHandSide(elem);
        }
    }

    public void printProductionRule () {
        System.out.print(this.leftHandSide + " -> ");
        StringBuilder stringBuilder = new StringBuilder();
        for (List<String> right: this.rightHandSide) {
            stringBuilder.append(" ").append(right).append(" |");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        System.out.println(stringBuilder);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProductionRule that = (ProductionRule) o;
        return Objects.equals(leftHandSide, that.leftHandSide);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftHandSide);
    }

    @Override
    public String toString() {
//        return "ProductionRule {" +
//                "leftHandSide='" + leftHandSide + '\'' +
//                ", rightHandSide=" + rightHandSide +
//                '}';
//        return "ProductionRule: " + leftHandSide + " -> " + rightHandSide;
        return leftHandSide + " -> " + rightHandSide;
    }

    // for testing
    public static void main(String[] args) {
        ProductionRule productionRule = new ProductionRule("S");
        List<String> right = new ArrayList<>();
        right.add("S");
        right.add("a");
        productionRule.addRightHandSide(right);

        right.clear();
        right.add("S");
        right.add("b");
        productionRule.addRightHandSide(right);

        right.clear();
        right.add("c");
        productionRule.addRightHandSide(right);

        right.clear();
        right.add("d");
        productionRule.addRightHandSide(right);

        productionRule.printProductionRule();
    }
}
