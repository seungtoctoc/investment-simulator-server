package stt.investmentsimulatorserver.service;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import lombok.AllArgsConstructor;
import stt.investmentsimulatorserver.domain.Assets;
import stt.investmentsimulatorserver.repository.AssetsRepository;

@org.springframework.stereotype.Service
@AllArgsConstructor
public class Service {
    private AssetsRepository assetsRepository;

    public List<Assets> findAssets(String keyword) {
        List<Assets> foundAssets = assetsRepository.findAllBySymbolContainingIgnoreCase(keyword);

        return foundAssets;
    }
}
