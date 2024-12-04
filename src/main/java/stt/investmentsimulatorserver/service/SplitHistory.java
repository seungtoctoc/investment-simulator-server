package stt.investmentsimulatorserver.service;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SplitHistory {
    private LocalDate date;

    private Double beforeAmount;

    private Double afterAmount;
}
