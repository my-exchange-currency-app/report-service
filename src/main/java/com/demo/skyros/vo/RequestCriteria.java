package com.demo.skyros.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RequestCriteria {

    //@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date from;
    //@DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date to;

}
