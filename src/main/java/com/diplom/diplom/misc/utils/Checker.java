package com.diplom.diplom.misc.utils;

import com.diplom.diplom.entity.EntRole;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.repository.RepRole;
import com.diplom.diplom.repository.RepUser;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public class Checker {
    public static boolean checkUserIdentity(UserDetails userDetails, EntUser supposedUser, RepUser rUser) {
        String username = userDetails.getUsername();
        EntUser user=rUser.findByLogin(username).orElseThrow(()->new RuntimeException("user "+username+" not found"));
        return user.equals(supposedUser);
    }

    public static boolean checkUserIdentity(EntUser supposedUser, EntUser unverifiedUser) {
        return unverifiedUser.equals(supposedUser);
    }

    public static boolean isUserAdmin(EntUser user){
        List<String> roles=user.getRoles().stream().map(EntRole::getName).toList();
        return roles.contains("ROLE_ADMIN");
    }

    public static boolean isUserMorePowerful(UserDetails userDetails, String supposedrole, RepUser rUser, RepRole rRole) {
        String username = userDetails.getUsername();
        EntUser user=rUser.findByLogin(username).orElseThrow(()->new RuntimeException("user "+username+" not found"));
        EntRole role=rRole.findByName(supposedrole).orElseThrow(()->new RuntimeException("role "+supposedrole+" not found"));
        int maxrole=Parser.parseUserRolesMaxPower(user);
        return maxrole>=role.getPower();
    }

    public static boolean isUserMorePowerful(EntUser user, int supposedpower) {
        int maxrole=Parser.parseUserRolesMaxPower(user);
        return maxrole>=supposedpower;
    }

    public static boolean isUserMorePowerful(EntUser checkedUser, EntUser supposedUser, boolean strictComparsion) {
        int maxrole1=Parser.parseUserRolesMaxPower(checkedUser);
        int maxrole2=Parser.parseUserRolesMaxPower(supposedUser);
        return strictComparsion ? maxrole1>maxrole2 : maxrole1>=maxrole2;
    }

    public static boolean isUserHasAnyRole(EntUser user, String[] supposedRole) {
        List<String> roles=user.getRoles().stream().map(EntRole::getName).toList();
        if(!roles.isEmpty()){
            for(String role:roles){
                for(String supposedRoleName:supposedRole) {
                    if (role.equals(supposedRoleName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean isUserHasRole(UserDetails userDetails, RepUser rUser) {
        EntUser user=rUser.findByLogin(userDetails.getUsername()).orElseThrow(()->new RuntimeException("user "+userDetails.getUsername()+" not found"));
        return isUserAdmin(user);
    }
}
