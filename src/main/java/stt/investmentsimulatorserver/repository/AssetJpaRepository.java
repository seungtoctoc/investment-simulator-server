package stt.investmentsimulatorserver.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import stt.investmentsimulatorserver.domain.Asset;

@Repository
public interface AssetJpaRepository extends JpaRepository<Asset, Integer> {
    List<Asset> findAllByKoreanNameContaining(String koreanName);

    List<Asset> findAllByNameContainingIgnoreCase(String symbol);

    List<Asset> findAllBySymbolStartingWithIgnoreCase(String symbol);
}
