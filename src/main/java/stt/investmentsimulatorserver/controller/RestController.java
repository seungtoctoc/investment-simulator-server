package stt.investmentsimulatorserver.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.AllArgsConstructor;
import stt.investmentsimulatorserver.domain.Assets;
import stt.investmentsimulatorserver.service.Service;
import stt.investmentsimulatorserver.utils.ApiUtils;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api")
@CrossOrigin(origins = "${CLIENT_URL}")
@AllArgsConstructor
public class RestController {
    private Service service;

    @GetMapping("/find")
    public ApiUtils.ApiResult<Object> findAssets(@RequestParam String keyword, Integer limit) {
        List<Assets> foundAssets = service.findAssets(keyword, limit);

        if (foundAssets == null) {
            return ApiUtils.error(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        if (foundAssets.isEmpty()) {
            return ApiUtils.error(HttpStatus.NOT_FOUND);
        }
        return ApiUtils.success(foundAssets);
    }
}
