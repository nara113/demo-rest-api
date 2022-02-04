package com.study.demorestapi.accounts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccountService implements UserDetailsService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Account saveAccount(Account account) {
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final Account account = accountRepository.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException(username));
        return new User(account.getEmail(), account.getPassword(), getAuthorities(account.getRoles()));
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<AccountRole> roles) {
        return roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.name())).collect(Collectors.toSet());
    }
}
