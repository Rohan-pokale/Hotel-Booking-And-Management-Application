package com.hotel.booking.Service;

import com.hotel.booking.Entity.User;
import com.hotel.booking.Exception.ResourceNotFoundException;
import com.hotel.booking.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class userServiceImpl implements userService, UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public User getUserByid(Long id) {
        return userRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("User not found for id :"+id));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElseThrow(()->new ResourceNotFoundException("use not found for this emial :"+username));
    }
}
