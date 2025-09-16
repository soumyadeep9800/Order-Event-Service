package com.ecommerce.orderevent.dtos;

import com.ecommerce.orderevent.entity.User;
import lombok.Data;

@Data
public class UserResponseDto {
    private Long id;
    private String name;
    private String email;

    public static UserResponseDto fromEntity(User user) {
        UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setId(user.getId());
        userResponseDto.setName(user.getName());
        userResponseDto.setEmail(user.getEmail());

        return userResponseDto;
    }
}
