package stt.investmentsimulatorserver.service;

import java.util.Comparator;
import java.util.List;

import lombok.AllArgsConstructor;
import stt.investmentsimulatorserver.domain.Assets;
import stt.investmentsimulatorserver.repository.AssetsRepository;
import stt.investmentsimulatorserver.utils.Utils;

@org.springframework.stereotype.Service
@AllArgsConstructor
public class Service {
    private AssetsRepository assetsRepository;

    public List<Assets> findAssets(String keyword) {
        List<Assets> foundAssets = assetsRepository.findAllBySymbolStartingWithIgnoreCase(keyword);
        foundAssets.sort(Comparator.comparing(Assets::getSymbol));

        if (foundAssets.size() > 20) {
            return foundAssets;
        }

        if (Utils.isEnglishOnly(keyword)) {
            List<Assets> foundAssetsByName = assetsRepository.findAllByNameContainingIgnoreCase(keyword);
            foundAssetsByName.sort(Comparator.comparing(Assets::getSymbol));
            return foundAssetsByName;
        }

        List<Assets> foundAssetsByKoreanName = assetsRepository.findAllByKoreanNameContainingIgnoreCase(keyword);
        foundAssetsByKoreanName.sort(Comparator.comparing(Assets::getSymbol));
        return foundAssetsByKoreanName;
    }
}
