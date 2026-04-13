package com.fhict.hololiveocgmanager.domain;

import com.fhict.hololiveocgmanager.entity.ArtcostEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Art {
    private Integer ID;
    private String name;
    private String effect;
    private Integer damage;
    private String critColourName;
    private List<ArtcostEntity> costs;
}
