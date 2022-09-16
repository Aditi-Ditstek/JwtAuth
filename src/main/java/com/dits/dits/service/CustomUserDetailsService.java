package com.dits.dits.service;

import com.dits.dits.dto.AuthenticationRequest;
import com.dits.dits.dto.UserDto;
import com.dits.dits.model.User;

public interface CustomUserDetailsService {
    public User save(UserDto userDto);
    public String changePassword(String oldPassword,String confirmPassword, String newPassword, String username);
    public String retrievePassword(String username);

    public User updateProfile(UserDto u);

}
