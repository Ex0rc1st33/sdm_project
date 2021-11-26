package com.example.ui.controllers;

import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import com.example.movie_management.movie.watchlater.WatchLaterService;
import com.example.ui.requests.AuthenticationRequest;
import com.example.ui.requests.ModifyWatchLaterRequest;
import com.example.ui.requests.ResetRequest;
import com.example.ui.responses.LoginResponse;
import com.example.user_management_system.registration.RegistrationService;
import com.example.user_management_system.registration.Request;
import com.example.user_management_system.security.jwt.JWTTokenHelper;
import com.example.user_management_system.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import static com.example.user_management_system.registration.RegistrationController.isMatchingPassword;


@RestController
@RequestMapping("/api")
@CrossOrigin
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    JWTTokenHelper jWTTokenHelper;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private WatchLaterService watchLaterService;

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest authenticationRequest) throws InvalidKeySpecException,
            NoSuchAlgorithmException {

        UsernamePasswordAuthenticationToken loginToken = new UsernamePasswordAuthenticationToken(authenticationRequest.getEmail(),
                authenticationRequest.getPassword());
        Authentication authenticatedUser = authenticationManager.authenticate(loginToken);
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser);

        User user = (User) authenticatedUser.getPrincipal();

        String jwtToken = jWTTokenHelper.generateToken(user.getEmail());

        LoginResponse response = new LoginResponse();
        response.setToken(jwtToken);
        System.out.println(jwtToken);
        return ResponseEntity.ok(response);
    }

    @PostMapping(path = "/register")
    public ResponseEntity<?> postRegistration(@RequestBody Request request) throws IllegalAccessException {
        System.out.println(request.getFirstName());
        if (request.checkAnyNull()) {
            return ResponseEntity.ok().body("Fill out all the fields");
        }

        if (!isMatchingPassword(request.getPassword(), request.getPasswordConfirm())) {
            return ResponseEntity.ok().body("Passwords not matching");
        }
        return ResponseEntity.ok().body(registrationService.registration(request));
    }

    @GetMapping(path = "/userinfo")
    public ResponseEntity<?> getUserInfo(Principal user) {
        User userObj = (User) userDetailsService.loadUserByUsername(user.getName());
        return ResponseEntity.ok(userObj);
    }

    @PostMapping(path = "/watchlater")
    public ResponseEntity<?> modifyWatchLater(Principal user, @RequestBody ModifyWatchLaterRequest modifyWatchLaterRequest) {
        User userObj = (User) userDetailsService.loadUserByUsername(user.getName());
        System.out.println(userObj.getEmail());
        String userEmail= userObj.getEmail();
        String action = modifyWatchLaterRequest.getAction();
        System.out.println(modifyWatchLaterRequest.getAction());

        if(action.equals("GET_LIST")){
            List<Integer> watchLaterList = watchLaterService.getWatchLaterList(userEmail);
            return ResponseEntity.ok(watchLaterList);
        }
        String movie =modifyWatchLaterRequest.getMovie_id();
        System.out.println(movie);

        int movie_id = Integer.parseInt(movie);
        if(action.equals("ADD")) {
            watchLaterService.addToList(userObj.getEmail(), movie_id);
            return ResponseEntity.ok(movie_id+" was added to WatchLater");
        }
        if(action.equals("REMOVE")) {
            watchLaterService.deleteMovieFromWatchLaterList(userEmail, movie_id);
            return ResponseEntity.ok(movie_id+" was removed from WatchLater");
        }

        return ResponseEntity.ok("bad");
    }

    @GetMapping(path = "/logout")
    public ResponseEntity<?> logout() {
        SecurityContextHolder.getContext().setAuthentication(null);
        return ResponseEntity.ok().body("Successfully logged out");
    }

    @PostMapping(path = "/reset")
    public ResponseEntity<?> postResetPassword(@RequestBody ResetRequest resetRequest) {
        if (!isMatchingPassword(resetRequest.getPassword(), resetRequest.getPassword_confirm())) {
            return ResponseEntity.ok().body("Passwords not matching");
        }
        if (registrationService.changePassword(resetRequest.getPassword(), resetRequest.getToken())) {
            return ResponseEntity.ok().body("Successfully changed password");
        }
        return ResponseEntity.ok().body("Cannot change password");
    }



}
