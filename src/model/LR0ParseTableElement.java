package model;

public class LR0ParseTableElement {
    public enum ElementType {
        SHIFT, REDUCE, GOTO
    }

    private final ElementType elementType;
    private int stateNumber;
    private ProductionRule reductionProductionRule;

    public LR0ParseTableElement(ElementType elementType, int stateNumber) {
        this.elementType = elementType;
        this.stateNumber = stateNumber;
    }

    public LR0ParseTableElement(ElementType elementType, ProductionRule productionRule) {
        this.elementType = elementType;
        this.reductionProductionRule = productionRule;
    }

    @Override
    public String toString() {
        if(elementType == ElementType.REDUCE) {
            assert reductionProductionRule != null;
            return "R: " + reductionProductionRule;
        } else if(elementType == ElementType.SHIFT) {
            return "S: " + stateNumber;
        } else if(elementType == ElementType.GOTO) {
            return "GOTO: " + stateNumber;
        }
        return "NULL";
    }
}
