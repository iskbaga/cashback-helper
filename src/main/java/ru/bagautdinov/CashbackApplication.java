package ru.bagautdinov;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CashbackApplication {
    private static final String DATA_FILE = "cashback_data.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    protected static final Data data = loadData();

    public static void main(String[] args) {
        System.out.println(data.banks);
        // Пример использования программы
        addBank("Тинькофф", 10);
        addCard("Тинькофф", "Тинькофф МИР");
        addCurrentCashback("Тинькофф МИР", "Рестораны", 5, false);
        addCurrentCashback("Тинькофф МИР", "Дом и Ремонт", 5, false);
        addCurrentCashback("Тинькофф МИР", "Остальное", 1, true);

        addBank("Банк Санкт-Петербург", 3000);
        addCard("Банк Санкт-Петербург", "Банк Санкт-Петербург");
        addCurrentCashback("Банк Санкт-Петербург", "ЖД билеты", 7, true);
        addCurrentCashback("Банк Санкт-Петербург", "Остальное", 1.5, true);
        addBank("Банк Уфа", 3000);

        cardList();
        chooseCard("Рестораны", 1500);
        addTransaction("Тинькофф МИР", "Рестораны", 1500);
        chooseCard("Рестораны", 1500);
        estimateCashback();
        saveData();
    }

    protected static Data loadData() {
        try (FileReader reader = new FileReader(DATA_FILE)) {
            Type type = new TypeToken<Data>() {
            }.getType();
            return gson.fromJson(reader, type);
        } catch (IOException e) {
            return new Data();
        }
    }

    protected static void saveData() {
        try (FileWriter writer = new FileWriter(DATA_FILE)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected static void addBank(String name, Integer limit) {
        if (!data.banks.containsKey(name)) {
            data.banks.put(name, new Bank(name, limit));
            saveData();
            System.out.println("Банк " + name + " добавлен.");
        } else {
            System.out.println("Банк " + name + " уже существует.");
        }
    }

    protected static void addCard(String bankName, String cardName) {
        Bank bank = data.banks.get(bankName);
        if (bank != null) {
            if (!bank.contains(cardName)) {
                bank.addCard(cardName);
                saveData();
                System.out.println("Карта " + cardName + " добавлена в банк " + bankName + ".");
            } else {
                System.out.println("Карта " + cardName + " уже существует в банке " + bankName + ".");
            }
        } else {
            System.out.println("Банк " + bankName + " не найден.");
        }
    }

    protected static void addCurrentCashback(String cardName, String category, double percent, boolean permanent) {
        for (Bank bank : data.banks.values()) {
            if (bank.contains(cardName)) {
                bank.getCurrentCashback().computeIfAbsent(cardName, k -> new HashMap<>()).put(category, new Cashback(percent, permanent));
                saveData();
                System.out.println("Кешбек для категории " + category + " на " + percent + "% добавлен для карты " + cardName + ".");
                return;
            }
        }
        System.out.println("Карта " + cardName + " не найдена.");
    }

    protected static void removeCashback(String cardName, String category, boolean future) {
        for (Bank bank : data.banks.values()) {
            if (bank.contains(cardName)) {
                Map<String, Map<String, Cashback>> cashbackMap = future ? bank.getFutureCashback() : bank.getCurrentCashback();
                if (cashbackMap.get(cardName).remove(category) != null) {
                    saveData();
                    System.out.println("Кешбек для категории " + category + " удален для карты " + cardName + ".");
                    return;
                }
            }
        }
        System.out.println("Карта " + cardName + " или категория " + category + " не найдены.");
    }

    protected static void cardList() {
        if (!data.banks.isEmpty()) {
            System.out.println("Список карт:");
            for (Bank bank : data.banks.values()) {
                for (String card : bank.getCards()) {
                    System.out.println(card + " (" + bank.getName() + ")");
                }
            }
        } else {
            System.out.println("Нет доступных карт.");
        }
    }

    protected static void addTransaction(String cardName, String category, double value) {
        for (Map.Entry<String, Bank> entry : data.banks.entrySet()) {
            Bank bank = entry.getValue();
            if (bank.contains(cardName)) {
                Map<String, Cashback> cashbackCategories = bank.getCurrentCashback().get(cardName);
                if (cashbackCategories != null && cashbackCategories.containsKey(category)) {
                    Cashback cashback = cashbackCategories.get(category);
                    double cashbackAmount = value * cashback.percent / 100;
                    if (bank.getCashbackEarned() + cashbackAmount <= bank.getLimit()) {
                        bank.addCashback(cashbackAmount);
                        data.transactions.add(new Transaction(cardName, bank.getName(), category, value));
                        saveData();
                        System.out.println("Транзакция добавлена: " + cardName + " - " + category + " - " + value + "р");
                    } else {
                        System.out.println("Превышен лимит кешбека для банка " + bank.getName());
                    }
                } else {
                    System.out.println("Категория " + category + " не найдена для карты " + cardName);
                }
                return;
            }
        }
        System.out.println("Карта " + cardName + " не найдена.");
    }

    protected static String chooseCard(String category, double value) {
        String bestCard = null;
        double bestCashback = 0;
        for (Bank bank : data.banks.values()) {
            for (String cardName : bank.getCards()) {
                double cashback = bank.getCurrentCashback().getOrDefault(cardName, new HashMap<>())
                        .getOrDefault(category, new Cashback(0, false)).percent;
                if (cashback == 0) {
                    cashback = bank.getCurrentCashback().getOrDefault(cardName, new HashMap<>())
                            .getOrDefault("Остальное", new Cashback(0, false)).percent;
                }
                System.out.println(bank.getName() + " " + bank.getCashbackEarned());
                if (cashback > bestCashback && bank.getCashbackEarned() + value * cashback / 100 <= bank.getLimit()) {
                    bestCashback = cashback;
                    bestCard = cardName;
                }
            }
        }
        if (bestCard != null) {
            System.out.println("Лучше всего использовать карту: " + bestCard);
        } else {
            System.out.println("Нет подходящей карты для этой категории.");
        }
        return bestCard;
    }

    protected static void estimateCashback() {
        Map<String, Double> bankCashbacks = new HashMap<>();
        for (Transaction transaction : data.transactions) {
            Bank bank = data.banks.get(transaction.bankName);
            if (bank != null) {
                Map<String, Cashback> cashbacks = bank.getCurrentCashback().get(transaction.cardName);
                if (cashbacks != null) {
                    Cashback cashback = cashbacks.getOrDefault(transaction.category, new Cashback(0, false));
                    if (cashback.percent == 0) {
                        cashback = cashbacks.getOrDefault("Остальное", new Cashback(0, false));
                    }
                    bankCashbacks.merge(transaction.bankName, transaction.value * cashback.percent / 100, Double::sum);
                }
            }
        }
        System.out.println("Оценка кешбека:");
        for (Map.Entry<String, Double> entry : bankCashbacks.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue() + "р");
        }
    }

    static class Data {
        Map<String, Bank> banks = new HashMap<>();
        List<Transaction> transactions = new ArrayList<>();
    }
}
