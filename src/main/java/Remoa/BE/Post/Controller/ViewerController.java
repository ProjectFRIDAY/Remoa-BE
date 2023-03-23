package Remoa.BE.Post.Controller;

import Remoa.BE.Member.Dto.Res.ResMemberInfoDto;
import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Domain.UploadFile;
import Remoa.BE.Post.Dto.Response.ResCommentDto;
import Remoa.BE.Post.Dto.Response.ResFeedbackDto;
import Remoa.BE.Post.Dto.Response.ResReferenceViewerDto;
import Remoa.BE.Post.Dto.Response.ResReplyDto;
import Remoa.BE.Post.Service.CommentService;
import Remoa.BE.Post.Service.FeedbackService;
import Remoa.BE.Post.Service.PostService;
import Remoa.BE.exception.CustomMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;

import static Remoa.BE.exception.CustomBody.successResponse;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ViewerController {

    private final CommentService commentService;
    private final PostService postService;
    private final FeedbackService feedbackService;

    @GetMapping("reference/{reference_id}")
    public ResponseEntity<Object> referenceViewer(@PathVariable("reference_id") Long referenceId) {

        Post post = postService.findOneViewPlus(referenceId);
        List<ResCommentDto> comments = commentService.findAllCommentsOfPost(post).stream()
                .filter(comment -> comment.getParentComment() == null)
                .map(comment -> ResCommentDto.builder()
                        .commentId(comment.getCommentId())
                        .member(new ResMemberInfoDto(comment.getMember().getMemberId(),
                                comment.getMember().getNickname(),
                                comment.getMember().getProfileImage()))
                        .comment(comment.getComment())
                        .likeCount(comment.getCommentLikeCount())
                        .commentedTime(comment.getCommentedTime())
                        .replies(commentService.getParentCommentsReply(comment).stream()
                                .map(reply -> ResReplyDto.builder()
                                .replyId(reply.getCommentId())
                                .member(new ResMemberInfoDto(reply.getMember().getMemberId(),
                                        reply.getMember().getNickname(),
                                        reply.getMember().getProfileImage()))
                                .content(reply.getComment())
                                .likeCount(reply.getCommentLikeCount())
                                .build()).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList());

        List<ResFeedbackDto> feedbacks = feedbackService.findAllFeedbacksOfPost(post).stream()
                .filter(feedback -> feedback.getParentFeedback() == null)
                .map(feedback -> ResFeedbackDto.builder()
                        .feedbackId(feedback.getFeedbackId())
                        .member(new ResMemberInfoDto(feedback.getMember().getMemberId(),
                                feedback.getMember().getNickname(),
                                feedback.getMember().getProfileImage()))
                        .feedback(feedback.getFeedback())
                        .page(feedback.getPageNumber())
                        .likeCount(feedback.getFeedbackLikeCount())
                        .feedbackTime(feedback.getFeedbackTime())
                        .replies(feedbackService.getParentFeedbacksReply(feedback).stream()
                                .map(reply -> ResReplyDto.builder()
                                .replyId(reply.getFeedbackId())
                                .member(new ResMemberInfoDto(reply.getMember().getMemberId(),
                                        reply.getMember().getNickname(),
                                        reply.getMember().getProfileImage()))
                                .content(reply.getFeedback())
                                .likeCount(reply.getFeedbackLikeCount())
                                .build()).collect(Collectors.toList()))
                        .build()).collect(Collectors.toList());

        ResReferenceViewerDto result = ResReferenceViewerDto.builder()
                .postId(post.getPostId())
                .postMember(new ResMemberInfoDto(post.getMember().getMemberId(),
                        post.getMember().getNickname(),
                        post.getMember().getProfileImage()))
                .thumbnail(post.getThumbnail().getStoreFileUrl())
                .contestName(post.getContestName())
                .contestAwardType(post.getContestAwardType())
                .category(post.getCategory().getName())
                .title(post.getTitle())
                .likeCount(post.getLikeCount())
                .scrapCount(post.getScrapCount())
                .postingTime(post.getPostingTime().toString())
                .views(post.getViews())
                .pageCount(post.getPageCount())
                .fileNames(post.getUploadFiles().stream().map(UploadFile::getStoreFileUrl).collect(Collectors.toList()))
                .comments(comments)
                .feedbacks(feedbacks)
                .build();


        return successResponse(CustomMessage.OK, result);
    }
}