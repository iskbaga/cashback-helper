package ru.bagautdinov;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static ru.bagautdinov.CashbackApplication.chooseCard;
import static ru.bagautdinov.CashbackApplication.estimateCashback;

public class CashbackApplicationTest {
    private CashbackApplication.Data testData;

    @BeforeEach
    void setUp() {
        testData = CashbackApplication.data;
        testData.banks.put("TestBank", new Bank("TestBank", 1000));
        CashbackApplication.saveData();
    }

    @Test
    @Order(1)
    void testAddBank() {
        CashbackApplication.addBank("TestBank", 1000);
        assertNotNull(testData.banks.get("TestBank"), "Банк должен быть добавлен");
    }

    @Test
    @Order(2)
    void testAddCard() {
        CashbackApplication.addBank("TestBank", 1000);
        CashbackApplication.addCard("TestBank", "TestCard");
        assertTrue(testData.banks.get("TestBank").contains("TestCard"), "У банка должна быть эта карта");
    }

    @Test
    @Order(3)
    void testChooseCard() {
        CashbackApplication.addBank("TestBank", 1000);
        CashbackApplication.addCard("TestBank", "TestCard");
        CashbackApplication.addCurrentCashback("TestCard", "Рестораны", 5, false);
        String bestCard = chooseCard("Рестораны", 500);
        assertNotNull(bestCard, "Лучшая карта банка должна существовать");
    }

    @Test
    @Order(4)
    void testEstimateCashback() {
        CashbackApplication.addBank("TestBank", 0);
        CashbackApplication.addCard("TestBank", "TestCard");
        estimateCashback();
        assertEquals("Альфа Кредитка", chooseCard("Рестораны", 10000), "Карта должна быть выбрана");
    }

    @Test
    @Order(5)
    void testAddTransaction() {
        CashbackApplication.addBank("TestBank", 1000);
        CashbackApplication.addCard("TestBank", "TestCard");
        CashbackApplication.addCurrentCashback("TestCard", "TestCategory", 5, false);
        CashbackApplication.addTransaction("TestCard", "TestCategory", 500);
        assertEquals(25, testData.banks.get("TestBank").getCashbackEarned(), "Кэшбек должен считаться правильно");
    }


    @Test
    @Order(6)
    void chooseCashback() {
        CashbackApplication.addBank("TestBank", 3000);
        CashbackApplication.addCard("TestBank", "TestCard");
        CashbackApplication.addCurrentCashback("TestCard", "Рестораны", 5, false);
        CashbackApplication.addTransaction("TestCard", "Рестораны", 500);
        assertEquals("TestCard", chooseCard("Рестораны", 1500), "Карта должна быть выбрана");
    }
}
