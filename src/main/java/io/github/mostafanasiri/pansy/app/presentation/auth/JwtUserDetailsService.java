package io.github.mostafanasiri.pansy.app.presentation.auth;

import io.github.mostafanasiri.pansy.app.data.repository.jpa.UserJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JwtUserDetailsService implements UserDetailsService {
    @Autowired
    private UserJpaRepository userJpaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var userEntity = userJpaRepository.findByUsername(username) // TODO: Get from UserService
                .orElseThrow(() -> new UsernameNotFoundException(
                        String.format("User with username: %s was not found", username))
                );

        return new AppUserDetails(userEntity);
    }
}
