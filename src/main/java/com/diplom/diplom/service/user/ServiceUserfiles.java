package com.diplom.diplom.service.user;

import com.diplom.diplom.configuration.ConfPropsPaths;
import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.content_management.FilesMGMT;
import com.diplom.diplom.dto.DTOFile;
import com.diplom.diplom.dto.DTOUserSettings;
import com.diplom.diplom.dto.converter.ConverterFileToEntityFile;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.entity.EntUserfiles;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.misc.utils.Checker;
import com.diplom.diplom.misc.utils.FilesProcessor;
import com.diplom.diplom.repository.RepUser;
import com.diplom.diplom.repository.RepUserfiles;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceUserfiles {
    private final RepUserfiles rUserfiles;
    private final RepUser rUser;
    private final ConfPropsPaths apppaths;
    private final ObjectMapper objectMapper;

    @Autowired
    public ServiceUserfiles(RepUserfiles rUserfiles, RepUser rUser, ConfPropsPaths apppaths, ObjectMapper objectMapper) {
        this.rUserfiles = rUserfiles;
        this.rUser = rUser;
        this.apppaths = apppaths;
        this.objectMapper = objectMapper;
    }

    public List<DTOFile> getUserAvatars(){
        List<EntUserfiles> avatarsList= (List<EntUserfiles>) rUserfiles.findAll();
        List<DTOFile> files=new ArrayList<>();
        for(EntUserfiles u:avatarsList){
            files.add(ConverterFileToEntityFile.convertUserFileToDTOFile(u));
        }
        return files;
    }

    public DTOFile getUserAvatarById(Long id) throws EntityException {
        EntUserfiles file=rUserfiles.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user file with id "+id+" not found",
                "Файл не найден",
                EntUserfiles.class
        ));
        return ConverterFileToEntityFile.convertUserFileToDTOFile(file);
    }

    public EntUserfiles getUserfilesByUser(EntUser user,EntUserfiles.fileType type) throws EntityException {
        return rUserfiles.findByFilesuserIdAndType(user,type).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user avatar not found when getting by user",
                "Файл не найден",
                EntUserfiles.class
        ));
    }

    public ResponseEntity<Resource> getUserAvatar(Long  userId) throws EntityException {
        EntUserfiles file;
        try{
            EntUser user=rUser.findById(userId).orElseThrow(()->new EntityException(
                    HttpStatus.NOT_FOUND,
                    "user with id "+userId+" not found",
                    "Пользователь не найден",
                    EntUser.class
            ));
            file=rUserfiles.findByFilesuserIdAndType(user, EntUserfiles.fileType.AVATAR).orElseThrow(()->new EntityException(
                    HttpStatus.NOT_FOUND,
                    "user avatar not found when getting for profile page",
                    "Аватар не найден",
                    EntUserfiles.class
            ));
        }catch (EntityException e){
            file=new EntUserfiles();
            file.setPath("src/main/files/user/profile/avatar_default.png");
        }
            return FilesProcessor.getFileResource(file.getPath(),null);
    }

    public ResponseEntity<Resource> getUserCss(HttpServletRequest request, DiplomUserDetails userDetails) throws EntityException {
        EntUserfiles file;
        EntUser user = userDetails.getUser();
        file = rUserfiles.findByFilesuserIdAndType(user, EntUserfiles.fileType.CSS).orElse(null);
        ResponseEntity<Resource> response=file!=null ? FilesProcessor.getFileResource(file.getPath(),request) : ResponseEntity.notFound().build();
        return response.getStatusCode()!=HttpStatus.NOT_FOUND ? response : ResponseEntity.ok(new ByteArrayResource("".getBytes()));
    }

    public DTOUserSettings getUserSettings(DiplomUserDetails userDetails) throws EntityException, AccessException, JsonProcessingException {
        if(userDetails==null){
            throw new AccessException(
                    HttpStatus.UNAUTHORIZED,
                    "user details was null",
                    "Ошибка проверки данных аккаунта",
                    null
            );
        }
        EntUser user=userDetails.getUser();
        EntUserfiles file=rUserfiles.findByFilesuserIdAndType(user,EntUserfiles.fileType.SETTINGS).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "settings for user {"+user.getId()+"} "+user.getLogin()+" not found",
                "Настройки не найдены",
                EntUserfiles.class
        ));
        return objectMapper.readValue(file.getPath(),DTOUserSettings.class);
    }

    @Transactional
    public EntUserfiles addUserAvatar(MultipartFile file, EntUser userId, DiplomUserDetails userDetails) throws AccessException {
        try{
            if(!(Checker.checkUserIdentity(userDetails,userId,rUser) || (Checker.isUserHasRole(userDetails,rUser)))){
                throw new AccessException(HttpStatus.FORBIDDEN,"user identity check failed","Ошибка доступа",userDetails);
            }
            String fullpath= apppaths.getUserfilesfolder()+"avatar_"+userId.getId();
            File infile=new File(fullpath);
            boolean fileexistst=new File(fullpath+".jpg").exists();
            try(InputStream img_stream = file.getInputStream()) {
                Thumbnails.of(img_stream).scale(1).outputFormat("jpg").toFile(infile);
            }
            EntUserfiles newfile=new EntUserfiles(null,fullpath+".jpg",userId, EntUserfiles.fileType.AVATAR);
            if(!fileexistst) {
                rUserfiles.save(newfile);
            }
        return newfile;
        } catch (IOException e) {throw new RuntimeException(e);}
    }

    @Transactional
    public void addUserCss(String text,UserDetails userDetails) throws AccessException, EntityException, IOException {
        if(userDetails==null){
            throw new AccessException(
                    HttpStatus.UNAUTHORIZED,
                    "user details was null",
                    "Ошибка проверки данных аккаунта",
                    null
            );
        }
        EntUser user=rUser.findByLogin(userDetails.getUsername()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user "+userDetails.getUsername()+" not found",
                "Пользователь не найден",
                EntUser.class
        ));
        String path= FilesMGMT.saveTextFile(apppaths.getUserfilesfolder(),text,"css_"+user.getId()+".css");
        Optional<EntUserfiles> file=rUserfiles.findByFilesuserIdAndType(user, EntUserfiles.fileType.CSS);
        if(file.isEmpty()) {
            EntUserfiles newfile=new EntUserfiles(null,path,user,EntUserfiles.fileType.CSS);
            rUserfiles.save(newfile);
        }
    }

    @Transactional
    public DTOUserSettings addUserSettings(DTOUserSettings dtoSettings,DiplomUserDetails userDetails) throws AccessException, EntityException, IOException {
        if(userDetails==null){
            throw new AccessException(
                    HttpStatus.UNAUTHORIZED,
                    "user details was null",
                    "Ошибка проверки данных аккаунта",
                    null
            );
        }
        EntUser user= userDetails.getUser();
        File jsonFile = Paths.get(apppaths.getUserfilesfolder(), "settings_"+user.getId()+ ".json").toFile();
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(jsonFile, dtoSettings);
        EntUserfiles file=rUserfiles.findByFilesuserIdAndType(user, EntUserfiles.fileType.SETTINGS).orElse(new EntUserfiles(null,jsonFile.getPath(),user,EntUserfiles.fileType.SETTINGS));
        rUserfiles.save(file);
        return dtoSettings;
    }

    @Transactional
    public DTOFile deleteUserFile(Long id) throws EntityException {
        EntUserfiles file=rUserfiles.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user file with id "+id+" not found",
                "Файл не найден",
                EntUserfiles.class
        ));
        rUserfiles.delete(file);
        return ConverterFileToEntityFile.convertUserFileToDTOFile(file);
    }
}
