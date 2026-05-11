package com.fhict.hololiveocgmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class KeywordResponse {
    private Integer id;
    private String type;
    private String name;
    private String effect;
}
