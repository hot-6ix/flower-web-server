package com.web.flower.security.config.auth;

import com.web.flower.domain.user.entity.User;
import com.web.flower.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service("userDetailsService")
@RequiredArgsConstructor
public class PrincipalUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        System.out.println("=== PrincipalUserDetailsService : loadUserByUsername() =========");
        Optional<User> byUsername = userRepository.findByUsername(username);
        if(!byUsername.isPresent()){
            throw new UsernameNotFoundException("해당 유저 이름(이메일)이 존재하지 않습니다.");
        }
        User user = byUsername.get();
//        List<GrantedAuthority> roles = new ArrayList<>();
//        roles.add(new SimpleGrantedAuthority(user.getRole()));

        return new PrincipalDetails(user);
    }
}