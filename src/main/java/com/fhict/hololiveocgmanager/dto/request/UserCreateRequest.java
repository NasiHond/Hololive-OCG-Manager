package com.fhict.hololiveocgmanager.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreateRequest {
	@NotBlank
	private String username;

	@NotBlank
	private String email;

	@NotBlank
	private String password;
}
