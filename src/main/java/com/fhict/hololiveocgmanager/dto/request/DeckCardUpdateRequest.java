package com.fhict.hololiveocgmanager.dto.request;

import lombok.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeckCardUpdateRequest {
    @NotNull
    @Positive
    private Integer cardId;

    @NotNull
    @Min(0)
    private Integer count;
}
