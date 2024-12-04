package stt.investmentsimulatorserver.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import stt.investmentsimulatorserver.domain.Price;

@Repository
public interface PriceJpaRepository extends JpaRepository<Price, Integer> {
    List<Price> findAllBySymbol(String symbol);
}
