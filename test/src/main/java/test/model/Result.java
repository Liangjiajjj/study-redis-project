package test.model;

public class Result {

    public Result(int value) {
        this.value = value;
    }

    private int value;

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void addValue(int add) {
        this.value += add;
    }
}
