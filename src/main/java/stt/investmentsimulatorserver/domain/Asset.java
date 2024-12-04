package stt.investmentsimulatorserver.domain;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Asset {
    @Id
    private Integer id;

    private String symbol;

    private String type;

    private String exchange;

    private String name;

    private String koreanName;

    private Long marketCap;
}
