package com.diplom.diplom.service.security;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.repository.RepBanlist;
import com.diplom.diplom.repository.RepUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ServiceDiplomUserDetails implements UserDetailsService {
    private final RepUser rUser;
    private final RepBanlist rBanlist;

    @Autowired
    public ServiceDiplomUserDetails(RepUser rUser, RepBanlist rBanlist) {
        this.rUser = rUser;
        this.rBanlist = rBanlist;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        EntUser user = rUser.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь "+login+" не найден"));
        boolean isBanned = rBanlist.existsByUserId(user);
        user.getRoles().size();
        return new DiplomUserDetails(user, isBanned);
    }
}
