package stt.investmentsimulatorserver.service;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PurchaseHistory {
    private LocalDate date;

    private Double price;

    private Long amount;

    private Double totalAmount;
}
