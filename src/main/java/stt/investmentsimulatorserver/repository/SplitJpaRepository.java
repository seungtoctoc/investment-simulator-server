package stt.investmentsimulatorserver.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import stt.investmentsimulatorserver.domain.Price;
import stt.investmentsimulatorserver.domain.Split;

public interface SplitJpaRepository extends JpaRepository<Split, Integer> {
    List<Split> findAllBySymbol(String symbol);
}
