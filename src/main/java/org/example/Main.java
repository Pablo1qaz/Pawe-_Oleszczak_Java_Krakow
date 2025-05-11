package org.example;

import java.io.IOException;
import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.io.File;
import java.util.Arrays;
import java.util.List;


public class Main {
    public static void main(String[] args) throws IOException {

        if (args.length < 2) {                                                                                          // Sprawdzamy, czy użytkownik podał wymagane argumenty polecenia: ścieżki do plików JSON.
            System.err.println("Usage: java -jar app.jar <orders.json> <paymentmethods.json>");
            return;
        }

        if (!args[0].toLowerCase().endsWith(".json") || !args[1].toLowerCase().endsWith(".json")) {                     // Walidujemy rozszerzenia plików - muszą to być pliki .json.
            System.err.println("Error: The given files must have the extension: .json.");
            return;
        }

        ObjectMapper mapper = new ObjectMapper();                                                                       // Inicjalizujemy obiekt ObjectMapper z biblioteki Jackson, który umożliwia konwersję JSON <-> Java.
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);                                             // Konfigurujemy mapper, aby akceptował pojedynczy element również jako tablicę.
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);                                                 // Ustawiamy, aby podczas serializacji nie były zapisywane pola o wartości null.

        List<Order> orders = Arrays.asList(mapper.readValue(new File(args[0]), Order[].class));                         // Odczytujemy dane z pliku przekazanego jako pierwszy argument.
        List<PaymentMethod> methods = Arrays.asList(mapper.readValue(new File(args[1]), PaymentMethod[].class));        // Odczytujemy dane z pliku przekazanego jako drugi argument.

        orders.forEach(o -> o.value = new BigDecimal(String.valueOf(o.value)));                                         // Konwersja wartości w obiektach zamówień i metod płatności z String do BigDecimal.
        methods.forEach(m -> {
            m.discount = new BigDecimal(String.valueOf(m.discount));
            m.limit = new BigDecimal(String.valueOf(m.limit));
        });

        Algorithm optimizer = new Algorithm(methods);
        optimizer.optimize(orders);                                                                                     // Wywołujemy metodę optymalizującą alokację środków płatności dla wszystkich zamówień.
        optimizer.printResults();                                                                                       // Po zakończeniu optymalizacji, wypisujemy wyniki z rozbiciem na poszczególne metody.

    }
}