package com.diplom.diplom.misc.utils;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.entity.EntRole;
import com.diplom.diplom.entity.EntUser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.http.MediaType;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class Parser {
    private static final Safelist text_format_safelist = new Safelist().addTags("span").addAttributes("span", "class", "style");

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

    public static String parseXssText(String text){
        Document.OutputSettings settings=new Document.OutputSettings().prettyPrint(false);
        text= Jsoup.clean(text,"", text_format_safelist,settings);
        if(text.contains("&gt;")){
            text=text.replaceAll("(&gt;.*)", "<span class=\"txt-greentext\">$1</span>");
        }
        String newtext= text.replaceAll("\\[strong]", "<span class =\"txt-strong\">")
                .replaceAll("\\[/strong]", "</span>")
                .replaceAll("\\[spoiler]", "<span class=\"txt-spoiler\">")
                .replaceAll("\\[/spoiler]", "</span>")
                .replaceAll("\\[cursive]", "<span class=\"txt-cursive\">")
                .replaceAll("\\[/cursive]", "</span>")
                .replaceAll("(https?://[^\\s\"<>]+)","<a href=\"$1\" target=\"_blank\">$1</a>");
        Document doc=Jsoup.parse(newtext);
        doc.outputSettings().prettyPrint(false);
        return doc.body().html();
    }

    public static MediaType parseFileContentType(Path filePath) throws IOException {
        String contentType= Files.probeContentType(filePath);
        return contentType!=null ? MediaType.parseMediaType(contentType): MediaType.APPLICATION_OCTET_STREAM;
    }
}
