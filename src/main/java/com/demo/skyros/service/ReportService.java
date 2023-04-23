package com.demo.skyros.service;

import com.demo.skyros.proxy.CurrencyMailProxy;
import com.demo.skyros.proxy.LoggingProxy;
import com.demo.skyros.vo.CurrencyExchangeVO;
import com.demo.skyros.vo.CurrencyReportVO;
import com.demo.skyros.vo.RequestCriteria;
import com.demo.skyros.vo.RequestVO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Setter
@Getter
public class ReportService {

    @Autowired
    private LoggingProxy loggingProxy;

    @Autowired
    private CurrencyMailProxy currencyMailProxy;

    private GsonBuilder builder = new GsonBuilder();

    private Gson gson = builder.create();

    @Value("${report.hours}")
    private int hours;

    @Scheduled(cron = "${currency.conversion.report.cron}")
    public void sendCurrencyConversionReport() {
        CurrencyReportVO currencyReportVO = prepareCurrencyConversionReportData();
        List<CurrencyExchangeVO> list = currencyReportVO.getCurrencyExchangeVOList();
        if (!list.isEmpty()) {
            getCurrencyMailProxy().transactionsReport(currencyReportVO);
        }
    }

    @Scheduled(cron = "${currency.inquiry.report.cron}")
    public void sendCurrencyExchangeReport() {
        CurrencyReportVO currencyReportVO = prepareCurrencyExchangeReportData();
        List<CurrencyExchangeVO> list = currencyReportVO.getCurrencyExchangeVOList();
        if (!list.isEmpty()) {
            getCurrencyMailProxy().inquiryReport(currencyReportVO);
        }
    }

    public CurrencyReportVO prepareCurrencyConversionReportData() {
        Map<Map<String, String>, BigDecimal> currencyConversionRequests = prepareCurrencyConversionRequests(prepareClientRequest("conversion"));
        CurrencyReportVO currencyReportVO = prepareCurrencyReportVO(currencyConversionRequests);
        return currencyReportVO;
    }

    public CurrencyReportVO prepareCurrencyExchangeReportData() {
        Map<Map<String, String>, BigDecimal> currencyConversionRequests = prepareCurrencyExchangeRequests(prepareClientRequest("exchange"));
        CurrencyReportVO currencyReportVO = prepareCurrencyReportVO(currencyConversionRequests);
        return currencyReportVO;
    }

    private CurrencyReportVO prepareCurrencyReportVO(Map<Map<String, String>, BigDecimal> currencyConversionRequests) {
        CurrencyReportVO currencyReportVO = new CurrencyReportVO();
        List<CurrencyExchangeVO> currencyExchangeVOList = new ArrayList<>();
        currencyConversionRequests.forEach((currency, quantity) -> {
            CurrencyExchangeVO currencyExchangeVO = new CurrencyExchangeVO();
            Map.Entry<String, String> next = currency.entrySet().iterator().next();
            currencyExchangeVO.setFrom(next.getKey());
            currencyExchangeVO.setTo(next.getValue());
            currencyExchangeVO.setQuantity(quantity);
            currencyExchangeVOList.add(currencyExchangeVO);
        });
        currencyReportVO.setCurrencyExchangeVOList(currencyExchangeVOList);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -hours);
        Date fromDate = cal.getTime();
        currencyReportVO.setFrom(fromDate);
        currencyReportVO.setTo(new Date());
        return currencyReportVO;
    }

    public List<RequestVO> prepareClientRequest(String tag) {
        Date toDate = formatDate(new Date());
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -hours);
        Date fromDate = formatDate(cal.getTime());
        RequestCriteria requestCriteria = new RequestCriteria(cal.getTime(), new Date());
        List<RequestVO> requestEntities = getLoggingProxy().findClientRequestPerDate(requestCriteria);
        return prepareClientRequestByTag(requestEntities, tag);
    }

    private Date formatDate(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(format.format(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }


    public List<RequestVO> prepareClientRequestByTag(List<RequestVO> clientRequestList, String tag) {
        if (null != clientRequestList && !clientRequestList.isEmpty()) {
            return clientRequestList.stream().filter(clientRequestEntity -> tag.equals(clientRequestEntity.getTag())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public Map<Map<String, String>, BigDecimal> prepareCurrencyConversionRequests(List<RequestVO> clientRequestList) {
        Map<Map<String, String>, BigDecimal> currencyConversionMap = new HashMap<>();
        clientRequestList.forEach(clientRequest -> {
            CurrencyExchangeVO currencyExchangeVO = getGson().fromJson(clientRequest.getRequestBody(), CurrencyExchangeVO.class);
            String from = currencyExchangeVO.getFrom();
            String to = currencyExchangeVO.getTo();
            BigDecimal quantity = currencyExchangeVO.getQuantity();
            Map<String, String> map = new HashMap<>();
            map.put(from, to);
            if (null == currencyConversionMap.get(map)) {
                currencyConversionMap.put(map, quantity);
            } else {
                BigDecimal totalQuantity = currencyConversionMap.get(map);
                totalQuantity = totalQuantity.add(quantity);
                currencyConversionMap.put(map, totalQuantity);
            }
        });
        return currencyConversionMap;
    }

    public Map<Map<String, String>, BigDecimal> prepareCurrencyExchangeRequests(List<RequestVO> clientRequestList) {
        Map<Map<String, String>, BigDecimal> currencyExchangeMap = new HashMap<>();
        clientRequestList.forEach(clientRequest -> {
            CurrencyExchangeVO currencyExchangeVO = getGson().fromJson(clientRequest.getRequestBody(), CurrencyExchangeVO.class);
            String from = currencyExchangeVO.getFrom();
            String to = currencyExchangeVO.getTo();
            Map<String, String> map = new HashMap<>();
            map.put(from, to);
            if (null == currencyExchangeMap.get(map)) {
                currencyExchangeMap.put(map, new BigDecimal(1));
            } else {
                BigDecimal totalQuantity = currencyExchangeMap.get(map);
                totalQuantity = totalQuantity.add(new BigDecimal(1));
                currencyExchangeMap.put(map, totalQuantity);
            }
        });
        return currencyExchangeMap;
    }
}
