package Remoa.BE.Post.Controller;

import Remoa.BE.Member.Domain.CommentFeedback;
import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Dto.Res.ResMemberInfoDto;
import Remoa.BE.Member.Service.MemberService;
import Remoa.BE.Post.Domain.PostScarp;
import Remoa.BE.Post.Dto.Response.ResCommentDto;
import Remoa.BE.Post.Dto.Response.ResCommentFeedbackDto;
import Remoa.BE.Post.Dto.Response.ResPostDto;
import Remoa.BE.Post.Service.CommentFeedbackService;
import Remoa.BE.Post.Service.PostService;
import Remoa.BE.exception.CustomMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static Remoa.BE.Member.Domain.ContentType.COMMENT;
import static Remoa.BE.Member.Domain.ContentType.FEEDBACK;
import static Remoa.BE.exception.CustomBody.errorResponse;
import static Remoa.BE.exception.CustomBody.successResponse;
import static Remoa.BE.utill.MemberInfo.authorized;
import static Remoa.BE.utill.MemberInfo.getMemberId;

@RestController
@Slf4j
@RequiredArgsConstructor
public class MyActivityController {

    private final MemberService memberService;
    private final CommentFeedbackService commentFeedbackService;
    private final PostService postService;

    /**
     * 내 활동 관리
     * @param request
     * @return Map<String, Object>
     *     "contents" : 내가 작성한 최신 댓글(Comment, Feedback 무관)들의 List.
     *     "posts" : 내가 스크랩한 post들을 가장 최근 스크랩한 순서의 List.
     * 주의사항! : page는 고정해두고 size를 이용하므로 누적 데이터가 return 됨.
     */
    @GetMapping("/user/activity")
    public ResponseEntity<Object> myActivity(HttpServletRequest request) {

        if (authorized(request)) {
            Long memberId = getMemberId();
            Member myMember = memberService.findOne(memberId);

            Map<String, Object> result = new HashMap<>();

            CommentFeedback commentFeedback = commentFeedbackService.findNewestCommentFeedback(myMember);

            ResCommentFeedbackDto commentOrFeedback = null;
            if (commentFeedback.getType().equals(FEEDBACK)) {
                commentOrFeedback = ResCommentFeedbackDto.builder()
                        .title(commentFeedback.getPost().getTitle())
                        .postId(commentFeedback.getPost().getPostId())
                        .thumbnail(commentFeedback.getPost().getThumbnail().getStoreFileUrl())
                        .member(new ResMemberInfoDto(commentFeedback.getMember().getMemberId(),
                                commentFeedback.getMember().getNickname(),
                                commentFeedback.getMember().getProfileImage()))
                        .content(commentFeedback.getFeedback().getFeedback())
                        .likeCount(commentFeedback.getFeedback().getFeedbackLikeCount())
                        .build();
            } else if (commentFeedback.getType().equals(COMMENT)) {
                commentOrFeedback = ResCommentFeedbackDto.builder()
                        .title(commentFeedback.getPost().getTitle())
                        .postId(commentFeedback.getPost().getPostId())
                        .thumbnail(commentFeedback.getPost().getThumbnail().getStoreFileUrl())
                        .member(new ResMemberInfoDto(commentFeedback.getMember().getMemberId(),
                                commentFeedback.getMember().getNickname(),
                                commentFeedback.getMember().getProfileImage()))
                        .content(commentFeedback.getComment().getComment())
                        .likeCount(commentFeedback.getComment().getCommentLikeCount())
                        .build();
            }
            result.put("content", commentOrFeedback);


            /**
             * 조회한 최근에 스크랩한 12개의 post들을 dto로 mapping.
             */
            List<ResPostDto> posts = postService.findRecentTwelveScrapedPost(myMember).stream()
                    .map(post -> ResPostDto.builder()
                            .postId(post.getPostId())
                            .postMember(new ResMemberInfoDto(post.getMember().getMemberId(),
                                    post.getMember().getNickname(),
                                    post.getMember().getProfileImage()))
                            .thumbnail(post.getThumbnail().getStoreFileUrl())
                            .title(post.getTitle())
                            .likeCount(post.getLikeCount())
                            .postingTime(post.getPostingTime().toString())
                            .views(post.getViews())
                            .scrapCount(post.getScrapCount())
                            .categoryName(post.getCategory().getName())
                            .build()).collect(Collectors.toList());

            result.put("posts", posts);

            return successResponse(CustomMessage.OK, result);
        }
        return errorResponse(CustomMessage.UNAUTHORIZED);
    }
}
