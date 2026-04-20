package com.fhict.hololiveocgmanager.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginRequest {
    @NotNull
    private String identifier;
    @NotNull
    private String password;
}
