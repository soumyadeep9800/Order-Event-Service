package com.ecommerce.orderevent.dtos;

import lombok.Data;

@Data
public class UserRequestDto {
    private String name;
    private String email;
    private String password;
}
