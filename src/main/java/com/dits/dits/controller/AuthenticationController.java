package com.dits.dits.controller;

import com.dits.dits.dto.AuthenticationRequest;
import com.dits.dits.dto.AuthenticationResponse;
import com.dits.dits.dto.EmailDetails;
import com.dits.dits.dto.UserDto;
import com.dits.dits.model.RefreshToken;
import com.dits.dits.model.User;
import com.dits.dits.service.EmailService;
import com.dits.dits.serviceimpl.UserDetailsServiceImpl;
import com.dits.dits.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.impl.DefaultClaims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthenticationController {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserDetailsServiceImpl userDetailsService;

    @Autowired
    private EmailService emailService;

    @Autowired
    JwtUtil jwtUtil;

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
        System.out.println("hello");
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (DisabledException d) {
            throw new Exception("USER_DISABLED", d);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
      /*  final String jwt = jwtUtil.generateToken(userDetails);
        final String jwtRefreshToken = jwtUtil.doGenerateRefreshToken();*/
        AuthenticationResponse ar=userDetailsService.generateToken(userDetails);
        return ResponseEntity.ok(ar);

    }
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> saveUser(@RequestBody UserDto user) throws Exception {
        return ResponseEntity.ok(userDetailsService.save(user));
    }

    @PostMapping("/app/changepassword")
    public ResponseEntity changePassword(@RequestParam String  oldPassword ,@RequestParam String confirmPassword,@RequestParam String newPassword,@RequestHeader(HttpHeaders.AUTHORIZATION) String requestTokenHeader) {
        String username = "";
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            String jwtToken = requestTokenHeader.substring(7);

            try {
                System.out.println(jwtToken);
                username = jwtUtil.extractUsername(jwtToken);
            } catch (IllegalArgumentException e) {
                System.out.println("Unable to get JWT Token");
            } catch (ExpiredJwtException e) {
                System.out.println("JWT Token has expired");
            }
        }
        String result= userDetailsService.changePassword(oldPassword, confirmPassword, newPassword, username);
       return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/forgetPassword")
    public String retrievePassword(@RequestParam String username){
        return userDetailsService.retrievePassword(username);
    }

    @PostMapping("/sendmail")
    public String sendMail(@RequestBody EmailDetails details){
        String status
                = emailService.sendSimpleMail(details);
        return status;

 }
 @PutMapping("/app/editprofile")
    public ResponseEntity editProfile(@RequestBody UserDto user) throws Exception {
     User jwtUser= new User();
     jwtUser.setUsername(user.getUsername());
     jwtUser.setPassword(user.getPassword());
     jwtUser.setFullName(user.getFullName());
     return ResponseEntity.ok(userDetailsService.updateProfile(user)); }

  /*  @RequestMapping(value = "/refreshtoken", method = RequestMethod.GET)
    public ResponseEntity<?> refreshtoken(HttpServletRequest request) throws Exception {
        // From the HttpRequest get the claims
        DefaultClaims claims = (io.jsonwebtoken.impl.DefaultClaims) request.getAttribute("claims");

        Map<String, Object> expectedMap = getMapFromIoJsonwebtokenClaims(claims);
        String token = jwtUtil.doGenerateRefreshToken(expectedMap, expectedMap.get("sub").toString());
        return ResponseEntity.ok(new AuthenticationResponse(token));
    }*/

    @GetMapping(value="/refreshtoken")
    public ResponseEntity refreshToken(@RequestParam String token){
        RefreshToken rt=userDetailsService.validateRefreshToken(token);
        return new ResponseEntity(rt,HttpStatus.OK);
    }

    public Map<String, Object> getMapFromIoJsonwebtokenClaims(DefaultClaims claims) {
        Map<String, Object> expectedMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : claims.entrySet()) {
            expectedMap.put(entry.getKey(), entry.getValue());
        }
        return expectedMap;
    }
}
