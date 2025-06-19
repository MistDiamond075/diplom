package com.diplom.diplom.controller;

import com.diplom.diplom.configuration.userdetails.DiplomUserDetails;
import com.diplom.diplom.dto.*;
import com.diplom.diplom.entity.*;
import com.diplom.diplom.exception.AccessException;
import com.diplom.diplom.exception.EntityException;
import com.diplom.diplom.misc.utils.Parser;
import com.diplom.diplom.service.*;
import com.diplom.diplom.service.chat.ServiceChat;
import com.diplom.diplom.service.tasks.ServiceTasks;
import com.diplom.diplom.service.tasks.ServiceTasksCompleted;
import com.diplom.diplom.service.user.ServiceUser;
import com.diplom.diplom.service.videocalls.ServiceVideocalls;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
public class CtrlPage {
    private final ServiceJournal srvJournal;
    private final ServiceUser srvUser;
    private final ServiceGroup srvGroup;
    private final ServiceSubject srvSubject;
    private final ServiceTasks srvTasks;
    private final ServiceTasksCompleted srvTasksCompleted;
    private final ServiceConference srvConference;
    private final ServiceVideocalls srvVideocalls;
    private final ServicePasswordRestoreMails srvPasswordRestoreMails;
    private final ServiceChat srvChat;

    public CtrlPage(ServiceJournal srvJournal, ServiceUser srvUser, ServiceGroup srvGroup, ServiceSubject srvSubject, ServiceTasks srvTasks, ServiceTasksCompleted srvTasksCompleted, ServiceConference srvConference, ServiceVideocalls srvVideocalls, ServicePasswordRestoreMails srvPasswordRestoreMails, ServiceChat srvChat) {
        this.srvJournal = srvJournal;
        this.srvUser = srvUser;
        this.srvGroup = srvGroup;
        this.srvSubject = srvSubject;
        this.srvTasks = srvTasks;
        this.srvTasksCompleted = srvTasksCompleted;
        this.srvConference = srvConference;
        this.srvVideocalls = srvVideocalls;
        this.srvPasswordRestoreMails = srvPasswordRestoreMails;
        this.srvChat = srvChat;
    }

    @GetMapping(path = "/")
    public String getIndex(@AuthenticationPrincipal DiplomUserDetails userDetails, Model model) {
        String role=Parser.parseUserRole(userDetails);
        model.addAttribute("role", role);
        return "index";
    }

    @GetMapping(path = {"/loginpage","/loginpage/"})
    public String getLoginpage(){
        return "loginpage";
    }

    @GetMapping(path = {"/registrationpage","/registrationpage/"})
    public String getRegpage(Model model){
        List<EntGroup> groups=srvGroup.getGroups();
        model.addAttribute("groups", groups);
        return "registrationpage";
    }

    @GetMapping(path = {"/pwrestorepage","/pwrestorepage/"})
    public String getPWRestorepage(Model model){
        model.addAttribute("isRestore",false);
        model.addAttribute("uuid",null);
        model.addAttribute("user_id",null);
        return "pwrestorepage";
    }

    @GetMapping(path="/pwrestorepage/restore")
    public String getPWRestorepageToRestore(Model model, @RequestParam String uuid,@RequestParam Long user_id){
        model.addAttribute("isRestore",true);
        model.addAttribute("uuid",uuid);
        model.addAttribute("user_id",user_id);
        return "pwrestorepage";
    }

    @GetMapping(path = "/pwrestorepage/confirm")
    public String getConfirmPwrestore(@RequestParam String uuid) throws EntityException {
        EntPasswordRestoreMails mail=srvPasswordRestoreMails.getMailByUUID(uuid);
        if(mail!=null){
            return "redirect:/pwrestorepage/restore?uuid="+uuid+"&user_id="+mail.getMailuserId().getId();
        }
        return "loginpage";
    }

    @GetMapping(path ="/profile")
    public String getProfilepage(@RequestParam(value = "id",required = false) Long userId,@AuthenticationPrincipal UserDetails userDetails, Model model) throws EntityException {
        String username=Objects.requireNonNull(userDetails).getUsername();
        EntUser user=userId!=null ? srvUser.getOtherUserProfile(userId) : srvUser.getUserByUsername(Objects.requireNonNull(username));
        String groupNames=null;
        model.addAttribute("user",user);
        if(user!=null) {
            List<EntRole> roleList=user.getRoles();
            String userroles="";
            for(EntRole role:roleList){
                String rolename=role.getName();
                userroles+=rolename.substring(rolename.indexOf("ROLE_")+5)+",";
            }
            userroles=userroles.substring(0,userroles.length()-1);
            List<EntGroup> groups=user.getGroups();
            groupNames = groups.stream()
                    .map(EntGroup::getName)
                    .collect(Collectors.joining(", "));
            model.addAttribute("userroles", userroles);
        }else{
            model.addAttribute("userroles",null);
        }
        List<EntGroup> groupsAll=srvGroup.getGroups();
        boolean ownProfile=userId!=null;
        model.addAttribute("ownProfile",ownProfile);
        model.addAttribute("groupNames", groupNames);
        model.addAttribute("groups", groupsAll);
        return "profilepage";
    }

    @GetMapping(path = "/journal")
    public String getJournalPageForUser(@AuthenticationPrincipal DiplomUserDetails userDetails, Model model) throws EntityException {
        String username=Objects.requireNonNull(userDetails).getUsername();
        List<EntJournal> journalList=null;
        String userrole=null;
        List<EntSubject> subjects=null;
        userrole= Parser.parseUserRole(userDetails);
        if (userrole.equals("ROLE_STUDENT")) {
            EntUser user = srvUser.getUserByUsername(username);
            journalList = srvJournal.getAllJournalForUserByName(user.getFirstname());
        } else if(userrole.equals("ROLE_TEACHER") || userrole.equals("ROLE_ADMIN")){
            List<EntGroup> groups=srvGroup.getGroups();
            List<EntUser> students=srvUser.getUsersByRole("ROLE_STUDENT");
            journalList=srvJournal.getAllJournal();
            model.addAttribute("group_list",groups);
            model.addAttribute("student_list",students);
        }
        List<DTOJournal> dtoJournalList = new ArrayList<>();
        for (EntJournal journal : journalList) {
            EntUser user1 = journal.getJournaluserId();
            List<EntGroup> userGroups = user1.getGroups();
            String groupNames = userGroups.stream()
                    .map(EntGroup::getName)
                    .distinct()
                    .collect(Collectors.joining(", "));
            DTOJournal dto = new DTOJournal();
            dto.setJournal(journal);
            dto.setGroups(groupNames);
            dtoJournalList.add(dto);
        }
        subjects=srvSubject.getSubjects();
        model.addAttribute("subject_list",subjects);
        model.addAttribute("userrole",userrole);
        model.addAttribute("journal_list", dtoJournalList);
        return "journalpage";
    }

    @GetMapping(path = "/tasks")
    public String getTaskspage(@AuthenticationPrincipal DiplomUserDetails userDetails, Model model) throws EntityException {
        String username = Objects.requireNonNull(userDetails).getUsername();
        List<EntTasks> taskList = null;
        EntUser user = srvUser.getUserByUsername(Objects.requireNonNull(username));
        String userrole = Parser.parseUserRole(userDetails);
        if (userrole.equals("ROLE_STUDENT")) {
            List<EntGroup> group = user.getGroups();
            taskList = srvTasks.getTasksByGroupId(group);
        } else if (userrole.equals("ROLE_TEACHER") || userrole.equals("ROLE_ADMIN")) {
            taskList = srvTasks.getTasksCreatedByUser(user);
        }
        userrole = userrole.substring(userrole.indexOf("ROLE_") + 5);
        List<EntSubject> subjects=srvSubject.getSubjects();
        List<EntGroup> groups=srvGroup.getGroups();
        model.addAttribute("taskList", taskList);
        model.addAttribute("userrole", userrole);
        model.addAttribute("subject_list",subjects);
        model.addAttribute("group_list",groups);
        return "taskspage";
    }

    @GetMapping(path = "/task/{id}")
    public String getSingleTaskPage(@AuthenticationPrincipal DiplomUserDetails userDetails, Model model,@PathVariable Long id,@RequestParam(value = "studentId",required = false) Long studentId) throws EntityException {
        EntTasks task=srvTasks.getTaskById(id);
        String username=Objects.requireNonNull(userDetails).getUsername();
        String userrole =Parser.parseUserRole(userDetails);
        EntUser user=srvUser.getUserByUsername(username);
        model.addAttribute("task",task);
        model.addAttribute("userrole",userrole);
        if(!Objects.equals(userrole, "ROLE_STUDENT")) {
            List<EntGroup> groupList = task.getGroups();
            Map<EntUser, Set<String>> userToGroupNamesMap = new HashMap<>();
            for (EntGroup group : groupList) {
                for (EntUser user1 : group.getUsers_list()) {
                    userToGroupNamesMap
                            .computeIfAbsent(user1, k -> new HashSet<>())
                            .add(group.getName());
                }
            }
            List<DTOUserTask> dtoUserTasks = new ArrayList<>();
            for (Map.Entry<EntUser, Set<String>> entry : userToGroupNamesMap.entrySet()) {
                DTOUserTask dto = new DTOUserTask();
                dto.setUser(entry.getKey());
                String groupNames = String.join(", ", entry.getValue());
                dto.setGroups(groupNames);
                dtoUserTasks.add(dto);
            }
            List<EntTasksCompleted> tasksCompletedList=srvTasksCompleted.getTasksCompletedByTask(task);
            model.addAttribute("students",dtoUserTasks);
            model.addAttribute("groups",groupList);
            model.addAttribute("completedtasks",tasksCompletedList);
        }
        EntTasksCompleted completedtask = studentId==null ? srvTasksCompleted.getCompletedTaskByUserIdAndTaskId(user.getId(), task.getId()) : srvTasksCompleted.getCompletedTaskByUserIdAndTaskId(srvUser.getUserById(studentId).getId(), task.getId());
        model.addAttribute("completedtask", completedtask);
        model.addAttribute("user_id",user.getId());
        return "taskpage";
    }

    @GetMapping({"/tasks/create","/task/{id}/update"})
    public String getCreateTaskPage(@PathVariable(required = false) Long id,@AuthenticationPrincipal UserDetails userDetails, Model model) throws EntityException {
        List<EntGroup> groups=srvGroup.getGroups();
        List<EntSubject> subjects=srvSubject.getSubjects();
        EntUser user=srvUser.getUserByUsername(userDetails.getUsername());
        EntTasks task;
        try{
            task=srvTasks.getTaskById(id);
        }catch (RuntimeException e){
            task=null;
        }
        model.addAttribute("user_id",user.getId());
        model.addAttribute("subjects",subjects);
        model.addAttribute("groups",groups);
        model.addAttribute("task",task);
        return "taskcreation";
    }

    @GetMapping("/conferences")
    public String getConferencesPage(@AuthenticationPrincipal DiplomUserDetails userDetails, Model model) throws AccessException, EntityException {
        String username = Objects.requireNonNull(userDetails).getUsername();
        List<EntConferences> conferencesList = null;
        EntUser user = srvUser.getUserByUsername(Objects.requireNonNull(username));
        String userrole = Parser.parseUserRole(userDetails);
        userrole = userrole.substring(userrole.indexOf("ROLE_") + 5);
        conferencesList=userrole.equals("STUDENT") ? srvConference.getConferencesByUserGroup(userDetails,user) :  srvConference.getConferencesCreatedByUser(userDetails,user);
        List<EntSubject> subjects=srvSubject.getSubjects();
        List<EntGroup> groups=srvGroup.getGroups();
        List<EntVideocalls> videocallsList=userrole.equals("STUDENT") ? srvVideocalls.getVideocallsByGroup(user) : srvVideocalls.getVideocallsByConferencesCreator(user,userDetails);
        LocalDateTime datenow = LocalDateTime.now();
        Map<EntVideocalls,String> videocalls=new HashMap<>();
        for(EntVideocalls videocall:videocallsList) {
            Duration duration=Duration.between(videocall.getConferencesId().getDatestart(),datenow);
            long mins=duration.toMinutes();
            long hours=duration.toHours();
            String time=mins>60 ? Parser.parseTimeEnding(hours,mins-60*hours) : Parser.parseTimeEnding(null,mins);
            videocalls.put(videocall, time);
        }
        model.addAttribute("conferencesList", conferencesList);
        model.addAttribute("videocallsList", videocalls);
        model.addAttribute("userrole", userrole);
        model.addAttribute("subject_list",subjects);
        model.addAttribute("group_list",groups);
        return "conferencespage";
    }

    @GetMapping({"/conference/create","conference/{id}/update"})
    public String getConferencesCreationPage(@PathVariable(required = false) Long id,@AuthenticationPrincipal UserDetails userDetails, Model model) throws EntityException {
        List<EntGroup> groups=srvGroup.getGroups();
        List<EntSubject> subjects=srvSubject.getSubjects();
        EntUser user=srvUser.getUserByUsername(userDetails.getUsername());
        EntConferences conference;
        try{
            conference=srvConference.getConferenceById(id);
        }catch (RuntimeException | EntityException e){
            conference=null;
        }
        model.addAttribute("user_id",user.getId());
        model.addAttribute("subjects",subjects);
        model.addAttribute("groups",groups);
        model.addAttribute("conference",conference);
        return "conferencescreation";
    }

    @GetMapping("/videocall/{id}")
    public String getVideocallPage(@PathVariable Long id,@AuthenticationPrincipal UserDetails userDetails, Model model) throws EntityException {
        EntVideocalls videocall=srvVideocalls.getVideocallById(id);
        EntUser user=srvUser.getUserByUsername(Objects.requireNonNull(userDetails.getUsername()));
        //EntVideocallsHasUser videocallsHasUser=srvVideocalls.getVideocallHasUserByUserAndVideocall(user,videocall);
        model.addAttribute("STATE_ON", EntVideocallsHasUser.defaultStates.ON);
        model.addAttribute("STATE_OFF", EntVideocallsHasUser.defaultStates.OFF);
        model.addAttribute("STATE_MUTED_BY_ADMIN", EntVideocallsHasUser.defaultStates.MUTED_BY_ADMIN);
        model.addAttribute("user",user);
        model.addAttribute("videocall",videocall);
       // model.addAttribute("videocallhasuser",videocallsHasUser);
        return "videocallpage";
    }

    @GetMapping("/chats")
    public String getChatsPage(@AuthenticationPrincipal DiplomUserDetails userDetails, Model model,@RequestParam(value = "page",defaultValue = "0") int page) throws EntityException, AccessException {
        String username = Objects.requireNonNull(userDetails).getUsername();
        String userrole = Parser.parseUserRole(userDetails);
        List<DTOChatDisplay> chats=srvChat.getChatsDTO(userDetails,page);
        List<EntGroup> groupList = srvGroup.getGroups();
        EntUser user=srvUser.getUserByUsername(userDetails.getUsername());
        model.addAttribute("user_id",user.getId());
        model.addAttribute("groups", groupList);
        model.addAttribute("userrole", userrole);
        model.addAttribute("chatList", chats);
        return "chatspage";
    }

    @GetMapping({"/chat/create","/chat/{id}/update"})
    public String getChatsCreationPage(@PathVariable(required = false) Long id,@AuthenticationPrincipal UserDetails userDetails, Model model) throws EntityException {
        List<EntGroup> groups=srvGroup.getGroups();
        List<EntSubject> subjects=srvSubject.getSubjects();
        List<DTOUserUpdate> users=srvUser.getUsersWithGroups(0);
        EntUser user=srvUser.getUserByUsername(userDetails.getUsername());
        DTOChatDisplay chat;
        try {
            chat = srvChat.getChat(id);
            if(chat.getUser()==null){
                chat.setUser(new ArrayList<>());
            }
        }catch (RuntimeException | EntityException e){
            chat=null;
        }
        model.addAttribute("user_id",user.getId());
        model.addAttribute("subjects",subjects);
        model.addAttribute("groups",groups);
        model.addAttribute("users",users);
        model.addAttribute("chat",chat);
        return "chatcreation";
    }

    @GetMapping("/admin/menu")
    public String getAdminPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        return "adminpage";
    }
}
