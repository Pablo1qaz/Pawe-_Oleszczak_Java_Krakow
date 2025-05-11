package org.example;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Klasa Algorithm odpowiada za optymalizację sposobu alokacji płatności dla zamówień.
 * Korzysta z listy dostępnych metod płatności np. "PUNKTY" lub konkretne karty promocyjne
 * oraz realizuje zasady określone w specyfikacji zadania.
 */

public class Algorithm {

    private final List<PaymentMethod> paymentMethods;                                                   // Lista wszystkich dostępnych metod płatności
    private final Map<String, PaymentMethod> methodMap;                                                 // Mapa dla szybkiego dostępu – kluczem jest identyfikator metody płatności, a wartością obiekt metody.
    private final Map<String, BigDecimal> methodSpendings = new HashMap<>();                            // Mapa "wydatków" - przechowuje, ile środków (w kwotach) zostało użytych dla każdej metody płatności.


    public Map<String, BigDecimal> getMethodSpendings()
    {
        return new HashMap<>(methodSpendings);
    }

    /**
     * Konstruktor klasy Algorithm.
     * Inicjuje listę metod płatności, tworzy mapę dla szybkiego dostępu oraz ustawia początkowe wydatki na zero.
     *
     * @param paymentMethods Lista dostępnych metod płatności.
     */
    public Algorithm(List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
        this.methodMap = paymentMethods.stream().collect(Collectors.toMap(pm -> pm.id, pm -> pm));      // Tworzymy mapę, aby można było szybko wyszukać metodę płatności po jej identyfikatorze.
        paymentMethods.forEach(pm -> methodSpendings.put(pm.id, BigDecimal.ZERO));                      // Inicjalizacja sumy wydatków dla każdej metody – początkowo wszystkie mają wartość zero.
    }

    /**
     * Metoda publiczna do optymalizacji alokacji płatności dla listy zamówień.
     * Iteruje po każdym zamówieniu oraz dokonuje przypisania odpowiedniej metody płatności.
     *
     * @param orders Lista zamówień, które mają być opłacone.
     */
    public void optimize(List<Order> orders) {
        for (Order order : orders) {
            allocatePayment(order);                                                                     // Dla każdego zamówienia wywołujemy metodę, która przypisze sposób płatności.
        }
    }

    /**
     * Metoda alokująca płatność dla pojedynczego zamówienia.
     *
     * Logika alokacji:
     * 1. Jeśli dostępne są punkty ("PUNKTY") i ich limit wystarcza, aby opłacić całe zamówienie:
     *    - naliczamy rabat punktowy (np. 15%), obliczamy kwotę do zapłaty,
     *      rejestrujemy płatność oraz aktualizujemy limit punktów.
     * 2. Jeśli zamówienie posiada przypisane promocje (np. "mZysk", "BosBankrut"), sprawdzamy:
     *    - czy istnieje taka metoda płatności, która ma wystarczający limit na opłacenie całej wartości zamówienia,
     *      wybieramy tę z najwyższym rabatem, obliczamy rabat oraz modyfikujemy limit tej metody.
     * 3. Jeśli żaden z powyższych warunków nie jest spełniony, sprawdzamy możliwość częściowej płatności punktami:
     *    - Warunkiem jest, aby płatność punktami wynosiła co najmniej 10% wartości zamówienia.
     *      Następnie naliczamy stały rabat 10% dla całego zamówienia, rozliczamy płatność punktami
     *      (do posiadanego limitu) oraz pozostałą kwotę próbujemy opłacić kartą.
     * 4. Jeśli żaden z powyższych sposobów nie działa, próbujemy przypisać płatność przy użyciu dowolnej metody (innej niż PUNKTY)
     *    o wystarczającym limicie na całość zamówienia.
     *
     * @param order Zamówienie, dla którego przydzielamy metodę płatności.
     */
    private void allocatePayment(Order order) {
        BigDecimal orderValue = order.value;                                                            // Pobieramy wartość zamówienia
        BigDecimal hundred = BigDecimal.valueOf(100);                                                   // Utworzenie stałej reprezentującej 100, używanej przy obliczeniach procentowych rabatów.
        PaymentMethod points = methodMap.get("PUNKTY");                                                 // Pobieramy obiekt reprezentujący metodę "PUNKTY" – punkty lojalnościowe.

        //  1. Pełna płatność punktami
        if (points != null && points.limit.compareTo(orderValue) >= 0) {                                // Sprawdzamy, czy metoda "PUNKTY" jest dostępna oraz czy jej limit jest wystarczający by opłacić całe zamówienie.
            BigDecimal discount = orderValue.multiply(points.discount)                                  // Obliczamy rabat w oparciu o procentowy rabat przypisany do punktów.
                    .divide(hundred, 2, RoundingMode.HALF_UP);
            BigDecimal toPay = orderValue.subtract(discount);                                           // Obliczamy kwotę do zapłaty po naliczeniu rabatu.
            applyPayment("PUNKTY", toPay);                                                     // Rejestrujemy płatność dla metody "PUNKTY".
            points.limit = points.limit.subtract(toPay);                                                // Aktualizujemy limit dostępnych środków punktowych, odejmując użyte środki.
            return;
        }

        //  2. Pełna płatność promocyjną kartą
        if (order.promotions != null) {                                                                 // Sprawdzamy, czy zamówienie posiada listę promocji (czyli możliwe metody płatności ze specjalnymi rabatami).
            Optional<String> best = order.promotions.stream()                                           // Przeglądamy listę promocji.
                    .filter(methodMap::containsKey)                                                     // Filtrujemy tylko te, które są zdefiniowane w systemie (istnieją w methodMap).
                    .sorted((a, b) -> methodMap.get(b).discount.compareTo(methodMap.get(a).discount))   // Sortujemy malejąco według wartości rabatu (metoda z najwyższym rabatem będzie pierwsza).
                    .filter(m -> methodMap.get(m).limit.compareTo(orderValue) >= 0)                     // Wybieramy tę metodę, która ma wystarczający limit do opłacenia całego zamówienia.
                    .findFirst();

            if (best.isPresent()) {                                                                     // Jeśli znaleziono odpowiednią metodę płatności promocyjnej
                String methodId = best.get();
                BigDecimal discount = orderValue.multiply(methodMap.get(methodId).discount)             // Obliczamy rabat wg specyfikacji tej metody (procentowo).
                        .divide(hundred, 2, RoundingMode.HALF_UP);
                BigDecimal toPay = orderValue.subtract(discount);                                       // Obliczamy kwotę do zapłaty po naliczeniu rabatu.
                applyPayment(methodId, toPay);                                                          // Rejestrujemy płatność przypisaną tej metodzie (np. "mZysk").
                methodMap.get(methodId).limit = methodMap.get(methodId).limit.subtract(toPay);          // Aktualizujemy limit danej metody, odejmując kwotę, za jaką zamówienie zostało opłacone.
                return;
            }
        }

        //  3. Częściowa płatność punktami + karta (z 10% rabatem)
        if (points != null && points.limit.compareTo(orderValue.multiply(BigDecimal.valueOf(0.10))) >= 0) {     // Sprawdzamy, czy dostępne są punkty, a ich limit wynosi co najmniej 10% wartości zamówienia.
            BigDecimal discount = orderValue.multiply(BigDecimal.valueOf(0.10));                        // Naliczamy stały rabat 10% – niezależnie od rabatu z punktów, gdyż stosujemy specjalną regułę.
            BigDecimal toPayAfterDiscount = orderValue.subtract(discount);                              // Kwota do zapłaty po odjęciu rabatu.

            BigDecimal pointsUsed = points.limit.min(toPayAfterDiscount);                               // Obliczamy, ile punktów możemy użyć – nie więcej niż dostępny limit oraz nie więcej niż wymagana kwota.
            BigDecimal remaining = toPayAfterDiscount.subtract(pointsUsed);                             // Pozostała kwota, którą trzeba opłacić kartą.

            boolean success = allocateWithBestAvailableMethod(order, remaining);                        // Próba alokacji pozostałej kwoty przy użyciu dostępnych metod (z wyłączeniem "PUNKTY").
            if (success) {
                applyPayment("PUNKTY", pointsUsed);                                            // Jeśli udało się opłacić resztę kwoty kartą, rejestrujemy wydatkowanie punktów.
                points.limit = points.limit.subtract(pointsUsed);                                       // Aktualizujemy limit punktów po użyciu.
                return;
            }
        }

        //  4. Fallback - opłacenie całego zamówienia przy użyciu dowolnej metody (innej niż "PUNKTY")
        // Jeśli żaden z powyższych przypadków nie został zastosowany,
        // próbujemy znaleźć dowolną metodę, która ma wystarczający limit,
        // aby opłacić całość zamówienia.
        boolean fallback = allocateWithBestAvailableMethod(order, orderValue);
        if (!fallback) {
            System.out.println("Failed to pay for order: " + order.id);                    // Jeśli nie udało się znaleźć żadnej metody, wypisujemy komunikat o błędzie.
        }
    }

    /**
     * Metoda pomocnicza, która próbuje przypisać płatność dla określonej kwoty
     * przy użyciu dowolnej dostępnej metody (karty, pomijając "PUNKTY").
     * Iterujemy po liście metod i wybieramy pierwszą, która ma wystarczający dostępny limit.
     *
     * @param order Zamówienie, dla którego dokonujemy alokacji (może być użyte do rozszerzenia logiki).
     * @param amount Kwota, którą trzeba opłacić.
     * @return True, jeśli udało się znaleźć metodę płatności i opłacić zamówienie; false w przeciwnym razie.
     */
    private boolean allocateWithBestAvailableMethod(Order order, BigDecimal amount) {
        for (PaymentMethod pm : paymentMethods) {                                           // Iterujemy po wszystkich metodach płatności.
            if (!pm.id.equals("PUNKTY") && pm.limit.compareTo(amount) >= 0) {               // Pomijamy metodę "PUNKTY", ponieważ ta opcja była rozpatrywana osobno.
                applyPayment(pm.id, amount);                                                // Jeśli metoda ma wystarczający limit, przypisujemy kwotę.
                pm.limit = pm.limit.subtract(amount);                                       // Aktualizujemy limit metody – odejmujemy wykorzystaną kwotę.
                return true;                                                                // Zwracamy sukces, bo kwota została opłacona.
            }
        }
        return false;                                                                       // Jeśli żadna metoda nie ma wystarczających środków, zwracamy false.
    }

    /**
     * Metoda rejestrująca wydatkowanie środków dla danej metody płatności.
     * Zapisuje sumę wykorzystanych środków w mapie "methodSpendings".
     *
     * @param methodId Identyfikator metody płatności (np. "PUNKTY", "mZysk", "BosBankrut").
     * @param amount Kwota, która ma być dodana do całkowitych wydatków tej metody.
     */
    private void applyPayment(String methodId, BigDecimal amount) {
        methodSpendings.put(methodId, methodSpendings.get(methodId).add(amount));           // Aktualizacja sumy wydatków – dodajemy nową wartość do aktualnie zarejestrowanej sumy.
    }

    /**
     * Metoda wypisująca wyniki alokacji płatności.
     * Dla każdej metody, dla której zostały użyte środki, wypisuje identyfikator metody oraz sumaryczną kwotę użytych środków.
     * Format: <id_metody> <wydana_kwota> (kwota formatowana do dwóch miejsc po przecinku).
     */
    public void printResults() {
        methodSpendings.forEach((id, amount) -> {                                           // Iterujemy po mapie "methodSpendings"
            if (amount.compareTo(BigDecimal.ZERO) > 0) {                                    // Wypisujemy tylko te metody, dla których suma wydatków jest większa niż zero.
                System.out.printf("%s %.2f%n", id, amount);
            }
        });
    }
}
