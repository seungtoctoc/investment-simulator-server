package stt.investmentsimulatorserver.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import stt.investmentsimulatorserver.utils.ApiUtils;

@org.springframework.web.bind.annotation.RestController
@RequestMapping("/api")
public class RestController {
    @GetMapping("/test")
    public ApiUtils.ApiResult<Object> test() {
        return ApiUtils.success("success");
    }

    @GetMapping("/test2")
    public String test2() {
        return "success";
    }
}
