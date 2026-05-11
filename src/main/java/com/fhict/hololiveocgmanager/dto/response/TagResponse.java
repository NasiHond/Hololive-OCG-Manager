package com.fhict.hololiveocgmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class TagResponse {
    private Integer id;
    private String name;
}
