package com.hotel.booking.Security;

import com.hotel.booking.Dto.loginRequestDto;
import com.hotel.booking.Dto.signUpRequestDto;
import com.hotel.booking.Dto.signUpResponseDto;
import com.hotel.booking.Entity.User;
import com.hotel.booking.Entity.enums.Role;
import com.hotel.booking.Exception.ResourceNotFoundException;
import com.hotel.booking.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public signUpResponseDto signUp(signUpRequestDto signUpRequestDto){
        User user=userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);

        if(user!=null){
            throw new RuntimeException("User is already registred with this email :"+signUpRequestDto.getEmail());
        }

        User newUser=modelMapper.map(signUpRequestDto,User.class);
        newUser.setRoles(Set.of(Role.GUEST));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        newUser=userRepository.save(newUser);

        return modelMapper.map(newUser, signUpResponseDto.class);
    }

    public String[] login(loginRequestDto loginRequestDto){

        Authentication authentication=authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getEmail(),loginRequestDto.getPassword()
                ));

        User user=(User) authentication.getPrincipal();

        String arr[]=new String[2];

        arr[0]= jwtService.generateAccessToken(user);
        arr[1]=jwtService.generateRefreshToken(user);

        return arr;
    }

    public String refreshToken(String refreshToken){
        Long id= jwtService.getUserIdFromToken(refreshToken);
        User user=userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found for this id:"+id));

        return jwtService.generateAccessToken(user);

    }




}
