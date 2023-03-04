package Remoa.BE.Post.Service;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Post.Domain.Category;
import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Domain.UploadFile;
import Remoa.BE.Post.Repository.PostRepository;
import Remoa.BE.Post.Repository.UploadFileRepository;
import Remoa.BE.Post.Repository.CategoryRepository;
import Remoa.BE.Post.form.Request.UploadPostForm;
import Remoa.BE.Post.form.Response.ResReferenceDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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

}
