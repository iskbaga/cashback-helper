package ru.bagautdinov;

public class Transaction {
    final String cardName;
    final String bankName;
    final String category;
    final double value;

    Transaction(String cardName, String bankName, String category, double value) {
        this.cardName = cardName;
        this.bankName = bankName;
        this.category = category;
        this.value = value;
    }
}
