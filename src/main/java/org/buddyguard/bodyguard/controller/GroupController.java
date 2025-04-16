package org.buddyguard.bodyguard.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buddyguard.bodyguard.entity.Group;
import org.buddyguard.bodyguard.entity.GroupMember;
import org.buddyguard.bodyguard.entity.Post;
import org.buddyguard.bodyguard.entity.User;
import org.buddyguard.bodyguard.repository.GroupMemberRepository;
import org.buddyguard.bodyguard.repository.GroupRepository;
import org.buddyguard.bodyguard.repository.PostRepository;
import org.buddyguard.bodyguard.repository.UserRepository;
import org.buddyguard.bodyguard.vo.GroupWithCreator;
import org.buddyguard.bodyguard.vo.PostWithWriter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/group")
public class GroupController {

    private GroupRepository groupRepository;
    private GroupMemberRepository groupMemberRepository;
    private UserRepository userRepository;
    private PostRepository postRepository;

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


        User creator = userRepository.findById(group.getCreatorId());

        GroupWithCreator GC = GroupWithCreator.builder().group(group).creator(creator).build();
        model.addAttribute("groupWithCreator", GC);




        List<Post> posts = postRepository.findByGroupId(id);
        List<PostWithWriter> postWithWriters = new ArrayList<>();

        for (Post post : posts){
            PostWithWriter pw = new PostWithWriter();
            pw.setPost(post);

            User writer = userRepository.findById(post.getWriterId());
            pw.setWriter(writer);

            postWithWriters.add(pw);
        }



        model.addAttribute("posts", posts);
        model.addAttribute("postWithWriters", postWithWriters);

        return "group/view";
    }

    //모임 가입 처리
    @Transactional
    @GetMapping("/{id}/join")
    public String joinHandle(@PathVariable("id") String id, @SessionAttribute("user") User user) {


        boolean exist = false;
        List<GroupMember> list = groupMemberRepository.findByUserId(user.getId());
        for (GroupMember one : list) {
            if (one.getGroupId().equals(id)) {
                exist = true;
                break;
            }
        }

        if (!exist) {
            GroupMember member = GroupMember.builder().
                    userId(user.getId()).groupId(id).role("멤버").build();
            Group group = groupRepository.findById(id);
            if (group.getType().equals("공개")) {
                groupMemberRepository.createApproved(member);
                groupRepository.addMemberCountById(id);
            } else {
                groupMemberRepository.createPending(member);
            }
        }

        return "redirect:/group/" + id;
    }

    // 탈퇴 요청 처리 핸들러
    @GetMapping("/{groupId}/leave")
    public String leaveHandle(@PathVariable("groupId") String groupId, @SessionAttribute("user") User user) {
        int userId = user.getId();
        Map map = Map.of("groupId", groupId, "userId", userId);

        GroupMember found = groupMemberRepository.findByUserIdAndGroupId(map);
        groupMemberRepository.deleteById(found.getId());

        groupRepository.subtractMemberCountById(groupId);
        return "redirect:/";
    }

    // 신청 철회 요청 핸들러
    // 승인 대기중일때 신청 철회버튼으로 변경 기능 만들어야함
    @GetMapping("/{groupId}/cancel")
    public String cancelHandle(@PathVariable("groupId") String groupId, @SessionAttribute("user") User user) {
        int userId = user.getId();
        Map map = Map.of("groupId", groupId, "userId", userId);

        GroupMember found = groupMemberRepository.findByUserIdAndGroupId(map);
        if (found != null && found.getJoinedAt() == null && found.getRole().equals("멤버")) {
            groupMemberRepository.deleteById(found.getId());
        }

        return "redirect:/group/" + groupId;
    }

    //모임 해산
    @Transactional
    @GetMapping("/{groupId}/remove")
    public String removeHandle(@PathVariable("groupId") String groupId, @SessionAttribute("user") User user) {
        Group group = groupRepository.findById(groupId);

        if (group != null && group.getCreatorId() == user.getId()) {
            groupMemberRepository.deleteByGroupId(groupId);
            groupRepository.deleteById(groupId);
            return "redirect:/";
        } else {
            return "redirect:/group/" + groupId;
        }
    }

    //모임 가입 승인
    //이것도 연결을 만들어야함.
    @GetMapping("/{groupId}/approve")
    public String approveHandle(@PathVariable("groupId") String groupId,
                                @RequestParam("targetUserId") String targetUserId,
                                @SessionAttribute("user") User user) {

        Group group = groupRepository.findById(groupId);


        if (group != null && group.getCreatorId() == user.getId() ) {
            GroupMember found = groupMemberRepository.findByUserIdAndGroupId(
                    Map.of("userId", targetUserId, "groupId", groupId)
            );

            if (found != null) {
                groupMemberRepository.updateJoinedAtById(found.getId());
                groupRepository.addMemberCountById(groupId);
            }
        }

        return "redirect:/group/" + groupId;
    }

    // 그룹내 새글 등록
    @PostMapping("/{groupId}/post")
    public String postHandle(@PathVariable("groupId") String id,
                             @ModelAttribute Post post,
                             @SessionAttribute("user") User user) {
        /*
         모델 attribute 로 파라미터는 받았을텐데, 빠진 정보들이 있을거임. 이걸 추가로 set  .
         postRepository를 이용해서 create 메서드 작성
         */
        post.setWriterId(user.getId());
        post.setWroteAt(LocalDateTime.now());

        postRepository.create(post);

        return "redirect:/group/" + id;
    }







}
