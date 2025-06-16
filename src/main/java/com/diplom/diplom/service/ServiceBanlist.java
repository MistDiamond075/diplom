package com.diplom.diplom.service;

import com.diplom.diplom.entity.EntBanlist;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.repository.RepBanlist;
import com.diplom.diplom.repository.RepUser;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceBanlist {
    private final RepBanlist rBanlist;
    private final RepUser rUser;

    public ServiceBanlist(RepBanlist rBanlist, RepUser rUser) {
        this.rBanlist = rBanlist;
        this.rUser = rUser;
    }

    public List<EntBanlist> getBanlist() {
        return rBanlist.findAll();
    }

    public EntBanlist getBan(Long id) throws EntityException {
        return rBanlist.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "ban with id "+id+" not found",
                "Бан не найден",
                EntBanlist.class
        ));
    }

    public EntBanlist getBanByBannedUsername(String username) throws EntityException {
        EntUser banned=rUser.findByLogin(username).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user with username "+username+" not found",
                "Пользователь не найден",
                EntUser.class
        ));
        return rBanlist.findByBannedBy(banned).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user with username "+username+" ban not found",
                "Бан не найден",
                EntBanlist.class
        ));
    }

    public EntBanlist addBan(EntBanlist banlist, Long userId, UserDetails userDetails) throws EntityException, AccessException {
        if(userDetails==null){
            throw new AccessException(HttpStatus.BAD_REQUEST,"user details was null","Ошибка проверки данных аккаунта",null);
        }
        EntUser banned=rUser.findById(userId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user with id "+userId+" not found",
                "Пользователь не найден",
                EntUser.class
        ));
        EntUser bannedBy=rUser.findByLogin(userDetails.getUsername()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user with id "+userId+" not found",
                "Ошибка проверки данных аккаунта",
                EntUser.class
        ));
        banlist.setBannedBy(bannedBy);
        banlist.setUserId(banned);
        rBanlist.save(banlist);
        return banlist;
    }

    @Transactional
    public EntBanlist deleteBan(Long id) throws EntityException {
        EntBanlist ban=rBanlist.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "ban with id "+id+" not found",
                "Бан не найден",
                EntBanlist.class
        ));
        rBanlist.delete(ban);
        return ban;
    }

    @Transactional
    public EntBanlist updateBan(EntBanlist banlist, Long id) throws EntityException {
        EntBanlist ban=rBanlist.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "ban with id "+id+" not found",
                "Бан не найден",
                EntBanlist.class
        ));
        ban.setReason(banlist.getReason());
        ban.setEnd(banlist.getEnd());
        ban.setIpaddress(banlist.getIpaddress());
        rBanlist.save(ban);
        return ban;
    }
}
