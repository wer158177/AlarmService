package com.hangha.alarmservice.Controller;


import com.hangha.alarmservice.Service.ProductService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class productContoller {

    private final ProductService productService;

    public productContoller(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/products/{productId}/notifications/re-stock")
    public void reStock(@PathVariable Long productId) {
        productService.handleRestock(productId);
    }
}
