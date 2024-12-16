package com.hangha.alarmservice.Controller;


import com.hangha.alarmservice.Service.ManualNotificationSevice;
import com.hangha.alarmservice.Service.ProductService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class productContoller {

    private final ProductService productService;
    private final ManualNotificationSevice manualNotificationSevice;

    public productContoller(ProductService productService, ManualNotificationSevice manualNotificationSevice) {
        this.productService = productService;
        this.manualNotificationSevice = manualNotificationSevice;
    }


    //재입고 알람 발송
    @PostMapping("/products/{productId}/notifications/re-stock")
    public void reStock(@PathVariable Long productId) {
        productService.handleRestock(productId);
    }

    @PutMapping("/products/{productId}/mark-in-stock")
    public void markInStock(@PathVariable Long productId) {
        productService.handleOutOfStock(productId);
    }

    //수동으로 재입고 알람 발송
    @PostMapping("/admin/products/{productId}/notifications/re-stock")
    public void manualsend(@PathVariable Long productId){
        manualNotificationSevice.resendNotifications(productId);
    }
}
