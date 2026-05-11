package com.fhict.hololiveocgmanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtcostResponse {
    private Integer id;
    private Integer amount;
    private String colourName;
    private String colourImageUrl;
}

