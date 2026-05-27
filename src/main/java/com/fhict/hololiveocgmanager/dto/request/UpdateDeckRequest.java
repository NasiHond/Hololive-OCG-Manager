package com.fhict.hololiveocgmanager.dto.request;

import com.fhict.hololiveocgmanager.domain.Visibility;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDeckRequest
{
    private String title;
    private String description;
    private Visibility visibility;
    private String deckImageUrl;
}
