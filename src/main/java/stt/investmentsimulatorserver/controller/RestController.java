package stt.investmentsimulatorserver.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import stt.investmentsimulatorserver.domain.Asset;
import stt.investmentsimulatorserver.request.SimulateAssetRequest;
import stt.investmentsimulatorserver.response.SimulateAssetResponse;
import stt.investmentsimulatorserver.service.Service;
import stt.investmentsimulatorserver.utils.ApiUtils;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${CLIENT_URL}")
@AllArgsConstructor
public class RestController {
    private Service service;

    @GetMapping("/find")
    public ApiUtils.ApiResult<List<Asset>> findAssets(@RequestParam String keyword, Integer limit) {
        List<Asset> foundAssets = service.findAssets(keyword, limit);

        if (foundAssets == null) {
            return ApiUtils.error(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (foundAssets.isEmpty()) {
            return ApiUtils.error(HttpStatus.NOT_FOUND);
        }
        return ApiUtils.success(foundAssets);
    }

    @PostMapping("/simulate")
    public ApiUtils.ApiResult<Object> simulateAsset(@RequestBody @Valid SimulateAssetRequest simulateAssetRequest) {
        SimulateAssetResponse simulateAssetResponse = service.simulateAsset(simulateAssetRequest);

        if (simulateAssetResponse == null) {
            return ApiUtils.error(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        
        return ApiUtils.success(simulateAssetResponse);
    }
}
