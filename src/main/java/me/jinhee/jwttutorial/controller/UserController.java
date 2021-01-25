package me.jinhee.jwttutorial.controller;

import me.jinhee.jwttutorial.dto.UserDto;
import me.jinhee.jwttutorial.entity.User;
import me.jinhee.jwttutorial.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public  ResponseEntity<User> signup(@Valid@RequestBody UserDto userDto){
        return ResponseEntity.ok(userService.signup(userDto));
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER','ADMIN')") //@PreAuthorize를 통해 user와 admin 두가지 권한 모두 호출가능
    public ResponseEntity<User> getMyUserInfo(){
        return ResponseEntity.ok(userService.getMyUserWithAuthorities().get());
    }

    @GetMapping("/user/{usernme}")
    @PreAuthorize("hasAnyRole('ADMIN')")    //admin권한만 호출
    public ResponseEntity<User> getUserInfo(@PathVariable String username){
        return ResponseEntity.ok(userService.getUserWithAuthorities(username).get());
    }







}
