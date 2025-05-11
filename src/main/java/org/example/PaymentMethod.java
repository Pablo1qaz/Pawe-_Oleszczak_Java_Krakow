package org.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * Klasa PaymentMethod reprezentuje jedną metodę płatności dostępną w systemie.
 *
 * Każda metoda płatności określa:
 * - unikalny identyfikator (id), który rozpoznaje, o jaką metodę chodzi (np. "PUNKTY", "mZysk", "BosBankrut"),
 * - procentowy rabat (discount), który jest naliczany w przypadku użycia danej metody,
 * - dostępny limit środków (limit), czyli maksymalną kwotę, którą można wykorzystać przy
 *   danej metodzie płatności.
 *
 * Przykład:
 * Jeśli mamy metodę "mZysk" z rabatem 10% i limitem 180.00, oznacza to, że przy pełnej płatności
 * tą metodą klient otrzyma rabat 10%, a maksymalnie można rozliczyć zamówienia o łącznej wartości
 * nieprzekraczającej 180.00.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class PaymentMethod
{
        public String id;
        public BigDecimal discount;
        public BigDecimal limit;

        }

