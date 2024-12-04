package stt.investmentsimulatorserver.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;

import lombok.AllArgsConstructor;
import stt.investmentsimulatorserver.domain.Asset;
import stt.investmentsimulatorserver.domain.Dividend;
import stt.investmentsimulatorserver.domain.Price;
import stt.investmentsimulatorserver.domain.Split;
import stt.investmentsimulatorserver.repository.AssetJpaRepository;
import stt.investmentsimulatorserver.repository.DividendJpaRepository;
import stt.investmentsimulatorserver.repository.PriceJpaRepository;
import stt.investmentsimulatorserver.repository.SplitJpaRepository;
import stt.investmentsimulatorserver.request.SimulateAssetRequest;
import stt.investmentsimulatorserver.utils.Utils;

@org.springframework.stereotype.Service
@AllArgsConstructor
public class Service {
    private AssetJpaRepository assetJpaRepository;
    private DividendJpaRepository dividendJpaRepository;
    private PriceJpaRepository priceJpaRepository;
    private SplitJpaRepository splitJpaRepository;

    PriceCache priceCache;

    public List<Asset> findAssets(String keyword, int limit) {
        if (Utils.containsKorean(keyword)) {
            List<Asset> assetsFoundByKoreanName = assetJpaRepository.findAllByKoreanNameContaining(keyword);
            assetsFoundByKoreanName = sortAssetsByMarketCap(assetsFoundByKoreanName);
            return Utils.limitList(assetsFoundByKoreanName, limit);
        }

        List<Asset> assetsFoundByName = assetJpaRepository.findAllByNameContainingIgnoreCase(keyword);
        assetsFoundByName = sortAssetsByMarketCap(assetsFoundByName);

        if (Utils.containsEnglishAndNumber(keyword)) {
            return Utils.limitList(assetsFoundByName, limit);
        }

        List<Asset> assetsFoundBySymbol = assetJpaRepository.findAllBySymbolStartingWithIgnoreCase(keyword);
        assetsFoundBySymbol = sortAssetsByMarketCap(assetsFoundBySymbol);

        List<Asset> result = new ArrayList<>();
        result.addAll(Utils.limitList(assetsFoundBySymbol, limit / 2));
        result.addAll(Utils.limitList(assetsFoundByName, limit - result.size()));

        return result;
    }

    public Map<String, Object> simulateAsset(SimulateAssetRequest simulateAssetRequest) {
        Map<String, Object> result = new HashMap<>();

        List<Price> prices = priceJpaRepository.findAllBySymbol(simulateAssetRequest.getSymbol());
        List<Dividend> dividends = dividendJpaRepository.findAllBySymbol(simulateAssetRequest.getSymbol());
        List<Split> splits = splitJpaRepository.findAllBySymbol(simulateAssetRequest.getSymbol());

        prices.sort(Comparator.comparing(Price::getDate).reversed());
        prices = Utils.limitList(prices, simulateAssetRequest.getPeriod() * 12);

        LocalDate date = LocalDate.parse("2024-01-02");
        Double depositExchangeRate = getDepositExchangeRate(simulateAssetRequest.getExchange(),
            simulateAssetRequest.getIsDollar(), date);

        // result.put("sortedPrices", prices);
        result.put("prices size: ", prices.size());
        result.put("depositExchangeRate", depositExchangeRate);

        return result;
    }

    Boolean isSameCurrency(String exchange, Boolean isDollar) {
        if (exchange.equals("FOREX")) {
            return false;
        }

        Map<String, String> currency = Map.of(
            "KOSDAQ", "KRW",
            "KOSPI", "KRW",
            "NASDAQ", "USD",
            "NYSE", "USD",
            "AMEX", "USD"
        );

        String originalCurrency = isDollar ? "USD" : "KRW";
        String assetCurrency = currency.get(exchange);

        return originalCurrency.equals(assetCurrency);
    }

    Double getDepositExchangeRate(String exchange, Boolean isDollar, LocalDate date) {
        if (isSameCurrency(exchange, isDollar)) {
            return 1.0;
        }

        List<Price> depositExchangeRates = isDollar ? priceCache.getKrwUsdPrices() : priceCache.getUsdKrwPrices();

        Price depositExchangeRate = depositExchangeRates.stream()
            .filter(price -> price.getDate().getYear() == date.getYear()
                && price.getDate().getMonthValue() == date.getMonthValue())
            .min(Comparator.comparing(Price::getDate))
            .orElse(null);

        if (depositExchangeRate == null) {
            throw new IllegalStateException("cannot find dollar price");
        }

        return depositExchangeRate.getClose();
    }

    List<Asset> sortAssetsByMarketCap(List<Asset> assets) {
        int krwUsd = 1400;

        return assets.stream().sorted((asset1, asset2) -> {
            long marketCap1 =
                asset1.getExchange().startsWith("KOS") ? asset1.getMarketCap() / krwUsd : asset1.getMarketCap();
            long marketCap2 =
                asset2.getExchange().startsWith("KOS") ? asset2.getMarketCap() / krwUsd : asset2.getMarketCap();

            return Long.compare(marketCap2, marketCap1);
        }).collect(Collectors.toList());
    }

}