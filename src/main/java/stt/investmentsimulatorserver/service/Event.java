package stt.investmentsimulatorserver.service;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import stt.investmentsimulatorserver.domain.Dividend;
import stt.investmentsimulatorserver.domain.Price;
import stt.investmentsimulatorserver.domain.Split;

@AllArgsConstructor
@Getter
public class Event {
    private LocalDate date;

    private String type;

    private Price price;

    private Dividend dividend;

    private Split split;
}
