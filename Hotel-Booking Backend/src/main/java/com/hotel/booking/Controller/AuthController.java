package com.hotel.booking.Controller;

import com.hotel.booking.Dto.loginRequestDto;
import com.hotel.booking.Dto.loginResponseDto;
import com.hotel.booking.Dto.signUpRequestDto;
import com.hotel.booking.Dto.signUpResponseDto;
import com.hotel.booking.Security.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AuthorizationServiceException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;


@RequiredArgsConstructor
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signUp")
    public ResponseEntity<signUpResponseDto> signUp(@RequestBody signUpRequestDto signUpDto){
        signUpResponseDto signUpResponseDto = authService.signUp(signUpDto);

        return new ResponseEntity<>(signUpResponseDto, HttpStatus.CREATED);
    }


    @PostMapping("/login")
    public ResponseEntity<loginResponseDto> login(@RequestBody loginRequestDto loginDto, HttpServletRequest request, HttpServletResponse response){
        String[] tokens = authService.login(loginDto);

        Cookie cookie=new Cookie("refreshToken",tokens[1]);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);

        return new ResponseEntity<>(new loginResponseDto(tokens[0]),HttpStatus.CREATED);
    }

    @PostMapping("/refresh")
    public ResponseEntity<loginResponseDto> refresh(HttpServletRequest request){
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(cookie -> cookie.getValue())
                .orElseThrow(() -> new AuthenticationServiceException("refresh Token not found in cookies."));

        String accessToken=authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new loginResponseDto(accessToken));
    }
}
