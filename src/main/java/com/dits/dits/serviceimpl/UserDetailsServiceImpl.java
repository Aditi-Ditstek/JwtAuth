package com.dits.dits.serviceimpl;

import com.dits.dits.dto.AuthenticationResponse;
import com.dits.dits.dto.UserDto;
import com.dits.dits.model.RefreshToken;
import com.dits.dits.model.Role;
import com.dits.dits.model.User;
import com.dits.dits.repository.JwtUserRepository;
import com.dits.dits.repository.PrivilegeRepository;
import com.dits.dits.repository.RefreshTokenRepository;
import com.dits.dits.repository.RoleRepository;
import com.dits.dits.service.CustomUserDetailsService;
import com.dits.dits.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
public class UserDetailsServiceImpl implements UserDetailsService, CustomUserDetailsService {

    @Autowired
    JwtUserRepository jwtUserRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PrivilegeRepository privilegeRepository;

    @Autowired
    RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder bCryptEncoder;

    @Autowired
    private JwtUtil jwtUtil;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        List<SimpleGrantedAuthority> roles = null;
                User jwtUser = jwtUserRepository.findByUsername(username);
           if(!(jwtUser == null)) {
//               roles = Arrays.asList(new SimpleGrantedAuthority(roleRepository.findByUsers(jwtUser).getName().toString()));
           return new org.springframework.security.core.userdetails.User(jwtUser.getUsername(), jwtUser.getPassword(), getAuthority(jwtUser));
       }
           else  {
            throw new UsernameNotFoundException("username Not found" + username);
        }
    }


    @Override
    public String changePassword(String oldPassword,String confirmPassword, String newPassword, String username){
        User jwtUser = jwtUserRepository.findByUsername(username);
        System.out.println(bCryptEncoder.matches(oldPassword,jwtUser.getPassword()));
        if(!(jwtUser==null) && bCryptEncoder.matches(oldPassword,jwtUser.getPassword())){
            boolean passwordCompare = comparePasswords(oldPassword,confirmPassword,newPassword);
           if(passwordCompare){
               jwtUser.setPassword(bCryptEncoder.encode(newPassword));
               jwtUser =  jwtUserRepository.save(jwtUser);
                return "Successfully Updated";
           }
           else{
               return "Kindly check Password";
           }
        }
        return "Enter correct current Password";
    }

    private boolean comparePasswords(String oldPassword, String confirmPassword, String newPassword) {
        if(oldPassword!= newPassword){
            if(newPassword.equals(confirmPassword)){
                return true;
            }
            return false;
        }

            return false;
    }
    private Set<SimpleGrantedAuthority> getAuthority(User user) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        user.getRoles().forEach(role -> {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
        });
        return authorities;
    }


    public User save(UserDto userDto){
        User u = userDto.getUserFromDto();
      u.setPassword(bCryptEncoder.encode(u.getPassword()));
         Set<Role> roleSet = new HashSet<>();
        if(userDto.getRoles().size()>0)
        {
            for (String r : userDto.getRoles()) {
                Role role = roleRepository.findByName(r);
                roleSet.add(role);
            }
        }
        else {
            roleSet.add(roleRepository.findByName("DEFAULT"));
        }
         u.setRoles(roleSet);
         return jwtUserRepository.save(u);
    }

    @Override
    public String retrievePassword(String username) {
        User u= jwtUserRepository.findByUsername(username);
        return u.getPassword();
    }

    @Override
    public User updateProfile(UserDto u) {
       User user = u.getUserFromDto();
        User userData = jwtUserRepository.findByUsername(user.getUsername());
        if(userData!=null)
        {
            userData.setUsername(user.getUsername());
            userData.setFullName(user.getFullName());
            userData.setRoles(user.getRoles());
            userData.setEnabled(user.isEnabled());
        }
       return jwtUserRepository.save(userData);
    }

    public AuthenticationResponse generateToken(UserDetails userDetails){
        final String jwt = jwtUtil.generateToken(userDetails);
        final String jwtRefreshToken = jwtUtil.doGenerateRefreshToken();

       RefreshToken rt= getRefreshTokenData(jwtRefreshToken,userDetails.getUsername());
       return new AuthenticationResponse(jwt,jwtRefreshToken);
    }

    private RefreshToken getRefreshTokenData(String token, String username){
        User userData = jwtUserRepository.findByUsername(username);
        RefreshToken rt =new RefreshToken();
        rt.setRefreshToken(token);
        rt.setUserId(userData.getId());
        rt.setExpirationDate(new Date(System.currentTimeMillis() + 30 * 24 * 1000 * 60 * 60));
        return refreshTokenRepository.save(rt);

    }

    public RefreshToken validateRefreshToken(String token){
        RefreshToken rt=refreshTokenRepository.findByRefreshToken(token);
        int userId = rt.getUserId();
        if(rt!=null){
            refreshTokenRepository.deleteById(rt.getId());
        }
        rt =new RefreshToken();
        rt.setRefreshToken(token);
        rt.setUserId(userId);
        rt.setExpirationDate(new Date(System.currentTimeMillis() + 30 * 24 * 1000 * 60 * 60));
        return refreshTokenRepository.save(rt);
    }
}
