package stt.investmentsimulatorserver.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import stt.investmentsimulatorserver.domain.Assets;

@Repository
public interface AssetsRepository extends JpaRepository<Assets, Integer> {
    List<Assets> findAllByKoreanNameContaining(String koreanName);

    List<Assets> findAllByNameContainingIgnoreCase(String symbol);

    List<Assets> findAllBySymbolStartingWithIgnoreCase(String symbol);
}
