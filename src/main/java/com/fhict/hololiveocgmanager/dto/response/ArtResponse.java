package com.fhict.hololiveocgmanager.dto.response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArtResponse {
    private Integer id;
    private String name;
    private String effect;
    private Integer damage;
    private String critColourName;
    private List<ArtcostResponse> costs;
}
