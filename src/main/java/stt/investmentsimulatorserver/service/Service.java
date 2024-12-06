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
        double totalAmount = 0;
        double totalDividend = 0;
        double latestValuation = 0;
        double totalInput = simulateAssetRequest.getSeed() - simulateAssetRequest.getMonthly();
        double cash = totalInput * depositExchangeRateOfStartDate;
        cash = Utils.floorMoney(cash, simulateAssetRequest.getIsDollar());

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
                totalInput += simulateAssetRequest.getMonthly();

                cash += simulateAssetRequest.getMonthly() * depositExchangeRate;
                cash = Utils.floorMoney(cash, simulateAssetRequest.getIsDollar());

                double amountIncrease = cash / event.getPrice().getClose();
                totalAmount += (long)amountIncrease;

                cash %= event.getPrice().getClose();
                cash = Utils.floorMoney(cash, simulateAssetRequest.getIsDollar());

                latestValuation = (event.getPrice().getClose() * totalAmount + cash) * valuationExchangeRate;
                latestValuation = Utils.floorMoney(latestValuation, simulateAssetRequest.getIsDollar());

                double valuationPer = event.getPrice().getClose() * valuationExchangeRate;
                valuationPer = Utils.floorMoney(valuationPer, simulateAssetRequest.getIsDollar());

                purchaseHistories.add(
                    new PurchaseHistory(event.getDate(), valuationPer, (long)amountIncrease, totalAmount));
                valuationHistories.add(
                    new ValuationHistory(event.getDate(), latestValuation)
                );

                continue;
            }

            if (event.getType().equals("dividend")) {
                double dividend = totalAmount * event.getDividend().getDividend();
                totalDividend += dividend;
                totalDividend = Utils.floorMoney(totalDividend, simulateAssetRequest.getIsDollar());

                if (simulateAssetRequest.getIsReinvest()) {
                    cash += dividend;
                    cash = Utils.floorMoney(cash, simulateAssetRequest.getIsDollar());
                }

                latestValuation += cash * valuationExchangeRate;
                latestValuation = Utils.floorMoney(latestValuation, simulateAssetRequest.getIsDollar());

                dividendHistories.add(
                    new DividendHistory(event.getDate(), totalAmount, dividend,
                        totalDividend));
                valuationHistories.add(
                    new ValuationHistory(event.getDate(), latestValuation));
            }
        }

        double totalProfit = latestValuation - totalInput;
        totalProfit = Utils.floorMoney(totalProfit, simulateAssetRequest.getIsDollar());
        double profitRate = Math.round(totalProfit / totalInput * 100 * 100) / 100.0;

        String valuationCurrency = simulateAssetRequest.getIsDollar() ? "달러(USD)" : "원(KRW)";
        String exchangeCurrency = getExchangeCurrency(simulateAssetRequest.getExchange());

        return new SimulateAssetResponse(
            latestValuation,
            totalProfit,
            profitRate,
            totalAmount,
            totalDividend,
            cash,
            valuationHistories,
            purchaseHistories,
            dividendHistories,
            splitHistories,
            simulateAssetRequest.getSymbol(),
            valuationCurrency,
            exchangeCurrency
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

    String getExchangeCurrency(String exchange) {
        Map<String, String> currency = Map.of(
            "KOSDAQ", "원(KRW)",
            "KOSPI", "원(KRW)",
            "NASDAQ", "달러(USD)",
            "NYSE", "달러(USD)",
            "AMEX", "달러(USD)"
        );
        return currency.get(exchange);
    }

    Boolean isSameCurrency(String exchange, Boolean isDollar) {
        if (exchange.equals("FOREX")) {
            return false;
        }

        String originalCurrency = isDollar ? "달러(USD)" : "원(KRW)";
        String assetCurrency = getExchangeCurrency(exchange);

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