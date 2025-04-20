package org.buddyguard.bodyguard.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.buddyguard.bodyguard.entity.*;
import org.buddyguard.bodyguard.query.FeelingStats;
import org.buddyguard.bodyguard.repository.*;
import org.buddyguard.bodyguard.vo.CommentWithWriter;
import org.buddyguard.bodyguard.vo.GroupWithCreator;
import org.buddyguard.bodyguard.vo.PostWithGroup;
import org.buddyguard.bodyguard.vo.PostWithWriter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@AllArgsConstructor
@Controller
@RequestMapping("/group")
public class GroupController {

    // 의존성 주입
    private GroupRepository groupRepository;
    private GroupMemberRepository groupMemberRepository;
    private UserRepository userRepository;
    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private PostReactionRepository postReactionRepository;


    // 그룹 생성 페이지
    @GetMapping("/create")
    public String createHandle(Model model,
                               @SessionAttribute("user") @Nullable User user) {

        // 로그인한 유저가 아니라면
        if (user == null) {
            return "auth/login";
        }

        return "group/create";
    }


    // 그룹 생성 및 검증 처리
    @Transactional
    @PostMapping("/create/verify")
    public String createVerifyHandle(@ModelAttribute Group group,
                                     @SessionAttribute("user") User user) {

        // 그룹 ID 랜덤 생성
        String randomId = UUID.randomUUID().toString().substring(24);

        // 그룹의 고유 ID와 생성자 ID 설정
        group.setId(randomId);
        group.setCreatorId(user.getId());

        // DB에 그룹 저장
        groupRepository.create(group);

        // 그룹 생성자(리더)를 멤버로 추가하고 승인 처리
        GroupMember groupMember = new GroupMember();
        groupMember.setUserId(user.getId());     // 그룹 생성자의 사용자 ID 설정
        groupMember.setGroupId(group.getId());   // 그룹 ID 설정
        groupMember.setRole("리더");              // 역할을 리더로 설정

        groupMemberRepository.createApproved(groupMember);   // 승인 상태로 추가
        groupRepository.addMemberCountById(group.getId());   // 멤버 수 1 증가

        return "redirect:/group/" + randomId;
    }


    // 그룹 검색 처리
    @GetMapping("/search")
    public String searchHandle(@RequestParam("word") Optional<String> word, Model model) {

        // 검색어가 Null일 경우 빈 문자열로 처리 (null-safe)
        // 공백을 제거
        String wordValue = word.orElse("").trim();

        // 검색된 그룹 결과를 저장할 리스트
        List<Group> result;

        // 검색어가 비어 있으면 모든 그룹 표시
        if (wordValue.isEmpty()) {
            result = groupRepository.findAll();
            model.addAttribute("isSearch", false);

        // 검색어가 있으면 검색 결과 표시
        } else {
            result = groupRepository.findByNameLikeOrGoalLike("%" + wordValue + "%");
            model.addAttribute("isSearch", true);
        }

        // 각 그룹의 생성자 정보를 추가 및 반환
        List<GroupWithCreator> convertedResult = new ArrayList<>();
        for (Group one : result) {   // 검색된 그룹들에 대해
            User found = userRepository.findById(one.getCreatorId());
            convertedResult.add(GroupWithCreator.builder().group(one).creator(found).build());
        }

        // 모델에 검색 결과와 결과 수 추가
        model.addAttribute("count", convertedResult.size());
        model.addAttribute("result", convertedResult);

        return "group/search";
    }


    // 모임 상세보기 페이지
    @GetMapping("/{id}")
    public String viewHandle(@PathVariable("id") String id, Model model,
                             @SessionAttribute(value = "user", required = false) User user) {

        // 로그인 하지 않은 유저라면
        if (user == null) {
            return "auth/login";
        }

        // 해당 ID로 그룹 정보 조회
        Group group = groupRepository.findById(id);

        // 그룹이 없으면
        if (group == null) {
            return "redirect:/";
        }

        // 그룹 멤버 상태 조회
        Map<String, Object> map = new HashMap<>();
        map.put("groupId", id);            // 그룹 ID 추가
        map.put("userId", user.getId());   // 사용자 ID 추가
        GroupMember status = groupMemberRepository.findByUserIdAndGroupId(map);

        // 그룹 멤버 상태에 따른 역할
        String roleStatus;
        if (status == null) {
            roleStatus = "NOT_JOINED";                // 미가입 상태
        } else if (status.getJoinedAt() == null) {
            roleStatus = "PENDING";                   // 승인 대기 상태
        } else if ("멤버".equals(status.getRole())) {
            roleStatus = "MEMBER";                    // 멤버 역할
        } else {
            roleStatus = "LEADER";                    // 리더 역할
        }

        // 모델에 그룹 및 상태 정보 추가
        model.addAttribute("status", roleStatus);
        model.addAttribute("group", group);

        // 그룹 생성자 정보 조회
        User creator = userRepository.findById(group.getCreatorId());
        GroupWithCreator GC = GroupWithCreator.builder().group(group).creator(creator).build();

        // 그룹 상태 및 정보 추가
        model.addAttribute("groupWithCreator", GC);
        model.addAttribute("sessionUserId", user.getId());

        // 역할이 리더일 경우
        if ("LEADER".equals(roleStatus)) {
            // 승인 대기 중인 멤버 목록 조회
            List<GroupMember> pendingMembers = groupMemberRepository.findPendingMembers(group.getId()); // joined_at IS NULL
            // 승인 대기 중인 멤버 목록
            List<User> pendingUsers = new ArrayList<>();

            for (GroupMember gm : pendingMembers) {
                // 각 멤버의 사용자 정보 조회
                User u = userRepository.findById(gm.getUserId());
                // 멤버가 존재하면 목록에 추가
                if (u != null) pendingUsers.add(u);
            }

            model.addAttribute("pendingUsers", pendingUsers);

            // 승인된 멤버 목록 조회
            List<GroupMember> approvedMembers = groupMemberRepository.findApprovedMembers(group.getId());
            // 승인된 멤버 목록
            List<User> approvedUsers = new ArrayList<>();

            for (GroupMember gm : approvedMembers) {
                User u = userRepository.findById(gm.getUserId());
                if (u != null) approvedUsers.add(u);
            }

            model.addAttribute("approvedUsers", approvedUsers);
        }

        // 그룹 게시글 조회
        List<Post> posts = postRepository.findByGroupId(id);
        List<PostWithWriter> postWithWriters = new ArrayList<>();

        for (Post post : posts) {
            PostWithWriter pw = new PostWithWriter();
            pw.setPost(post);   // 게시글 추가

            // 게시글의 작성자 정보 조회
            User writer = userRepository.findById(post.getWriterId());

            // 작성자 정보 추가
            pw.setWriter(writer);
            // 게시글에 달린 댓글 조회
            pw.setComments(commentRepository.findByCommentWithWriter(post.getId()));

            // 게시글에 달린 감정 반응 통계 조회
            List<FeelingStats> statsList = postReactionRepository.countFeelingByPostId(post.getId());
            Map<String, Integer> reactions = new HashMap<>();

            for (FeelingStats stat : statsList) {
                // 감정별 반응 수 저장
                reactions.put(stat.getFeeling(), stat.getCount());
            }

            // 감정 추가
            pw.setReactions(reactions);

            // 사용자가 해당 게시글에 이미 감정을 남겼는지 확인
            pw.setAlreadyReacted(postReactionRepository.findByWriterIdAndPostId(
                    Map.of("writerId", user.getId(), "postId", post.getId())
            ) != null);   // 감정이 남겨져 있으면 T, 없으면 F

            // 게시글, 감정, 댓글 정보 추가
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

        // 사용자가 이미 그룹에 가입했는지 확인하는 변수
        boolean exist = false;

        // 사용자가 가입한 그룹 목록 조회
        List<GroupMember> list = groupMemberRepository.findByUserId(user.getId());

        for (GroupMember one : list) {
            // 그룹에 이미 가입된 경우
            if (one.getGroupId().equals(id)) {
                exist = true;
                break;
            }
        }

        // 그룹에 가입하지 않았다면
        if (!exist) {
            // 새로운 그룹 멤버 객체 생성
            GroupMember member = GroupMember
                    .builder()
                    .userId(user.getId())
                    .groupId(id)
                    .role("멤버")   // 멤버로 설정
                    .build();

            // DB에서 그룹 정보 조회
            Group group = groupRepository.findById(id);

            // 그룹이 공개 그룹이면 바로 승인 처리
            if (group.getType().equals("공개")) {
                groupMemberRepository.createApproved(member);
                groupRepository.addMemberCountById(id);   // 멤버 수 증가

            // 비공개 그룹은 승인 대기 상태
            } else {
                groupMemberRepository.createPending(member);
            }
        }

        return "redirect:/group/" + id;
    }

    // 탈퇴 요청 처리 핸들러
    @GetMapping("/{groupId}/leave")
    public String leaveHandle(@PathVariable("groupId") String groupId,
                              @SessionAttribute("user") User user) {

        // 세션에서 가져온 로그인 사용자의 ID
        int userId = user.getId();
        // 그룹 ID와 사용자 ID를 맵으로 생성
        Map map = Map.of("groupId", groupId, "userId", userId);

        // 사용자가 해당 그룹에 가입되어 있는지 확인
        GroupMember found = groupMemberRepository.findByUserIdAndGroupId(map);
        groupMemberRepository.deleteById(found.getId());   // 멤버 삭제
        groupRepository.subtractMemberCountById(groupId);  // 멤버 수 감소

        return "redirect:/";
    }


    // 신청 철회 요청
    @GetMapping("/{groupId}/cancel")
    public String cancelHandle(@PathVariable("groupId") String groupId, @SessionAttribute("user") User user) {

        int userId = user.getId();
        Map map = Map.of("groupId", groupId, "userId", userId);

        // 사용자가 해당 그룹에 가입했는지 확인
        GroupMember found = groupMemberRepository.findByUserIdAndGroupId(map);

        // 승인 대기 중일 시 신청 철회
        if (found != null && found.getJoinedAt() == null && found.getRole().equals("멤버")) {
            // 그룹 멤버 삭제
            groupMemberRepository.deleteById(found.getId());
        }

        return "redirect:/group/" + groupId;
    }

    // 모임 해산
    @GetMapping("/{groupId}/remove")
    @Transactional
    public String removeHandle(@PathVariable("groupId") String groupId, @SessionAttribute("user") User user) {
        // DB에서 그룹 정보 조회
        Group group = groupRepository.findById(groupId);

        // 그룹이 존재하고, 그룹의 생성자가 현재 사용자라면
        if (group != null && group.getCreatorId() == user.getId()) {

            // 해당 그룹의 모든 게시글 가져오기
            List<Post> posts = postRepository.findByGroupId(groupId);

            for (Post post : posts) {
                int postId = post.getId();

                // 게시글의 댓글 삭제
                commentRepository.deleteByPostId(postId);
                // 게시글의 감정 삭제
                postReactionRepository.deleteByPostId(postId);
                // 게시글 삭제
                postRepository.deleteById(postId);
            }

            // 그룹 멤버 삭제
            groupMemberRepository.deleteByGroupId(groupId);
            // 그룹 삭제
            groupRepository.deleteById(groupId);

            return "redirect:/";

        } else {
            return "redirect:/group/" + groupId;
        }
    }

    // 모임 가입 승인
    @GetMapping("/{groupId}/approve")
    public String approveHandle(@PathVariable("groupId") String groupId,
                                @RequestParam("targetUserId") String targetUserId,
                                @SessionAttribute("user") User user) {

        // 그룹 정보 조회
        Group group = groupRepository.findById(groupId);

        // 그룹이 존재하고 그룹 생성자가 현재 사용자라면 승인
        if (group != null && group.getCreatorId() == user.getId()) {
            GroupMember found = groupMemberRepository.findByUserIdAndGroupId(
                    Map.of("userId", targetUserId, "groupId", groupId)
            );

            if (found != null) {
                // 그룹 멤버의 승인 날짜를 업데이트하여 승인 처리
                groupMemberRepository.updateJoinedAtById(found.getId());
                groupRepository.addMemberCountById(groupId);   // 승인 처리
            }
        }

        return "redirect:/group/" + groupId;
    }

    // 그룹 리더가 멤버 강퇴
    @Transactional
    @PostMapping("/{groupId}/kick")
    public String kickMemberHandle(@PathVariable("groupId") String groupId,
                                   @RequestParam("targetUserId") int targetUserId,
                                   @SessionAttribute("user") User user) {

        Group group = groupRepository.findById(groupId);

        // 리더가 아니면 강퇴 불가
        if (group == null || group.getCreatorId() != user.getId()) {
            return "redirect:/group/" + groupId;
        }

        // 리더 자신은 강퇴 불가
        if (user.getId() == targetUserId) {
            return "redirect:/group/" + groupId;
        }

        // 강퇴할 멤버 조회
        Map<String, Object> map = Map.of("groupId", groupId, "userId", targetUserId);
        GroupMember member = groupMemberRepository.findByUserIdAndGroupId(map);

        // 그룹에 가입된 멤버라면
        if (member != null && member.getJoinedAt() != null) {
            // 멤버 삭제
            groupMemberRepository.deleteById(member.getId());
            // 그룹의 멤버 수 감소
            groupRepository.subtractMemberCountById(groupId);
        }

        return "redirect:/group/" + groupId;
    }


    // 그룹 내 새 게시글 등록
    @PostMapping("/{groupId}/post")
    public String postHandle(@PathVariable("groupId") String groupId,
                             @ModelAttribute Post post,
                             @SessionAttribute("user") User user,
                             @RequestParam("image") MultipartFile image) {

        // 현재 로그인한 사용자의 ID로 작성자 설정
        post.setWriterId(user.getId());
        // 글 작성 시간을 현재 시각으로 설정
        post.setWroteAt(LocalDateTime.now());

        // 이미지가 업로드된 경우 처리
        if (!image.isEmpty()) {
            // 이미지의 원본 파일명
            String originalName = image.getOriginalFilename();
            // 확장자만 추출
            String extension = originalName.substring(originalName.lastIndexOf("."));
            // UUID를 사용하여 이미지 파일명 생성
            String filename = UUID.randomUUID() + extension;

            // 파일 저장
            Path path = Paths.get("C:/resources/uploads/" + filename); //

            try {
                Files.copy(image.getInputStream(), path);  // 파일을 서버에 저장
                post.setImageUrl("/uploads/" + filename);  // 웹 접근 경로 설정

            } catch (IOException e) {
                e.printStackTrace(); // 나중에 logger로 변경 권장
            }
        }

        // 게시글 저장
        postRepository.create(post);

        return "redirect:/group/" + groupId;
    }


    // 그룹 내 게시글 삭제
    @PostMapping("/{groupId}/post/{postId}/delete")
    @Transactional
    public String deletePostHandle(@PathVariable("groupId") String groupId,
                                   @PathVariable("postId") int postId,
                                   @SessionAttribute("user") User user) {

        // 게시글 조회
        Post post = postRepository.findById(postId);

        // 본인이 작성한 글이라면
        if (post != null && post.getWriterId() == user.getId()) {
            // 댓글 삭제
            commentRepository.deleteByPostId(postId);
            // 게시글 삭제
            postRepository.deleteById(postId);
        }

        return "redirect:/group/" + groupId;
    }


    // 그룹 내 게시글 조회
    @GetMapping("/{groupId}/post/{postId}")
    public String viewPost(@PathVariable("groupId") String groupId,
                           @PathVariable("postId") int postId,
                           Model model) {

        // 게시글 조회
        Post post = postRepository.findById(postId);

        // 게시글이 존재하지 않으면
        if (post == null) {
            return "redirect:/group/" + groupId;
        }

        // 게시글 작성자 조회
        User writer = userRepository.findById(post.getWriterId());

        // 댓글 목록 조회
        List<CommentWithWriter> comments = commentRepository.findByCommentWithWriter(postId);

        // 감정 통계 조회
        List<FeelingStats> statsList = postReactionRepository.countFeelingByPostId(postId);
        Map<String, Integer> reactions = new HashMap<>();
        for (FeelingStats stat : statsList) {
            // 감정별 반응 수를 맵에 저장
            reactions.put(stat.getFeeling(), stat.getCount());
        }

        // 게시글과 관련된 정보를 담을 객체 생성
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
        // 어떤 게시글에 쓴 댓글인지 설정
        comment.setPostId(postId);
        // 댓글 작성 시각을 현재 시각으로 설정
        comment.setWroteAt(LocalDateTime.now());
        // DB에 댓글 저장
        commentRepository.create(comment);

        return "redirect:/group/" + groupId;
    }


    // 댓글 삭제 처리
    @PostMapping("/{groupId}/post/{postId}/comment/{commentId}/delete")
    public String deleteCommentHandle(@PathVariable("groupId") String groupId,
                                      @PathVariable("postId") int postId,
                                      @PathVariable("commentId") int commentId,
                                      @SessionAttribute("user") User user) {

        // 댓글 ID로 DB에서 댓글 조회
        Comment comment = commentRepository.findById(commentId);

        // 댓글 작성자와 로그인 사용자가 일치하면
        if (comment != null && comment.getWriterId() == user.getId()) {
            // 댓글 삭제
            commentRepository.deleteById(commentId);
        }

        return "redirect:/group/" + groupId;
    }


    // 게시글에 감정 남기기 요청 처리
    @PostMapping("/{groupId}/post/{postId}/reaction")
    public String postReactionHandle(@PathVariable("groupId") String groupId,
                                     @PathVariable("postId") int postId,
                                     @ModelAttribute PostReaction postReaction,
                                     @SessionAttribute("user") User user) {

        // 감정을 남긴 이력이 있는지 조회
        // 사용자 ID와 게시글 ID를 통해 감정 조회
        PostReaction found = postReactionRepository.findByWriterIdAndPostId(
                Map.of("writerId", user.getId(), "postId", postId)
        );

        if (found != null) {
            // 한 번 더 누르면 삭제
            postReactionRepository.deleteById(found.getId());

        } else {
            // 감정 새로 추가
            postReaction.setWriterId(user.getId());       // 감정을 단 작성자 ID 설정
            postReaction.setPostId(postId);               // 감정이 달린 게시글 ID 설정
            postReaction.setGroupId(groupId);             // 감정이 달린 그룹 ID 설정
            postReactionRepository.create(postReaction);  // DB에 감정 저장
        }

        return "redirect:/group/" + groupId;
    }


    // 내 글 목록 보기
    @GetMapping("/my-posts")
    public String myPostHandle(@SessionAttribute("user") User user, Model model) {

        // 사용자가 작성한 게시글 목록 조회
        List<Post> posts = postRepository.findByWriterId(user.getId());
        List<PostWithGroup> postWithGroups = new ArrayList<>();

        for (Post post : posts) {
            // 게시글이 속한 그룹 조회
            Group group = groupRepository.findById(post.getGroupId());
            // 해당 게시글의 댓글 수 조회
            int commentCount = commentRepository.countByPostId(post.getId());

            // 게시글과 그룹 정보를 담을 객체 생성
            PostWithGroup pwg = new PostWithGroup();
            pwg.setPost(post);       // 게시글 정보 추가
            pwg.setGroup(group);     // 그룹 정보 추가
            pwg.setCommentCount(commentCount);  // 댓글 수 주가
            postWithGroups.add(pwg); // 리스트에 추가
        }

        model.addAttribute("postWithGroups", postWithGroups);

        return "group/my-posts";
    }

    // 내 글 목록 보기에서 게시글 삭제
    @Transactional
    @PostMapping("/my-posts/delete")
    public String deleteMyPost(@RequestParam("postId") int postId,
                               @SessionAttribute("user") User user) {

        // 게시글 조회
        Post post = postRepository.findById(postId);

        // 작성자가 로그인한 사용자와 일치하면
        if (post != null && post.getWriterId() == user.getId()) {
            // 댓글 삭제
            commentRepository.deleteByPostId(postId);
            // 게시글 삭제
            postRepository.deleteById(postId);
        }

        return "redirect:/group/my-posts";
    }

}

