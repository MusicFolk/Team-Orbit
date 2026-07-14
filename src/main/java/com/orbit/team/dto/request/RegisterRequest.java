package com.orbit.team.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^\\S+$", message = "Username must contain no blank spaces")
    private String username;
    @NotBlank
    @Size(max = 100)
    private String fullName;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @Length(min = 6)
    private String password;
}
