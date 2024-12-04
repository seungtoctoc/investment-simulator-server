package stt.investmentsimulatorserver.domain;

import java.time.LocalDate;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Dividend {
    @Id
    Integer id;

    private String symbol;

    private LocalDate date;

    private Double dividend;
}
