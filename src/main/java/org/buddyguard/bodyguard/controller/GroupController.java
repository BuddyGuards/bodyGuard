package org.buddyguard.bodyguard.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buddyguard.bodyguard.entity.*;
import org.buddyguard.bodyguard.query.FeelingStats;
import org.buddyguard.bodyguard.repository.*;
import org.buddyguard.bodyguard.vo.CommentWithWriter;
import org.buddyguard.bodyguard.vo.GroupWithCreator;
import org.buddyguard.bodyguard.vo.PostWithWriter;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    private CommentRepository commentRepository;
    private PostReactionRepository postReactionRepository;

    // 그룹 생성 페이지
    @GetMapping("/create")
    public String createHandle(Model model) {

        return "group/create";
    }

    // 그룹 생성 및 검증 처리
    @Transactional
    @PostMapping("/create/verify")
    public String createVerifyHandle(@ModelAttribute Group group,
                                     @SessionAttribute("user") User user) {

        // 그룹 ID 랜덤 생성
        String randomId = UUID.randomUUID().toString().substring(24);

        group.setId(randomId);
        group.setCreatorId(user.getId());

        groupRepository.create(group);

        GroupMember groupMember = new GroupMember();
        groupMember.setUserId(user.getId());
        groupMember.setGroupId(group.getId());
        groupMember.setRole("리더");   // 역할 리더로 설정

        groupMemberRepository.createApproved(groupMember);   // 승인
        groupRepository.addMemberCountById(group.getId());   // 멤버 수 증가

        return "redirect:/group/" + randomId;
    }

    // 그룹 검색 처리
    @GetMapping("/search")
    public String searchHandle(@RequestParam("word") Optional<String> word, Model model) {

        // 검색어가 없으면
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

    // 모임 상세보기
    @GetMapping("/{id}")
    public String viewHandle(@PathVariable("id") String id, Model model,
                             @SessionAttribute(value = "user", required = false) User user) {

        if (user == null) {
            return "auth/login";
        }

        // 해당 ID로 그룹 정보 조회
        Group group = groupRepository.findById(id);

        // 그룹이 존재하지 않으면
        if (group == null) {
            return "redirect:/";
        }

        // 그룹 멤버 정보 조회
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

        // 그룹 생성자 정보 조회
        User creator = userRepository.findById(group.getCreatorId());

        GroupWithCreator GC = GroupWithCreator.builder().group(group).creator(creator).build();
        model.addAttribute("groupWithCreator", GC);

        // 그룹의 게시물 조회
        List<Post> posts = postRepository.findByGroupId(id);
        List<PostWithWriter> postWithWriters = new ArrayList<>();

        for (Post post : posts) {
            PostWithWriter pw = new PostWithWriter();

            // 게시글 본문
            pw.setPost(post);

            // 작성자 정보
            User writer = userRepository.findById(post.getWriterId());
            pw.setWriter(writer);

            // 댓글 목록
            pw.setComments(commentRepository.findByCommentWithWriter(post.getId()));

            // 감정 통계 조회 (List<FeelingStats> → Map<String, Integer>)
            List<FeelingStats> statsList = postReactionRepository.countFeelingByPostId(post.getId());
            Map<String, Integer> reactions = new HashMap<>();
            for (FeelingStats stat : statsList) {
                reactions.put(stat.getFeeling(), stat.getCount());
            }

            pw.setReactions(reactions);
            pw.setAlreadyReacted(postReactionRepository.findByWriterIdAndPostId(
                    Map.of("writerId", user.getId(), "postId", post.getId())
            ) != null );
            postWithWriters.add(pw);
        }

        model.addAttribute("postWithWriters", postWithWriters);
        model.addAttribute("posts", posts);

        return "group/view";
    }

    // 모임 가입 처리
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

    // 모임 해산
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
    @GetMapping("/{groupId}/approve")
    public String approveHandle(@PathVariable("groupId") String groupId,
                                @RequestParam("targetUserId") String targetUserId,
                                @SessionAttribute("user") User user) {

        Group group = groupRepository.findById(groupId);

        if (group != null && group.getCreatorId() == user.getId()) {
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

    // 그룹 내 새 글 등록
    @PostMapping("/{groupId}/post")
    public String postHandle(@PathVariable("groupId") String groupId,
                             @ModelAttribute Post post,
                             @SessionAttribute("user") User user) {

        // 현재 로그인한 사용자의 ID로 작성자 설정
        post.setWriterId(user.getId());
        // 글 작성 시간을 현재 시각으로 설정
        post.setWroteAt(LocalDateTime.now());
        // 게시글 DB에 저장
        postRepository.create(post);

        return "redirect:/group/" + groupId;
    }

    // 그룹 내 게시글 조회
    @GetMapping("/{groupId}/post/{postId}")
    public String viewPost(@PathVariable("groupId") String groupId,
                           @PathVariable("postId") int postId,
                           Model model) {

        // 게시글 ID로 게시글 정보 조회
        Post post = postRepository.findById(postId);

        if (post == null) {
            return "redirect:/group/" + groupId;
        }

        // 게시글 작성자 조회
        User writer = userRepository.findById(post.getWriterId());

        // 댓글 목록 조회
        List<CommentWithWriter> comments = commentRepository.findByCommentWithWriter(postId);

        // 감정 통계 조회 및 변환
        List<FeelingStats> statsList = postReactionRepository.countFeelingByPostId(postId);
        Map<String, Integer> reactions = new HashMap<>();
        for (FeelingStats stat : statsList) {
            reactions.put(stat.getFeeling(), stat.getCount());
        }

        PostWithWriter postWithWriter = new PostWithWriter();
        postWithWriter.setPost(post);
        postWithWriter.setWriter(writer);
        postWithWriter.setComments(comments);
        postWithWriter.setReactions(reactions);

        model.addAttribute("postWithWriter", postWithWriter);
        model.addAttribute("comments", comments);

        return "group/view";
    }


    // 댓글 등록 처리
    @PostMapping("/{groupId}/post/{postId}/comment")
    public String commentHandle(@PathVariable("groupId") String groupId,
                                @PathVariable("postId") int postId,
                                @ModelAttribute Comment comment,
                                @SessionAttribute("user") User user) {

        // 현재 로그인한 사용자를 댓글 작성자로 설정
        comment.setWriterId(user.getId());
        // 어떤 게시글에 쓴 댓글인지
        comment.setPostId(postId);
        // 댓글 작성 시각 설정
        comment.setWroteAt(LocalDateTime.now());
        // 댓글 DB에 저장
        commentRepository.create(comment);

        return "redirect:/group/" + groupId;
    }


    // 게시글에 감정 남기기 요청 처리
    @PostMapping("/{groupId}/post/{postId}/reaction")
    public String postReactionHandle(@PathVariable("groupId") String groupId,
                                     @PathVariable("postId") int postId,
                                     @ModelAttribute PostReaction postReaction,
                                     @SessionAttribute("user") User user) {

        // 이미 감정을 남긴 이력이 있는지 조회
        PostReaction found = postReactionRepository.findByWriterIdAndPostId(
                Map.of("writerId", user.getId(), "postId", postId)
        );

        if (found != null) {
            postReactionRepository.deleteById(found.getId());  // 한 번 더 누르면 삭제

        } else {
            postReaction.setWriterId(user.getId());
            postReaction.setPostId(postId);
            postReaction.setGroupId(groupId);
            postReactionRepository.create(postReaction);
        }

        return "redirect:/group/" + groupId;
    }


}

