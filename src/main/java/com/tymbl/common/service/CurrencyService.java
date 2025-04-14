package com.tymbl.common.service;

import com.tymbl.common.entity.Currency;
import com.tymbl.common.repository.CurrencyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CurrencyService {

    @Autowired
    private CurrencyRepository currencyRepository;

    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    public Optional<Currency> getCurrencyById(Long id) {
        return currencyRepository.findById(id);
    }

    public Optional<Currency> getCurrencyByCode(String code) {
        return currencyRepository.findByCode(code);
    }
} 