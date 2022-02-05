package com.study.demorestapi.accounts;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public class AccountAdapter extends User {

    private Account account;

    public AccountAdapter(Account account) {
        super(account.getEmail(), account.getPassword(), getAuthorities(account.getRoles()));
        this.account = account;
    }

    private static Collection<? extends GrantedAuthority> getAuthorities(Set<AccountRole> roles) {
        return roles.stream().map(r -> new SimpleGrantedAuthority("ROLE_" + r.name())).collect(Collectors.toSet());
    }

    public Account getAccount() {
        return account;
    }
}
