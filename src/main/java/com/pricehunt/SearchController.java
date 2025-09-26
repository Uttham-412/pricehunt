package com.pricehunt;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SearchController {
    private final SerpApiService service;

    public SearchController(SerpApiService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public List<Product> search(@RequestParam String query) throws Exception {
        return service.searchProducts(query);
    }
}
