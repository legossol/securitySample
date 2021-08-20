package kr.legossol.sampleSecurity.service;

import kr.legossol.sampleSecurity.Entity.Role;
import kr.legossol.sampleSecurity.Entity.User;
import kr.legossol.sampleSecurity.dto.UserDto;
import kr.legossol.sampleSecurity.repository.UserRepository;
import kr.legossol.sampleSecurity.util.SecurityUtil;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User signup(UserDto userDto){
        if(userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null)!=null){
            throw new RuntimeException("이미 존재하는 아이디입니다.");
        }

        Role role = Role.ROLE_USER;

        User user = User.builder()
                .username(userDto.getUsername())
                .password(passwordEncoder.encode(userDto.getPassword()))
                .nickname(userDto.getNickname())
                .role(role)
                .activated(true)
                .build();
        return userRepository.save(user);
    }
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities(String username){
        return userRepository.findOneWithAuthoritiesByUsername(username);
    }
    @Transactional(readOnly = true)
    public Optional<User> getUserWithAuthorities(){
        return SecurityUtil.getCurrentUsername().flatMap(userRepository::findOneWithAuthoritiesByUsername);
    }



}
