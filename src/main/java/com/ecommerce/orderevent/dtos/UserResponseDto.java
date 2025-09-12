package com.ecommerce.orderevent.dtos;

import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;

    public UserResponseDto(Long id, String name, String email){
        this.id = id;
        this.name = name;
        this.email = email;
    }
}
