package com.diplom.diplom.misc.utils;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.entity.EntRole;
import com.diplom.diplom.entity.EntUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class Parser {
    public static String parseUserRole(DiplomUserDetails userDetails) {
        if(userDetails!=null) {
            List<EntRole> roles = userDetails.getUser().getRoles();
            Optional<EntRole> role=roles.stream().max(Comparator.comparingInt(EntRole::getPower));
            return role.map(EntRole::getName).orElse("ROLE_STUDENT");
        }
        return "ROLE_STUDENT";
    }

    public static int parseUserRolesMaxPower(EntUser user) {
        Optional<EntRole> maxrole=user.getRoles().stream().max(Comparator.comparingInt(EntRole::getPower));
        return maxrole.map(EntRole::getPower).orElse(0);
    }

    public static List<String> parseJsonList(String json) throws JsonProcessingException {
        return new ObjectMapper().readValue(json, new TypeReference<>() {});
    }

    public static String parseListJson(List list) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(list);
    }

    public static String parseTimeEnding(Long hours, Long minutes) {
        String newhours="";
        if(hours!=null) {
            long hrs = hours > 20 ? hours % 10 : hours;
            if (hrs == 1) {
                newhours = hours + " час ";
            } else if (hrs > 1 && hrs < 5) {
                newhours = hours + " часа ";
            } else if (hrs > 4 || hrs == 0) {
                newhours = hours + " часов ";
            }
        }
        String newminutes = "";
        if(minutes!=null) {
            long min = minutes / 20 > 0 ? minutes % 10 : minutes;
            if (min == 1) {
                newminutes = minutes + " минуту";
            } else if (min > 1 && min < 5) {
                newminutes = minutes + " минуты";
            } else if (min > 4 || min == 0) {
                newminutes = minutes + " минут";
            }
        }
        return newhours+newminutes;
    }
}
