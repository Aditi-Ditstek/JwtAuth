package com.dits.dits.dto;

import com.dits.dits.model.User;
import lombok.Data;

import java.util.Set;

@Data
public class UserDto {

    private String fullName;
    private String username;
    private String password;
    private boolean enabled;
    private Set<String> roles;
    public User getUserFromDto(){
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setFullName(fullName);

        return user;

    }

}
