package stt.investmentsimulatorserver.service;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DividendHistory {
    private LocalDate date;

    private Double amount;

    private Double dividend;

    private Double totalDividend;
}
