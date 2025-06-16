package com.diplom.diplom.dto.converter;

import com.diplom.diplom.dto.DTOUserUpdate;
import com.diplom.diplom.entity.EntGroup;
import com.diplom.diplom.entity.EntRole;
import com.diplom.diplom.entity.EntUser;

import java.util.ArrayList;

public class ConverterUserUpdateToUser {
   public static EntUser convertDTOtoUser(DTOUserUpdate dtoUserUpdate) {
        EntUser user = new EntUser();
        user.setId(dtoUserUpdate.getId());
        user.setLogin(dtoUserUpdate.getLogin());
        user.setPassword(dtoUserUpdate.getPassword());
        user.setFirstname(dtoUserUpdate.getFirstname());
        user.setLastname(dtoUserUpdate.getLastname());
        user.setSurname(dtoUserUpdate.getSurname());
        user.setEmail(dtoUserUpdate.getEmail());
        user.setDateofbirth(dtoUserUpdate.getDateofbirth());
        user.setQwestion(EntUser.Qwestion.valueOf(dtoUserUpdate.getQwestion()));
        user.setQwestionanswer(dtoUserUpdate.getQwestionanswer());
        user.setStudentcard(dtoUserUpdate.getStudentcard());
        return user;
    }

    public static DTOUserUpdate convertUserToDTO(EntUser user) {
        return new DTOUserUpdate(
                user.getId(),
                user.getLogin(),
                user.getFirstname(),
                user.getLastname(),
                user.getSurname(),
                user.getDateofbirth(),
                user.getEmail(),
                user.getQwestion().toString(),
                user.getQwestionanswer(),
                user.getStudentcard(),
                user.getGroups().stream().map(EntGroup::getName).toList(),
                new ArrayList<>(user.getRoles().stream().map(EntRole::getName).toList())
        );
    }
}
