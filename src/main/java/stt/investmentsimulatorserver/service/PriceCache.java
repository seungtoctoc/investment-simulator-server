package stt.investmentsimulatorserver.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import stt.investmentsimulatorserver.domain.Price;
import stt.investmentsimulatorserver.repository.PriceJpaRepository;

@Component
@AllArgsConstructor
public class PriceCache {
    PriceJpaRepository priceJpaRepository;

    @Cacheable(value = "priceCache", key = "'KrwUsd'")
    public List<Price> getKrwUsdPrices() {
        return priceJpaRepository.findAllBySymbol("KRWUSD");
    }

    @Cacheable(value = "priceCache", key = "'UsdKrw'")
    public List<Price> getUsdKrwPrices() {
        return priceJpaRepository.findAllBySymbol("USDKRW");
    }
}
