package com.softwarearchitecture.testecs.testcomponents;

import java.io.Serializable;

public class MoneyComponent implements Serializable {
    public int amount;

    public MoneyComponent(int amount) {
        this.amount = amount;
    }
}