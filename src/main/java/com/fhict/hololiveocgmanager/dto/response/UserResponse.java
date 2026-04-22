package com.fhict.hololiveocgmanager.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Integer ID;
    private String username;
    private String email;
    private String bio;
//    private String profileImageURL;
}
