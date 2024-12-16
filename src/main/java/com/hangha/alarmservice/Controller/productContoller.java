package com.hangha.alarmservice.Controller;


import com.hangha.alarmservice.Service.ProductService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class productContoller {

    private final ProductService productService;

    public productContoller(ProductService productService) {
        this.productService = productService;
    }


    //재입고 알람 발송
    @PostMapping("/products/{productId}/notifications/re-stock")
    public void reStock(@PathVariable Long productId) {
        productService.handleRestock(productId);
    }

    @PutMapping("/products/{productId}/mark-in-stock")
    public String markInStock(@PathVariable Long productId) {
        productService.handleOutOfStock(productId);
        return "ok";
    }

    //수동으로 재입고 알람 발송
    @PostMapping
    public void manualsend(@PathVariable Long productId){

    }
}
