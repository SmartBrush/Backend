package com.smartbrush.smartbrush_backend.loader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartbrush.smartbrush_backend.entity.Product;
import com.smartbrush.smartbrush_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductDataLoader implements CommandLineRunner {

    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        loadAndSave("products_shampoo.json", "shampoo", mapper);
        loadAndSave("products_conditioner.json", "conditioner", mapper);
        loadAndSave("products_treatment.json", "treatment", mapper);
        loadAndSave("products_essence.json", "essence", mapper);
        loadAndSave("products_tonic.json", "tonic", mapper);
    }

    private void loadAndSave(String filename, String category, ObjectMapper mapper) throws IOException {
        File file = new File("src/main/resources/" + filename);
        List<Map<String, String>> rawList = mapper.readValue(file, new TypeReference<>() {});
        List<Product> products = rawList.stream().map(data -> Product.builder()
                .name(data.get("name"))
                .price(data.get("price"))
                .image(data.get("image"))
                .link(data.get("link"))
                .category(category)
                .build()).collect(Collectors.toList());
        productRepository.saveAll(products);
    }
}
