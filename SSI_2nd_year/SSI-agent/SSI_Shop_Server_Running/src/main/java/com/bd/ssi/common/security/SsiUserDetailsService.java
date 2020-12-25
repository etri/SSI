package com.bd.ssi.common.security;

import com.bd.ssi.auth.User;
import com.bd.ssi.auth.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SsiUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> optional = userRepository.findById(username);
        if(optional.isPresent()){
            User user = optional.get();
            return new SsiUserDetails(user.getUsername(), user.getPassword(), AuthorityUtils.createAuthorityList(user.getRole()));
        } else
            throw new UsernameNotFoundException("유저를 찾을 수 없습니다.");
    }
}
