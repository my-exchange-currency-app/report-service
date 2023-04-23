package com.demo.skyros.proxy;

import com.demo.skyros.vo.RequestCriteria;
import com.demo.skyros.vo.RequestVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "logging-service", path = "/request")
public interface LoggingProxy {

    @GetMapping("find")
    List<RequestVO> findClientRequestPerDate(@RequestBody RequestCriteria criteria);

    @GetMapping("findAll")
    List<RequestVO> findAll();

}
