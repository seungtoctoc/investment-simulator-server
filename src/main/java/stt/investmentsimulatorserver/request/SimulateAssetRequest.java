package stt.investmentsimulatorserver.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SimulateAssetRequest {
    @NotNull
    String symbol;

    @NotNull
    Integer period;

    @NotNull
    Long seed;

    @NotNull
    Long monthly;

    @NotNull
    Boolean isReinvest;

    @NotNull
    Boolean isDollar;
}
