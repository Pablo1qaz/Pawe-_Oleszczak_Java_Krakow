package org.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * Klasa testowa sprawdzająca działanie algorytmu alokacji płatności.
 */
public class AlgorithmTest {

    /**
     * Testuje scenariusz opisany w specyfikacji:
     *   - ORDER1: wartość 100.00, promocje: ["mZysk"]
     *   - ORDER2: wartość 200.00, promocje: ["BosBankrut"]
     *   - ORDER3: wartość 150.00, promocje: ["mZysk", "BosBankrut"]
     *   - ORDER4: wartość 50.00, bez promocji
     *
     * Przy użyciu metod płatności:
     *   - PUNKTY: discount 15%, limit 100.00
     *   - mZysk: discount 10%, limit 180.00
     *   - BosBankrut: discount 5%, limit 200.00
     *
     * Oczekiwany rozkład:
     *   - PUNKTY: 100.00
     *   - mZysk: 165.00
     *   - BosBankrut: 190.00
     */
    @Test
    public void testSampleScenario() {
        // Tworzymy listę zamówień.
        List<Order> orders = new ArrayList<>();

        Order order1 = new Order();
        order1.id = "ORDER1";
        order1.value = new BigDecimal("100.00");
        order1.promotions = Arrays.asList("mZysk");
        orders.add(order1);

        Order order2 = new Order();
        order2.id = "ORDER2";
        order2.value = new BigDecimal("200.00");
        order2.promotions = Arrays.asList("BosBankrut");
        orders.add(order2);

        Order order3 = new Order();
        order3.id = "ORDER3";
        order3.value = new BigDecimal("150.00");
        order3.promotions = Arrays.asList("mZysk", "BosBankrut");
        orders.add(order3);

        Order order4 = new Order();
        order4.id = "ORDER4";
        order4.value = new BigDecimal("50.00");
        // promocje pozostają null, co oznacza brak przypisanych promocji
        orders.add(order4);

        // Tworzymy listę metod płatności.
        List<PaymentMethod> methods = new ArrayList<>();

        PaymentMethod punkty = new PaymentMethod();
        punkty.id = "PUNKTY";
        punkty.discount = new BigDecimal("15"); // 15%
        punkty.limit = new BigDecimal("100.00");
        methods.add(punkty);

        PaymentMethod mZysk = new PaymentMethod();
        mZysk.id = "mZysk";
        mZysk.discount = new BigDecimal("10"); // 10%
        mZysk.limit = new BigDecimal("180.00");
        methods.add(mZysk);

        PaymentMethod bosBankrut = new PaymentMethod();
        bosBankrut.id = "BosBankrut";
        bosBankrut.discount = new BigDecimal("5"); // 5%
        bosBankrut.limit = new BigDecimal("200.00");
        methods.add(bosBankrut);

        // Inicjujemy algorytm i wykonujemy optymalizację alokacji płatności.
        Algorithm optimizer = new Algorithm(methods);
        optimizer.optimize(orders);

        // Pobieramy wyniki alokacji.
        // W klasie Algorithm dodaliśmy metodę getMethodSpendings(), która zwraca mapę:
        // Map<String, BigDecimal> methodSpendings.
        Map<String, BigDecimal> allocations = optimizer.getMethodSpendings();

        // Sprawdzamy, czy alokacja jest zgodna z oczekiwaniami.
        // Oczekiwane wartości:
        // PUNKTY: 100.00, mZysk: 165.00, BosBankrut: 190.00.
        assertNotNull(allocations);
        assertEquals(new BigDecimal("100.00"), allocations.get("PUNKTY"), "Błędna alokacja dla PUNKTY");
        assertEquals(new BigDecimal("165.00"), allocations.get("mZysk"), "Błędna alokacja dla mZysk");
        assertEquals(new BigDecimal("190.00"), allocations.get("BosBankrut"), "Błędna alokacja dla BosBankrut");
    }

    // Możesz dodać tutaj kolejne testy jednostkowe,
    // które sprawdzą np. scenariusze:
    // - pełnej płatności metodą punktową,
    // - pełnej płatności kartą promocyjną,
    // - częściowej płatności punktami z dopełnieniem kartą,
    // - obsługi zamówień, których nie uda się opłacić.
}