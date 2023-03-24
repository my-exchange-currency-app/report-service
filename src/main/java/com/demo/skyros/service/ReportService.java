package com.demo.skyros.service;

import com.demo.skyros.entity.ClientRequestEntity;
import com.demo.skyros.vo.CurrencyExchangeVO;
import com.demo.skyros.vo.CurrencyReportVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private ClientRequestService clientRequestService;

    @Value("${report.hours}")
    private int hours;

    public CurrencyReportVO sendCurrencyConversionReport() {
        Map<Map<String, String>, BigDecimal> currencyConversionRequests = prepareCurrencyConversionRequests(prepareClientRequest("conversion"));
        CurrencyReportVO currencyReportVO = prepareCurrencyReportVO(currencyConversionRequests);
        return currencyReportVO;
    }

    public CurrencyReportVO sendCurrencyExchangeReport() {
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
        currencyReportVO.setCurrencyExchangeVO(currencyExchangeVOList);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -hours);
        Date fromDate = cal.getTime();
        currencyReportVO.setFrom(fromDate);
        currencyReportVO.setTo(new Date());
        return currencyReportVO;
    }

    public List<ClientRequestEntity> prepareClientRequest(String tag) {
        Date toDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR_OF_DAY, -hours);
        Date fromDate = cal.getTime();
        List<ClientRequestEntity> requestEntities = getClientRequestService().findClientRequestPerDate(fromDate, toDate);
        return prepareClientRequestByTag(requestEntities, tag);
    }


    public List<ClientRequestEntity> prepareClientRequestByTag(List<ClientRequestEntity> clientRequestList, String tag) {
        if (null != clientRequestList && !clientRequestList.isEmpty()) {
            return clientRequestList.stream().filter(clientRequestEntity -> tag.equals(clientRequestEntity.getTag())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

    public Map<Map<String, String>, BigDecimal> prepareCurrencyConversionRequests(List<ClientRequestEntity> clientRequestList) {
        Map<Map<String, String>, BigDecimal> currencyConversionMap = new HashMap<>();
        clientRequestList.forEach(clientRequest -> {
            CurrencyExchangeVO currencyExchangeVO = getClientRequestService().getGson().fromJson(clientRequest.getRequestBody(), CurrencyExchangeVO.class);
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

    public Map<Map<String, String>, BigDecimal> prepareCurrencyExchangeRequests(List<ClientRequestEntity> clientRequestList) {
        Map<Map<String, String>, BigDecimal> currencyExchangeMap = new HashMap<>();
        clientRequestList.forEach(clientRequest -> {
            CurrencyExchangeVO currencyExchangeVO = getClientRequestService().getGson().fromJson(clientRequest.getRequestBody(), CurrencyExchangeVO.class);
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


    public ClientRequestService getClientRequestService() {
        return clientRequestService;
    }

    public void setClientRequestService(ClientRequestService clientRequestService) {
        this.clientRequestService = clientRequestService;
    }
}
