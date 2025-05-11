package org.example;

import java.math.BigDecimal;

/**
 * Klasa PaymentAllocation reprezentuje alokację środków dla
 * konkretnej metody płatności. Obiekt tej klasy informuje, jaka kwota
 * została przypisana do konkretnej metody, co pozwala śledzić rozkład płatności
 * na poszczególne metody (np. "PUNKTY", "mZysk", "BosBankrut").
 *
 * Użycie tej klasy ułatwia generowanie podsumowania rozliczeń,
 * które na końcu prezentuje użytkownikowi, ile środków zostało wykorzystanych z każdej metody.
 */
public class PaymentAllocation {
    public String methodId;
    public BigDecimal amount;

    /**
     * Konstruktor klasy PaymentAllocation.
     * Inicjalizuje nowy obiekt alokacji płatności z podanym identyfikatorem metody oraz kwotą.
     *
     * @param methodId Identyfikator metody płatności, której dotyczy alokacja.
     * @param amount Kwota, która została przypisana do tej metody płatności.
     */
    public PaymentAllocation(String methodId, BigDecimal amount) {
        this.methodId = methodId;
        this.amount = amount;
    }

}
