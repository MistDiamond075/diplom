package com.diplom.diplom.service.tasks;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.entity.EntGroup;
import com.diplom.diplom.entity.EntSubject;
import com.diplom.diplom.entity.EntTasks;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.misc.utils.Checker;
import com.diplom.diplom.misc.utils.FilesProcessor;
import com.diplom.diplom.repository.RepGroup;
import com.diplom.diplom.repository.RepSubject;
import com.diplom.diplom.repository.RepTasks;
import com.diplom.diplom.repository.RepUser;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
public class ServiceTasks {
    private final RepTasks rTasks;
    private final RepUser rUser;
    private final RepSubject rSubject;
    private final RepGroup rGroup;
    private final ServiceTasksfiles srvTasksfiles;

    @Autowired
    public ServiceTasks(RepTasks rTasks, RepUser rUser, RepSubject rSubject, RepGroup rGroup, ServiceTasksfiles srvTasksfiles) {
        this.rTasks = rTasks;
        this.rUser = rUser;
        this.rSubject = rSubject;
        this.rGroup = rGroup;
        this.srvTasksfiles = srvTasksfiles;
    }

    public List<EntTasks> getTasks(){
        return rTasks.findAll();
    }

    public List<EntTasks> getTasksByGroupId(List<EntGroup> group){
        return rTasks.findAllByGroupsIn(group);
    }

    public List<EntTasks> getTasksCreatedByUser(EntUser user){
        return rTasks.findAllByCreatedby(user);
    }

    public EntTasks getTaskById(Long id) throws EntityException {
        return rTasks.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "task not found when getting by id "+id,
                "Задание не найдено",
                EntTasks.class
        ));
    }

    @Transactional
    public EntTasks addTask(EntTasks task, String groups, String subjectname, Long user_id, MultipartFile[] files) throws EntityException {
        EntUser user=rUser.findById(user_id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user with id "+user_id+" not found when adding task",
                "Пользователь не найден",
                EntUser.class
        ));
        EntSubject subject=rSubject.findByName(subjectname).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "subject "+subjectname+" not found when adding task",
                "Предмет не найден",
                EntSubject.class
        ));
        List<EntGroup> groupList = Arrays.stream(groups.split(","))
                .map(group -> {
                    try {
                        return rGroup.findByName(group)
                                .orElseThrow(() -> new EntityException(
                                        HttpStatus.NOT_FOUND,
                                        "group " + group + " not found when adding task",
                                        "Группа не найдена",
                                        EntGroup.class
                                ));
                    } catch (EntityException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
        task.setCreatedby(user);
        task.setTasksubjectId(subject);
        task.setGroups(groupList);
        rTasks.save(task);
        if(files!=null){
            srvTasksfiles.addFiles(files,task,user);
        }
        return task;
    }

    @Transactional
    public EntTasks deleteTaskById(Long id, DiplomUserDetails userDetails) throws AccessException, EntityException {
        EntTasks task=rTasks.findById(Objects.requireNonNull(id)).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "task not found when deleting by id "+id,
                "Задание не найдено",
                EntTasks.class
        ));
        if(!Checker.checkUserIdentity(userDetails,task.getCreatedby(),rUser)){
            throw new AccessException(HttpStatus.FORBIDDEN,"user identity check failed","Ошибка доступа",userDetails);
        }
        char separator=task.getTaskfiles_list().getFirst().getPath().contains("/") ? '/' : '\\';
        FilesProcessor.deleteDirectory(task.getTaskfiles_list().getFirst().getPath().substring(0,task.getTaskfiles_list().getFirst().getPath().lastIndexOf(separator)));
        rTasks.delete(task);
        return task;
    }

    @Transactional
    public EntTasks updateTask(EntTasks newtask, Long taskId, String subjectname, String groups, MultipartFile[] files, DiplomUserDetails userDetails) throws EntityException, AccessException {
        EntTasks task=rTasks.findById(taskId).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "task not found when updating by id "+taskId,
                "Задание не найдено",
                EntTasks.class
        ));
        if(!Checker.checkUserIdentity(userDetails,task.getCreatedby(),rUser)){
            throw new AccessException(HttpStatus.FORBIDDEN,"user identity check failed","Ошибка доступа",userDetails);
        }
        if(!task.getTasksubjectId().getName().equals(Objects.requireNonNull(subjectname))){
            EntSubject subject=rSubject.findByName(subjectname).orElseThrow(()->new EntityException(
                    HttpStatus.NOT_FOUND,
                    "subject "+subjectname+" not found when adding task",
                    "Предмет не найден",
                    EntSubject.class
            ));
            task.setTasksubjectId(subject);
        }
        if(groups!=null) {
            List<String> newgroups = Arrays.stream(groups.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
            List<String> oldgroups = task.getGroups().stream()
                    .map(EntGroup::getName)
                    .toList();
            if (!new HashSet<>(oldgroups).equals(new HashSet<>(newgroups))) {
                List<EntGroup> groupList = new ArrayList<>();
                for (String newgroup : newgroups) {
                    EntGroup group = rGroup.findByName(newgroup).orElseThrow(() -> new EntityException(
                            HttpStatus.NOT_FOUND,
                            "group " + newgroup + " not found when adding task",
                            "Группа не найдена",
                            EntGroup.class
                    ));
                    groupList.add(group);
                }
                task.setGroups(groupList);
            }
        }
        task.setDateend(newtask.getDateend());
        task.setText(newtask.getText());
        task.setName(newtask.getName());
        if(files!=null){
            srvTasksfiles.addFiles(files,task,task.getCreatedby());
        }
        rTasks.save(task);
        return task;
    }
}
