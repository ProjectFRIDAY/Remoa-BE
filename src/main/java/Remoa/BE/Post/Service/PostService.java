package Remoa.BE.Post.Service;

import Remoa.BE.Member.Domain.Comment;
import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Post.Domain.Category;
import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Domain.UploadFile;
import Remoa.BE.Post.Repository.PostRepository;
import Remoa.BE.Post.Repository.UploadFileRepository;
import Remoa.BE.Post.Repository.CategoryRepository;
import Remoa.BE.Post.form.Request.UploadPostForm;
import Remoa.BE.Post.form.Response.ResReferenceDto;
import Remoa.BE.Post.form.Response.ResRegistCommentDto;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final UploadFileRepository uploadFileRepository;

    private final PostRepository postRepository;

    private final CategoryRepository categoryRepository;

    private final List<UploadFile> uploadFileList;

    private final FileService fileService;

    public Post dtoToEntity(UploadPostForm uploadPostForm, Member member){ // dto를 db에 저장하기 위해 entity로 변환

        // String category를 이용해 Category 엔티티를 찾기
        Category category = categoryRepository.findByCategoryName(uploadPostForm.getCategory());

        return Post.builder()
                .title(uploadPostForm.getTitle())
                .member(member)
                .contestName(uploadPostForm.getContestName())
                .category(category)
                .contestAwareType(uploadPostForm.getContestAward())
                .build();
    }

    public Post findOne(Long postId){
        Optional<Post> post = postRepository.findOne(postId);
        return post.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post not found"));
    }

    @Transactional
    public ResReferenceDto registPost(UploadPostForm uploadPostForm, List<MultipartFile> uploadFiles, Member member){

        Post post = dtoToEntity(uploadPostForm, member);
        postRepository.savePost(post);

        ResReferenceDto resReferenceDto = ResReferenceDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())
                .contestName(post.getContestName())
                .deadline(post.getDeadline())
                .ContestAward(post.getContestAward())
                .contestAwareType(post.getContestAwareType())
                .likeCount(post.getLikeCount())
                .postingTime(post.getPostingTime())
                .views(post.getViews())
                .deleted(post.getDeleted())
                    .build();

        fileService.saveUploadFiles(post, uploadFiles);
        return resReferenceDto;
    }

    @Transactional
    public ResRegistCommentDto registComment(Member member, String comment, Long postId){
        Comment commentObj = new Comment();
        String formatDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        commentObj.setComment(comment);
        commentObj.setCommentedTime(formatDate);

        ResRegistCommentDto resRegistCommentDto = ResRegistCommentDto.builder()
                .commentId(commentObj.getCommentId())
                .comment(commentObj.getComment())
                .commentedTime(commentObj.getCommentedTime())
                    .build();

        postRepository.saveComment(commentObj);
        return resRegistCommentDto;
    }
}
