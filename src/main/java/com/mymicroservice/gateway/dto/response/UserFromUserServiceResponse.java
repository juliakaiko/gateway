package com.mymicroservice.gateway.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class UserFromUserServiceResponse implements Serializable {

    private Long userId;
    private String name;
    private String surname;
    private LocalDate birthDate;
    private String email;
}
