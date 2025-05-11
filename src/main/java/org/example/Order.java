package org.example;

import java.math.BigDecimal;
import java.util.*;

/**
 * Klasa Order reprezentuje pojedyncze zamówienie w systemie.
 *
 * Każde zamówienie zawiera:
 * - id: unikalny identyfikator zamówienia,
 * - value: całkowitą wartość zamówienia, zapisaną jako BigDecimal zapewniając precyzyjne obliczenia,
 * - promotions: opcjonalną listę identyfikatorów promocji, które określają, jakie metody płatności (np. karty promocyjne)
 *   mogą być zastosowane do opłacenia tego zamówienia.
 */
public class Order
{
    public String id;
    public BigDecimal value;
    public List<String> promotions;
}
