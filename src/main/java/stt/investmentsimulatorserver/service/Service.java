package stt.investmentsimulatorserver.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

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
import stt.investmentsimulatorserver.response.SimulateAssetResponse;
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

    public SimulateAssetResponse simulateAsset(SimulateAssetRequest simulateAssetRequest) {
        List<Price> prices = priceJpaRepository.findAllBySymbol(simulateAssetRequest.getSymbol());
        List<Dividend> dividends = dividendJpaRepository.findAllBySymbol(simulateAssetRequest.getSymbol());
        List<Split> splits = splitJpaRepository.findAllBySymbol(simulateAssetRequest.getSymbol());

        prices.sort(Comparator.comparing(Price::getDate).reversed());
        prices = Utils.limitList(prices, simulateAssetRequest.getPeriod() * 12);

        LocalDate startDate = prices.get(prices.size() - 1).getDate();
        PriorityQueue<Event> eventQueue = new PriorityQueue<>(Comparator.comparing(Event::getDate));
        for (Price price : prices) {
            eventQueue.add(new Event(price.getDate(), "price", price, null, null));
        }
        for (Dividend dividend : dividends) {
            if (dividend.getDate().isAfter(startDate)) {
                eventQueue.add(new Event(dividend.getDate(), "dividend", null, dividend, null));
            }
        }
        for (Split split : splits) {
            if (split.getDate().isAfter(startDate)) {
                eventQueue.add(new Event(split.getDate(), "split", null, null, split));
            }
        }

        double depositExchangeRateOfStartDate = getDepositExchangeRate(
            simulateAssetRequest.getExchange(), simulateAssetRequest.getIsDollar(), startDate);
        double cash =
            (simulateAssetRequest.getSeed() - simulateAssetRequest.getMonthly()) * depositExchangeRateOfStartDate;
        cash = Utils.floorMoney(cash, simulateAssetRequest.getIsDollar());
        double totalAmount = 0;
        double totalDividend = 0;
        double latestValuation = 0;
        List<ValuationHistory> valuationHistories = new ArrayList<>();
        List<PurchaseHistory> purchaseHistories = new ArrayList<>();
        List<DividendHistory> dividendHistories = new ArrayList<>();
        List<SplitHistory> splitHistories = new ArrayList<>();
        while (!eventQueue.isEmpty()) {
            Event event = eventQueue.poll();

            if (event.getType().equals("split")) {
                double beforeAmount = totalAmount;
                totalAmount =
                    (double)Math.round(
                        totalAmount / event.getSplit().getDenominator() * event.getSplit().getNumerator() * 10) / 10;

                splitHistories.add(new SplitHistory(event.getDate(), beforeAmount, totalAmount));
                continue;
            }

            double depositExchangeRate = getDepositExchangeRate(simulateAssetRequest.getExchange(),
                simulateAssetRequest.getIsDollar(), event.getDate());
            double valuationExchangeRate = getValuationExchangeRate(simulateAssetRequest.getExchange(),
                simulateAssetRequest.getIsDollar(), event.getDate());

            if (event.getType().equals("price")) {
                cash += Utils.floorMoney(simulateAssetRequest.getMonthly() * depositExchangeRate,
                    simulateAssetRequest.getIsDollar());
                double amountIncrease = cash / event.getPrice().getClose();
                cash %= event.getPrice().getClose();
                totalAmount += (long)amountIncrease;
                latestValuation = (event.getPrice().getClose() * totalAmount + cash) * valuationExchangeRate;
                
                purchaseHistories.add(
                    new PurchaseHistory(event.getDate(), event.getPrice().getClose() * valuationExchangeRate,
                        (long)amountIncrease, totalAmount));
                valuationHistories.add(
                    new ValuationHistory(event.getDate(), latestValuation)
                );

                continue;
            }

            if (event.getType().equals("dividend")) {
                double dividend = totalAmount * event.getDividend().getDividend();
                totalDividend += dividend;
                latestValuation += cash * valuationExchangeRate;

                dividendHistories.add(
                    new DividendHistory(event.getDate(), totalAmount, dividend * valuationExchangeRate,
                        totalDividend * valuationExchangeRate));
                valuationHistories.add(
                    new ValuationHistory(event.getDate(), latestValuation));
            }
        }

        double finalValuationExchangeRate = getValuationExchangeRate(
            simulateAssetRequest.getExchange(), simulateAssetRequest.getIsDollar(), prices.get(0).getDate());
        double totalValuation = (prices.get(0).getClose() * totalAmount + cash) * finalValuationExchangeRate;
        double totalProfit = totalValuation - simulateAssetRequest.getMonthly()
            - simulateAssetRequest.getMonthly() * simulateAssetRequest.getMonthly() * 12;

        return new SimulateAssetResponse(
            totalValuation,
            totalProfit,
            totalProfit / totalValuation,
            totalAmount,
            totalDividend,
            cash,
            valuationHistories,
            purchaseHistories,
            dividendHistories,
            splitHistories
        );
    }

    Double getDepositExchangeRate(String exchange, Boolean isDollar, LocalDate date) {
        if (isSameCurrency(exchange, isDollar)) {
            return 1.0;
        }

        List<Price> depositExchangeRates = isDollar ? priceCache.getUsdKrwPrices() : priceCache.getKrwUsdPrices();

        Price depositExchangeRate = depositExchangeRates.stream()
            .filter(price -> price.getDate().getYear() == date.getYear()
                && price.getDate().getMonthValue() == date.getMonthValue())
            .min(Comparator.comparing(Price::getDate))
            .orElse(null);

        if (depositExchangeRate == null) {
            throw new IllegalStateException("cannot find price for deposit exchange rate");
        }

        return depositExchangeRate.getClose();
    }

    Double getValuationExchangeRate(String exchange, Boolean isDollar, LocalDate date) {
        if (isSameCurrency(exchange, isDollar)) {
            return 1.0;
        }

        List<Price> valuationExchangeRates = isDollar ? priceCache.getKrwUsdPrices() : priceCache.getUsdKrwPrices();

        Price valuationExchangeRate = valuationExchangeRates.stream()
            .filter(price -> price.getDate().getYear() == date.getYear()
                && price.getDate().getMonthValue() == date.getMonthValue())
            .min(Comparator.comparing(Price::getDate))
            .orElse(null);

        if (valuationExchangeRate == null) {
            throw new IllegalStateException("cannot find price for valuation exchange rate");
        }

        return valuationExchangeRate.getClose();
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