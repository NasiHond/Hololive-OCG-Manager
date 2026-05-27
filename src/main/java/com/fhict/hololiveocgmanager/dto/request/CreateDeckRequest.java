package com.fhict.hololiveocgmanager.dto.request;

import com.fhict.hololiveocgmanager.domain.Visibility;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateDeckRequest {
    private String title;
    private String description;
    private Visibility visibility;
    private String deckImageUrl;
}
