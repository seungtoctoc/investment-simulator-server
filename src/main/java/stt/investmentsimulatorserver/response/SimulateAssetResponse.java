package stt.investmentsimulatorserver.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import stt.investmentsimulatorserver.service.DividendHistory;
import stt.investmentsimulatorserver.service.PurchaseHistory;
import stt.investmentsimulatorserver.service.SplitHistory;
import stt.investmentsimulatorserver.service.ValuationHistory;

@AllArgsConstructor
@Getter
public class SimulateAssetResponse {
    private Double totalValuation;

    private Double totalProfit;

    private Double totalProfitRate;

    private Double totalAmount;

    private Double totalDividend;

    private Double extraCash;

    private List<ValuationHistory> valuationHistory;

    private List<PurchaseHistory> purchaseHistory;

    private List<DividendHistory> dividendHistory;

    private List<SplitHistory> splitHistory;
}
