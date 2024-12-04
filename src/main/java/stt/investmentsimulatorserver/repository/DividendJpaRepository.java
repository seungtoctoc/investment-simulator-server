package stt.investmentsimulatorserver.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import stt.investmentsimulatorserver.domain.Dividend;
import stt.investmentsimulatorserver.domain.Price;

public interface DividendJpaRepository extends JpaRepository<Dividend, Integer> {
    List<Dividend> findAllBySymbol(String symbol);
}
