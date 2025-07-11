package com.bxt.usercenter2.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class PageRequest implements Serializable {


    private static final long serialVersionUID = 2L;

    /**
     * 当前页号，从1开始
     */
    private Long current = 1L;

    /**
     * 每页条数，默认10条
     */
    private Long pageSize = 10L;
}
