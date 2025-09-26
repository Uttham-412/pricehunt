package com.pricehunt;

import com.google.gson.*;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SerpApiService {
    @Value("${serpapi.key}")
    private String apiKey;

    private String getJsonValue(JsonObject obj, String key) {
        return Optional.ofNullable(obj.get(key))
                .filter(e -> !e.isJsonNull())
                .map(JsonElement::getAsString).orElse("N/A");
    }

    private String getJsonValue(JsonObject obj, String key, String defaultValue) {
        return Optional.ofNullable(obj.get(key))
                .filter(e -> !e.isJsonNull())
                .map(JsonElement::getAsString).orElse(defaultValue);
    }
    private JsonObject fetch(String url) throws Exception {
        try (CloseableHttpClient client = HttpClients.createDefault();) {
            HttpGet request = new HttpGet(url);
            return client.execute(request, response -> {
                String json = EntityUtils.toString(response.getEntity());
                return JsonParser.parseString(json).getAsJsonObject();
            });
        }
    }

    public List<Product> searchProducts(String query) {
        List<Product> products = new ArrayList<>();

        // 🔹 Amazon
        String amazonUrl = UriComponentsBuilder.fromHttpUrl("https://serpapi.com/search.json")
                .queryParam("q", query)
                .queryParam("engine", "amazon")
                .queryParam("amazon_domain", "amazon.in")
                .queryParam("api_key", apiKey)
                .toUriString();

        try {
            JsonObject amazonData = fetch(amazonUrl);
            if (amazonData.has("organic_results")) {
                JsonArray amazonResults = amazonData.getAsJsonArray("organic_results");
                for (JsonElement e : amazonResults) {
                    JsonObject obj = e.getAsJsonObject();
                    String title = getJsonValue(obj, "title");
                    String priceValue = "N/A";
                    if (obj.has("price") && obj.get("price").isJsonObject()) {
                        priceValue = getJsonValue(obj.getAsJsonObject("price"), "value");
                    }
                    String rating = getJsonValue(obj, "rating");
                    String link = getJsonValue(obj, "link", "");
                    String image = getJsonValue(obj, "thumbnail", "");
                    products.add(new Product(title, "₹" + priceValue, rating, "Amazon", link, image));
                }
            }
        } catch (Exception e) {
            // Log the exception or handle it as needed
            System.err.println("Failed to fetch or parse Amazon results: " + e.getMessage());
        }

        // 🔹 Google Shopping (Flipkart, Myntra, Ajio)
        String shopUrl = UriComponentsBuilder.fromHttpUrl("https://serpapi.com/search.json")
                .queryParam("q", query)
                .queryParam("engine", "google_shopping")
                .queryParam("gl", "in")
                .queryParam("hl", "en")
                .queryParam("api_key", apiKey)
                .toUriString();
        try {
            JsonObject shopData = fetch(shopUrl);
            if (shopData.has("shopping_results")) {
                JsonArray shopResults = shopData.getAsJsonArray("shopping_results");
                for (JsonElement e : shopResults) {
                    JsonObject obj = e.getAsJsonObject();
                    String title = getJsonValue(obj, "title");
                    String price = getJsonValue(obj, "price");
                    String rating = getJsonValue(obj, "rating");
                    String image = getJsonValue(obj, "thumbnail", "");
                    String source = getJsonValue(obj, "source");
                    String link = getJsonValue(obj, "link");
                    products.add(new Product(title, price, rating, source, link, image));
                }
            }
        } catch (Exception e) {
            // Log the exception or handle it as needed
            System.err.println("Failed to fetch or parse Google Shopping results: " + e.getMessage());
        }

        return products;
    }
}
