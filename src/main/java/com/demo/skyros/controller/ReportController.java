package com.demo.skyros.controller;

import com.demo.skyros.service.ReportService;
import com.demo.skyros.vo.CurrencyReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("send-currency-conversion-report")
    public CurrencyReportVO sendCurrencyConversionReport() {
        return getReportService().sendCurrencyConversionReport();
    }

    @GetMapping("send-currency-exchange-report")
    public CurrencyReportVO sendCurrencyExchangeReport() {
        return getReportService().sendCurrencyExchangeReport();
    }

    public ReportService getReportService() {
        return reportService;
    }

    public void setReportService(ReportService reportService) {
        this.reportService = reportService;
    }
}
