package com.mymicroservice.gateway.dto.request;

import com.mymicroservice.gateway.dto.response.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRegistrationRequest implements Serializable {

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 50, message = "Name must be less than 50 characters")
    private String name;

    @NotBlank(message = "Surname cannot be blank")
    @Size(max = 50, message = "Surname must be less than 50 characters")
    private String surname;

    @NotNull(message = "Birth date cannot be null")
    @Past(message = "Birth date must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) //ISO.DATE = yyyy-MM-dd
    private LocalDate birthDate;

    @Email(regexp="[\\w.]+@\\w+\\.\\w+", message="Please provide a valid email address") // [a-zA-Z0-9_] _ .
    @NotBlank(message = "Email address may not be blank")
    private String email;

    @NotBlank (message = "Password may not be blank")
    @Size(min=5, max=255, message = "Password size must be between 5 and 255")
    private String password;

    private Role role;
}
