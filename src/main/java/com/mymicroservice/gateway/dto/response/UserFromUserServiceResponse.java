package com.mymicroservice.gateway.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFromUserServiceResponse implements Serializable {

    private Long userId;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
}
