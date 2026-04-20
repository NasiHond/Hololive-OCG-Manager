package com.fhict.hololiveocgmanager.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
	private Integer Id;
	private String username;
	private String email;
	private String passwordHash;
	private String bio;
	private String profileImageURL;

	public boolean isValidForCreate() {
		return username != null && !username.isEmpty() &&
				email != null && !email.isEmpty() &&
				passwordHash != null && !passwordHash.isEmpty();
	}
}
