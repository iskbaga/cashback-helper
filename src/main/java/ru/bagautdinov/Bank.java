package ru.bagautdinov;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

class Bank {
    private final String name;
    private final double limit;
    private double cashbackEarned;
    private final Set<String> cards = new HashSet<>();
    private final Map<String, Map<String, Cashback>> currentCashback = new HashMap<>();
    private final Map<String, Map<String, Cashback>> futureCashback = new HashMap<>();

    Bank(String name, double limit) {
        this.name = name;
        this.limit = limit;
        this.cashbackEarned = 0;
    }

    public String getName() {
        return name;
    }

    public double getLimit() {
        return limit;
    }

    public double getCashbackEarned() {
        return cashbackEarned;
    }

    public void addCashback(double amount) {
        this.cashbackEarned += amount;
    }

    public Set<String> getCards() {
        return cards;
    }

    public boolean contains(String cardName) {
        return cards.contains(cardName);
    }

    public void addCard(String cardName) {
        cards.add(cardName);
    }

    public Map<String, Map<String, Cashback>> getCurrentCashback() {
        return currentCashback;
    }

    public Map<String, Map<String, Cashback>> getFutureCashback() {
        return futureCashback;
    }
}