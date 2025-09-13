package com.ecommerce.orderevent.dtos;

import com.ecommerce.orderevent.entity.User;
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

    public static UserResponseDto fromEntity(User user) {
        return new UserResponseDto(user.getId(), user.getName(), user.getEmail());
    }
}
