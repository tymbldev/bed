package com.tymbl.common.controller;

import com.tymbl.common.entity.Currency;
import com.tymbl.common.service.CurrencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/currencies")
public class CurrencyController {

    @Autowired
    private CurrencyService currencyService;

    @GetMapping
    public ResponseEntity<List<Currency>> getAllCurrencies() {
        return ResponseEntity.ok(currencyService.getAllCurrencies());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Currency> getCurrencyById(@PathVariable Long id) {
        return currencyService.getCurrencyById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<Currency> getCurrencyByCode(@PathVariable String code) {
        return currencyService.getCurrencyByCode(code)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
} 