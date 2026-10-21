package com.careerhub.identity.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

   /* @GetMapping("/me")
    public Map<String, Object> me(
            @AuthenticationPrincipal Jwt jwt) {

        return Map.of(
                "userId", jwt.getSubject(),
                "email", jwt.getClaimAsString("email"),
                "roles", jwt.getClaimAsStringList("roles")
        );*/
   @GetMapping("/me")
   public Map<String, Object> me(
           @AuthenticationPrincipal Jwt jwt) {

       if (jwt == null) {
           throw new ResponseStatusException(
                   HttpStatus.UNAUTHORIZED,
                   "Valid JWT authentication is required"
           );
       }

       return Map.of(
               "userId", jwt.getSubject(),
               "email", jwt.getClaimAsString("email"),
               "roles", jwt.getClaimAsStringList("roles") != null
                       ? jwt.getClaimAsStringList("roles")
                       : java.util.List.of()
       );
    }
}
