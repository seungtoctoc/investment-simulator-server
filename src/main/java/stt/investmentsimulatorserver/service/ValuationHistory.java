package stt.investmentsimulatorserver.service;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ValuationHistory {
    private LocalDate date;

    private Double valuation;
}
