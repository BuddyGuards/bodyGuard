package org.buddyguard.bodyguard.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buddyguard.bodyguard.entity.Group;
import org.buddyguard.bodyguard.entity.GroupMember;
import org.buddyguard.bodyguard.entity.User;
import org.buddyguard.bodyguard.repository.GroupMemberRepository;
import org.buddyguard.bodyguard.repository.GroupRepository;
import org.buddyguard.bodyguard.repository.UserRepository;
import org.buddyguard.bodyguard.vo.GroupWithCreator;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/group")
public class GroupController {

    private GroupRepository groupRepository;
    private GroupMemberRepository groupMemberRepository;
    private UserRepository userRepository;

    @GetMapping("/create")
    public String createHandle(Model model) {

        // html에서 세팅해야하는 정보가 뭐에요?

        return "group/create";
    }


    @Transactional
    @PostMapping("/create/verify")
    public String createVerifyHandle(@ModelAttribute Group group,
                                     @SessionAttribute("user") User user) {

        String randomId = UUID.randomUUID().toString().substring(24);

        group.setId(randomId);
        group.setCreatorId(user.getId());
        groupRepository.create(group);

        GroupMember groupMember = new GroupMember();
        groupMember.setUserId(user.getId());
        groupMember.setGroupId(group.getId());
        groupMember.setRole("리더");
        groupMemberRepository.createApproved(groupMember);

        groupRepository.addMemberCountById(group.getId());

        return "redirect:/group/" + randomId;
    }

    @GetMapping("/search")
    public String searchHandle(@RequestParam("word") Optional<String> word, Model model) {
        if (word.isEmpty()) {
            return "redirect:/";
        }
        String wordValue = word.get();
        List<Group> result = groupRepository.findByNameLikeOrGoalLike("%" + wordValue + "%");
        List<GroupWithCreator> convertedResult = new ArrayList<>();

        for (Group one : result) {
            User found = userRepository.findById(one.getCreatorId());


            GroupWithCreator c = GroupWithCreator.builder().group(one).creator(found).build();
            convertedResult.add(c);
        }


        System.out.println("search count : " + result.size());
        model.addAttribute("count", convertedResult.size());
        model.addAttribute("result", convertedResult);


        return "group/search";
    }

    // 모임 상세보기 핸들러
    @GetMapping("/{id}")
    public String viewHandle(@PathVariable("id") String id, Model model, @SessionAttribute("user") User user) {

        Group group = groupRepository.findById(id);
        if (group == null) {
            return "redirect:/";
        }
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", id);
        map.put("userId", user.getId());
        GroupMember status = groupMemberRepository.findByUserIdAndGroupId(map);
        if (status == null) {
            // 아직 참여한적이 없다
            model.addAttribute("status", "NOT_JOINED");
        } else if (status.getJoinedAt() == null) {
            // 승인대기중
            model.addAttribute("status", "PENDING");
        } else if (status.getRole().equals("멤버")) {
            // 멤버이다
            model.addAttribute("status", "MEMBER");
        } else {
            // 리더이다.
            model.addAttribute("status", "LEADER");
        }

        model.addAttribute("group", group);



        return "group/view";
    }






}
