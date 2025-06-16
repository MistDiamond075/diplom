package com.diplom.diplom.service.tasks;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.entity.EntJournal;
import com.diplom.diplom.entity.EntTasks;
import com.diplom.diplom.entity.EntTasksCompleted;
import com.diplom.diplom.entity.EntUser;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.misc.utils.Checker;
import com.diplom.diplom.repository.RepJournal;
import com.diplom.diplom.repository.RepTasks;
import com.diplom.diplom.repository.RepTasksCompleted;
import com.diplom.diplom.repository.RepUser;
import jakarta.persistence.LockTimeoutException;
import jakarta.transaction.Transactional;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ServiceTasksCompleted {
    private final RepTasksCompleted rTasksCompleted;
    private final RepTasks rTasks;
    private final RepUser rUser;
    private final RepJournal rJournal;
    private final ServiceCompletedtasksfiles srvCompletedtasksfiles;

    public ServiceTasksCompleted(RepTasksCompleted rTasksCompleted, RepTasks rTasks, RepUser rUser, RepJournal rJournal, ServiceCompletedtasksfiles srvCompletedtasksfiles) {
        this.rTasksCompleted = rTasksCompleted;
        this.rTasks = rTasks;
        this.rUser = rUser;
        this.rJournal = rJournal;
        this.srvCompletedtasksfiles = srvCompletedtasksfiles;
    }

    public List<EntTasksCompleted> getTasksCompleted(){
        return rTasksCompleted.findAll();
    }

    public List<EntTasksCompleted> getTasksCompletedByTask(EntTasks task){
        return rTasksCompleted.findAllByTasksId(task);
    }

    public List<EntUser> getStudentsFromCompletedTasksList(EntTasks task){
        List<EntTasksCompleted> completedTasksList=getTasksCompletedByTask(task);
        List<EntUser> studentList=new ArrayList<>();
        for(EntTasksCompleted completedTask:completedTasksList){
            studentList.add(completedTask.getUserId());
        }
        return studentList;
    }

    public EntTasksCompleted getCompletedTaskByUserIdAndTaskId(Long userId, Long taskId){
        try {
            EntUser user = rUser.findById(userId).orElseThrow(() -> new EntityException(
                    HttpStatus.NOT_FOUND,
                    "user not found when getting completed task by user id " + userId,
                    "Пользователь не найден",
                    EntUser.class
            ));
            EntTasks task = rTasks.findById(taskId).orElseThrow(() -> new EntityException(
                    HttpStatus.NOT_FOUND,
                    "task not found when getting completed by task id " + taskId,
                    "Задание не найдено",
                    EntTasks.class
            ));
            return rTasksCompleted.findByUserIdAndTasksId(user, task).orElseThrow(() -> new EntityException(
                    HttpStatus.NOT_FOUND,
                    "task not found when getting completed by task id " + taskId + " and user id " + userId,
                    "Выполненное задание не найдено",
                    EntTasksCompleted.class
            ));
        }catch (RuntimeException | EntityException e){
            return null;
        }
    }

    @Transactional
    public EntTasksCompleted addTask(EntTasksCompleted completedtask, Long task_id, Long user_id, MultipartFile[] files, DiplomUserDetails userDetails) throws EntityException, AccessException {
        EntTasks task=rTasks.findById(task_id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "task not found when adding new completed by id "+task_id,
                "Задание не найдено",
                EntTasks.class
        ));
        EntUser user=rUser.findById(user_id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "user not found when adding new completed by id "+user_id,
                "Пользователь не найден",
                EntUser.class
        ));
        Optional<EntTasksCompleted> task_in_db=rTasksCompleted.findByUserIdAndTasksId(user,task);
        if(task_in_db.isPresent()){
            completedtask.setId(task_in_db.get().getId());
            return updateTaskByUser(completedtask,files,userDetails);
        }
        completedtask.setTasksId(task);
        completedtask.setUserId(user);
        completedtask=rTasksCompleted.save(completedtask);
        if(files!=null){
            srvCompletedtasksfiles.addFiles(files,completedtask,user_id);
        }
        return completedtask;
    }

    @Transactional
    public EntTasksCompleted delTaskById(Long id, DiplomUserDetails userDetails) throws AccessException, EntityException {
        EntTasksCompleted task=rTasksCompleted.findById(id).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "completed task not found when deleting by id "+id,
                "Выполненное задание не найдено",
                EntTasksCompleted.class
        ));
        if(!Checker.checkUserIdentity(userDetails,task.getTasksId().getCreatedby(),rUser)){
            throw new AccessException(HttpStatus.FORBIDDEN,"user identity check failed","Ошибка доступа",userDetails);
        }
        rTasksCompleted.delete(task);
        return task;
    }

    @Transactional
    @Retryable(
            retryFor = { PessimisticLockingFailureException.class, LockTimeoutException.class },
            backoff = @Backoff(delay = 200),
            maxAttempts = 5
    )
    public EntTasksCompleted updateTaskByUser(EntTasksCompleted newtask, MultipartFile[] files, DiplomUserDetails userDetails) throws EntityException, AccessException {
        EntTasksCompleted task=rTasksCompleted.findByIdWithLock(newtask.getId()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "task not found when updating by id "+newtask.getId(),
                "Выполненное задание не найдено",
                EntTasksCompleted.class
        ));
        if(!Checker.checkUserIdentity(userDetails,task.getUserId(),rUser)){
            throw new AccessException(HttpStatus.FORBIDDEN,"user identity check failed","Ошибка доступа",userDetails);
        }
        task.setCommentary(newtask.getCommentary());
        task.setDateofsubmit(newtask.getDateofsubmit());
        if(files!=null){
            srvCompletedtasksfiles.addFiles(files,newtask,newtask.getUserId().getId());
        }
        rTasksCompleted.save(task);
        return task;
    }

    @Transactional
    @Retryable(
            retryFor = { PessimisticLockingFailureException.class, LockTimeoutException.class },
            backoff = @Backoff(delay = 200),
            maxAttempts = 5
    )
    public EntTasksCompleted updateTaskByCheck(EntTasksCompleted task, DiplomUserDetails userDetails) throws AccessException, EntityException {
        EntTasksCompleted originaltask=rTasksCompleted.findByIdWithLock(task.getId()).orElseThrow(()->new EntityException(
                HttpStatus.NOT_FOUND,
                "task not found when checking and updating by id "+task.getId(),
                "Выполненное задание не найдено",
                EntTasksCompleted.class
        ));
        if(!Checker.checkUserIdentity(userDetails,originaltask.getTasksId().getCreatedby(),rUser)){
            throw new AccessException(HttpStatus.FORBIDDEN,"user identity check failed","Ошибка доступа",userDetails);
        }
        originaltask.setFeedback(task.getFeedback());
        originaltask.setDateofcheck(task.getDateofcheck());
        originaltask.setGrade(task.getGrade());
        rTasksCompleted.save(originaltask);
       Optional<EntJournal> journal_in_db=rJournal.findByJournaltasksCompletedId(originaltask);
        EntJournal journal;
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-d HH:mm");
        DateTimeFormatter outputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime dateTime = LocalDateTime.parse(originaltask.getDateofcheck(), inputFormat);
        String formattedDate = dateTime.format(outputFormat);
       if(journal_in_db.isPresent()){
           journal=journal_in_db.get();
           journal.setGrade(originaltask.getGrade());
           journal.setDate(formattedDate);
       }else{
           journal =new EntJournal(null, originaltask.getUserId(), originaltask.getTasksId().getTasksubjectId(),formattedDate, originaltask.getGrade(), null,originaltask);
       }
        rJournal.save(journal);
        return originaltask;
    }

    @Transactional
    @Retryable(
            retryFor = { PessimisticLockingFailureException.class, LockTimeoutException.class },
            backoff = @Backoff(delay = 200),
            maxAttempts = 5
    )
    public EntTasksCompleted updateTaskByCheck(EntTasksCompleted newtask, Long user_id, Long task_id, DiplomUserDetails userDetails) throws EntityException, AccessException {
        EntTasksCompleted originaltask=rTasksCompleted.findByIdWithLock(newtask.getId()).orElse(
                addTask(new EntTasksCompleted(null,null,null,null,null,null,null,null),task_id,user_id,null,userDetails)
        );
        originaltask.setFeedback(newtask.getFeedback());
        originaltask.setDateofcheck(newtask.getDateofcheck());
        originaltask.setGrade(newtask.getGrade());
        rTasksCompleted.save(originaltask);
        return originaltask;
    }
}
