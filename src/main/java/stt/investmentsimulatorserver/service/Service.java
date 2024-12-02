package stt.investmentsimulatorserver.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import stt.investmentsimulatorserver.domain.Assets;
import stt.investmentsimulatorserver.repository.AssetsRepository;
import stt.investmentsimulatorserver.utils.Utils;

@org.springframework.stereotype.Service
@AllArgsConstructor
public class Service {
    private AssetsRepository assetsRepository;

    public List<Assets> findAssets(String keyword, int limit) {
        if (Utils.containsKorean(keyword)) {
            List<Assets> assetsFoundByKoreanName = assetsRepository.findAllByKoreanNameContaining(keyword);
            assetsFoundByKoreanName = sortAssetsByMarketCap(assetsFoundByKoreanName);
            return Utils.limitList(assetsFoundByKoreanName, limit);
        }

        List<Assets> assetsFoundByName = assetsRepository.findAllByNameContainingIgnoreCase(keyword);
        assetsFoundByName = sortAssetsByMarketCap(assetsFoundByName);

        if (Utils.containsEnglishAndNumber(keyword)) {
            return Utils.limitList(assetsFoundByName, limit);
        }

        List<Assets> assetsFoundBySymbol = assetsRepository.findAllBySymbolStartingWithIgnoreCase(keyword);
        assetsFoundBySymbol = sortAssetsByMarketCap(assetsFoundBySymbol);

        List<Assets> result = new ArrayList<>();
        result.addAll(Utils.limitList(assetsFoundBySymbol, limit / 2));
        result.addAll(Utils.limitList(assetsFoundByName, limit - result.size()));

        return result;
    }

    List<Assets> sortAssetsByMarketCap(List<Assets> assets) {
        int krwUsd = 1400;

        return assets.stream().sorted((asset1, asset2) -> {
            long marketCap1 =
                asset1.getExchange().startsWith("KOS") ? asset1.getMarketCap() : asset1.getMarketCap() / krwUsd;
            long marketCap2 =
                asset2.getExchange().startsWith("KOS") ? asset2.getMarketCap() : asset2.getMarketCap() / krwUsd;

            return Long.compare(marketCap2, marketCap1);
        }).collect(Collectors.toList());
    }

}