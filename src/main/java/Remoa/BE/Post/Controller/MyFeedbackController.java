package Remoa.BE.Post.Controller;

import Remoa.BE.Member.Domain.Comment;
import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Dto.Res.ResMemberInfoDto;
import Remoa.BE.Member.Service.MemberService;
import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Dto.Response.ResCommentDto;
import Remoa.BE.Post.Dto.Response.ResReceivedCommentDto;
import Remoa.BE.Post.Dto.Response.ResReplyDto;
import Remoa.BE.Post.Service.CommentService;
import Remoa.BE.Post.Service.MyPostService;
import Remoa.BE.exception.CustomMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Remoa.BE.exception.CustomBody.errorResponse;
import static Remoa.BE.exception.CustomBody.successResponse;
import static Remoa.BE.utill.MemberInfo.authorized;
import static Remoa.BE.utill.MemberInfo.getMemberId;

@RestController
@RequiredArgsConstructor
@Slf4j
public class MyFeedbackController {

    private final MemberService memberService;
    private final MyPostService myPostService;
    private final CommentService commentService;

    @GetMapping("/user/feedback")
    public ResponseEntity<Object> receivedFeedback(HttpServletRequest request,
                                                   @RequestParam(required = false, defaultValue = "all") String category,
                                                   @RequestParam(required = false, defaultValue = "1", name = "page") int pageNumber) {

        if (authorized(request)) {
            Long memberId = getMemberId();
            Member myMember = memberService.findOne(memberId);

            pageNumber -= 1;
            if (pageNumber < 0) {
                return errorResponse(CustomMessage.PAGE_NUM_OVER);
            }

            Page<Post> posts = myPostService.getNewestThreePosts(pageNumber, myMember);

            if ((posts.getContent().isEmpty()) && (posts.getTotalElements() > 0)) {
                return errorResponse(CustomMessage.PAGE_NUM_OVER);
            }

            Map<String, Object> result = new HashMap<>();

            int postNumber = 1;
            for (Post post : posts) { //조회한 post
                Map<String, ResCommentDto> commentInfo = new HashMap<>();

                List<Comment> parentComments = commentService.getRecentThreeCommentsExceptReply(post);

                int commentNumber = 1;
                for (Comment parentComment : parentComments) { //조회한 post의 parent comment
                    List<Comment> parentCommentsReply = commentService.getParentCommentsReply(parentComment);



                    List<ResReplyDto> replies = new ArrayList<>();
                    for (Comment reply : parentCommentsReply) {

                        replies.add(new ResReplyDto(
                                reply.getCommentId(),
                                new ResMemberInfoDto(reply.getMember().getMemberId(),
                                        reply.getMember().getNickname(),
                                        reply.getMember().getProfileImage()),
                                reply.getComment(),
                                reply.getCommentLikeCount(),
                                reply.getCommentedTime()));
                    }

                    commentInfo.put("comment_" + commentNumber, new ResCommentDto(
                            parentComment.getCommentId(),
                            new ResMemberInfoDto(parentComment.getMember().getMemberId(),
                                    parentComment.getMember().getNickname(),
                                    parentComment.getMember().getProfileImage()),
                            parentComment.getComment(),
                            parentComment.getCommentLikeCount(),
                            parentComment.getCommentedTime(),
                            replies));

                    commentNumber++;
                }

                ResReceivedCommentDto map = ResReceivedCommentDto.builder()
                        .title(post.getTitle())
                        .postId(post.getPostId())
                        .commentInfo(commentInfo)
                        .build();

                result.put("post_" + postNumber, map);

                postNumber++;
            }
            result.put("totalPages", posts.getTotalPages()); //전체 페이지의 수
            result.put("totalOfAllComments", posts.getTotalElements()); //모든 코멘트의 수
            result.put("totalOfPageElements", posts.getNumberOfElements()); //현 페이지 피드백의 수

            return successResponse(CustomMessage.OK, result);

        }
        return errorResponse(CustomMessage.UNAUTHORIZED);
    }

}
