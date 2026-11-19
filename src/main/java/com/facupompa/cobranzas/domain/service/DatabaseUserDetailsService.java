package com.facupompa.cobranzas.domain.service;

import com.facupompa.cobranzas.persistence.entity.UserEntity;
import com.facupompa.cobranzas.persistence.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public DatabaseUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username){
        UserEntity user = userRepository.findByUsername(username).orElseThrow(() -> {
           return new UsernameNotFoundException("Usuario no encontrado");
        });

        return User.builder()
                .username(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();
    }
}
