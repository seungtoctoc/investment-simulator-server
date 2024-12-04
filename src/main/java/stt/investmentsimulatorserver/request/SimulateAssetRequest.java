package stt.investmentsimulatorserver.request;

import java.math.BigInteger;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class SimulateAssetRequest {
    @NotNull
    String symbol;

    @NotNull
    String exchange;

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
