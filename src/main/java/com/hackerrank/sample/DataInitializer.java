package com.hackerrank.sample;

import com.hackerrank.sample.model.Item;
import com.hackerrank.sample.repository.ItemRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public void run(ApplicationArguments args) {
        if (itemRepository.count() > 0) {
            return;
        }

        List<Item> items = List.of(
                new Item("SNEAK-001", "Air Walk Pro",
                        "https://example.com/images/sneak-001.jpg",
                        "Zapatilla cómoda para caminata diaria",
                        89.99, 4.5, specs("caminar", "blanco", "malla")),
                new Item("SNEAK-002", "Jog Master",
                        "https://example.com/images/sneak-002.jpg",
                        "Zapatilla para trote ligero con amortiguación media",
                        109.99, 4.2, specs("trotar", "azul", "sintético")),
                new Item("SNEAK-003", "Sprint Elite",
                        "https://example.com/images/sneak-003.jpg",
                        "Zapatilla de alto rendimiento para correr largas distancias",
                        139.99, 4.8, specs("correr", "rojo", "carbono")),
                new Item("SNEAK-004", "Trail Blazer",
                        "https://example.com/images/sneak-004.jpg",
                        "Zapatilla resistente para senderos de montaña",
                        149.99, 4.6, specs("montaña", "verde", "cuero sintético")),
                new Item("SNEAK-005", "Cloud Rest",
                        "https://example.com/images/sneak-005.jpg",
                        "Zapatilla ultrasuave para descanso y uso casual",
                        79.99, 4.3, specs("descanso", "gris", "espuma")),
                new Item("SNEAK-006", "Sand Step",
                        "https://example.com/images/sneak-006.jpg",
                        "Zapatilla ligera para caminar en la playa",
                        59.99, 4.0, specs("playa", "amarillo", "tela")),
                new Item("SNEAK-007", "City Walker",
                        "https://example.com/images/sneak-007.jpg",
                        "Zapatilla urbana para caminatas largas en ciudad",
                        94.99, 4.1, specs("caminar", "negro", "cuero")),
                new Item("SNEAK-008", "Morning Jog",
                        "https://example.com/images/sneak-008.jpg",
                        "Zapatilla para sesiones matutinas de trote",
                        119.99, 4.4, specs("trotar", "naranja", "malla")),
                new Item("SNEAK-009", "Speed Force",
                        "https://example.com/images/sneak-009.jpg",
                        "Zapatilla competitiva para carreras de velocidad",
                        155.99, 4.7, specs("correr", "plateado", "fibra")),
                new Item("SNEAK-010", "Mountain King",
                        "https://example.com/images/sneak-010.jpg",
                        "Zapatilla premium para alta montaña y terrenos difíciles",
                        169.99, 4.9, specs("montaña", "marrón", "gore-tex")),
                new Item("SNEAK-011", "Zen Step",
                        "https://example.com/images/sneak-011.jpg",
                        "Zapatilla ergonómica para descanso y recuperación",
                        84.99, 4.2, specs("descanso", "azul claro", "espuma viscoelástica")),
                new Item("SNEAK-012", "Wave Runner",
                        "https://example.com/images/sneak-012.jpg",
                        "Zapatilla acuática para actividades en la playa y orilla",
                        65.99, 3.9, specs("playa", "turquesa", "neopreno"))
        );

        itemRepository.saveAll(items);
    }

    private Map<String, String> specs(String subcategory, String color, String material) {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("subcategory", subcategory);
        map.put("color", color);
        map.put("material", material);
        return map;
    }
}
