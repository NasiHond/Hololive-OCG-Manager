package com.fhict.hololiveocgmanager.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private Integer id;
    private String username;
    private String Message;
    private Boolean Authenticated;
}
