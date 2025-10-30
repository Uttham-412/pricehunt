package com.pricehunt;

import com.google.gson.*;
import jakarta.annotation.PreDestroy; 
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException; 
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SerpApiService {
    // 4. FIX: Use a class-level logger
    private static final Logger logger = LoggerFactory.getLogger(SerpApiService.class);

    // 5. FIX: Initialize and reuse HttpClient for performance
    private final CloseableHttpClient httpClient = HttpClients.createDefault();

    @Value("${serpapi.key}")
    private String apiKey;

    // 6. FIX: Add method to close client when Spring context shuts down (Cleanup)
    @PreDestroy
    public void closeHttpClient() {
        try {
            logger.info("Closing SerpApiService HttpClient.");
            httpClient.close();
        } catch (IOException e) {
            logger.error("Error closing HttpClient on shutdown", e);
        }
    }

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
        // 7. FIX: Use the class-level client instead of creating a new one
        HttpGet request = new HttpGet(url);
        return httpClient.execute(request, response -> {
            String json = EntityUtils.toString(response.getEntity());
            return JsonParser.parseString(json).getAsJsonObject();
        });
    }

    public List<Product> searchProducts(String query) {
        List<Product> products = new ArrayList<>();

        // 🔹 Amazon
        String amazonUrl = UriComponentsBuilder.fromHttpUrl("https://serpapi.com/search.json")
                .queryParam("k", query) 
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
            } else if (amazonData.has("error")) {
                // Log explicit API error
                logger.error("SerpAPI (Amazon) returned an error: {}", getJsonValue(amazonData, "error"));
            }
        } catch (Exception e) {
            // Log failure using the logger
            logger.error("Failed to fetch or parse Amazon results for query: {}", query, e);
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
                    
                    // CRITICAL FIX: Check for root-relative links and prepend the Google domain.
                    if (link.startsWith("/")) {
                        link = "https://www.google.com" + link;
                    } else if (!link.startsWith("http://") && !link.startsWith("https://") && !link.equals("N/A")) {
                        // If it's missing the scheme (e.g., www.flipkart.com/...), prepend https://.
                        link = "https://" + link;
                    }
                    
                    products.add(new Product(title, price, rating, source, link, image));
                }
            } else if (shopData.has("error")) {
                // Log explicit API error
                logger.error("SerpAPI (Google Shopping) returned an error: {}", getJsonValue(shopData, "error"));
            }
        } catch (Exception e) {
            // Log failure using the logger
            logger.error("Failed to fetch or parse Google Shopping results for query: {}", query, e);
        }

        return products;
    }
}