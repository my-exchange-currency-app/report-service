package com.demo.skyros.proxy;

import com.demo.skyros.vo.CurrencyExchangeVO;
import com.demo.skyros.vo.CurrencyReportVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "currency-mail-service", url = "http://localhost:9999")
public interface CurrencyMailProxy {

    @PostMapping("transaction")
    void sendTransactionMail(@RequestBody CurrencyExchangeVO vo);

    @PostMapping("transactionsReport")
    void transactionsReport(@RequestBody CurrencyReportVO vo);

    @PostMapping("inquiryReport")
    void inquiryReport(@RequestBody CurrencyReportVO vo);

}
