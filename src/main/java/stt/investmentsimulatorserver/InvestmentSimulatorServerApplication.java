package stt.investmentsimulatorserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class InvestmentSimulatorServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvestmentSimulatorServerApplication.class, args);
    }

}
