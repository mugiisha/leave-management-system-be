package org.amir.leavemanagementsystem.service;

import lombok.RequiredArgsConstructor;
import org.amir.leavemanagementsystem.model.User;
import org.amir.leavemanagementsystem.model.UserStatus;
import org.amir.leavemanagementsystem.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
        
        // If the user is not approved, we still load the user details
        // The actual check for approval happens in the AuthenticationService
        // This allows us to provide a more specific error message
        
        return user;
    }
} 