package com.diplom.diplom.service;

import com.diplom.diplom.entity.EntPasswordRestoreMails;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.repository.RepPasswordRestoreMails;
import com.diplom.diplom.repository.RepUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ServicePasswordRestoreMails {
    private final RepPasswordRestoreMails rPasswordRestoreMails;
    private final RepUser rUser;
    private final JavaMailSender mailSender;

    @Autowired
    public ServicePasswordRestoreMails(RepPasswordRestoreMails rPasswordRestoreMails, RepUser rUser, JavaMailSender mailSender) {
        this.rPasswordRestoreMails = rPasswordRestoreMails;
        this.rUser = rUser;
        this.mailSender = mailSender;
    }

    public EntPasswordRestoreMails getMailByUUID(String uuid) throws EntityException {
        return rPasswordRestoreMails.findByMailuuid(uuid).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "mail not found when getting by uuid "+uuid,
                "Письмо не найдено",
                EntPasswordRestoreMails.class
        ));
    }

    public EntPasswordRestoreMails getMailByUserId(EntUser user) throws EntityException {
        return rPasswordRestoreMails.findByMailuserId(user).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "mail not found when getting by user id "+ Objects.requireNonNull(user.getId()),
                "Письмо не найдено",
                EntPasswordRestoreMails.class
        ));
    }

    @Transactional
    public EntPasswordRestoreMails addMail(EntPasswordRestoreMails newmail){
        rPasswordRestoreMails.save(newmail);
        return newmail;
    }

    @Transactional
    public EntPasswordRestoreMails delMail(EntPasswordRestoreMails mailtodel){
        rPasswordRestoreMails.delete(mailtodel);
        return mailtodel;
    }

    public void sendMail(EntPasswordRestoreMails newmail){
        SimpleMailMessage message = new SimpleMailMessage();
        String text="Для восстановления пароля перейди по этой ссылке: http://5.189.10.253:8083/pwrestorepage/confirm?uuid="+newmail.getMailuuid();
        message.setTo(newmail.getMailuserId().getEmail());
        message.setSubject("Password restore");
        message.setText(text);
        message.setFrom("egor.ufimtsef@yandex.ru");

        mailSender.send(message);
    }
}
