package Remoa.BE.Post.Service;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Service.MemberService;
import Remoa.BE.Post.Domain.Category;
import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Domain.PostScarp;
import Remoa.BE.Post.Dto.Request.UploadPostForm;
import Remoa.BE.Post.Repository.CategoryRepository;
import Remoa.BE.Post.Repository.PostPagingRepository;
import Remoa.BE.Post.Repository.PostRepository;
import Remoa.BE.Post.Repository.PostScrapRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static Remoa.BE.utill.FileExtension.fileExtension;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {


    private final MemberService memberService;

    private final PostRepository postRepository;

    private final CategoryRepository categoryRepository;

    private final FileService fileService;

    private final PostPagingRepository postPagingRepository;

    private final PostScrapRepository postScrapRepository;

    private static final int HOME_PAGE_SIZE = 12;

    public Post findOne(Long postId) {
        Optional<Post> post = postRepository.findOne(postId);
        return post.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post not found"));
    }

    @Transactional
    public Post findOneViewPlus(Long postId) {
        Optional<Post> findPost = postRepository.findOne(postId);
        findPost.ifPresent(post -> post.setViews(post.getViews() + 1));
        return findPost.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Post not found"));
    }

    @Transactional
    public Post registerPost(UploadPostForm uploadPostForm, MultipartFile thumbnail, List<MultipartFile> uploadFiles, Long memberId) throws IOException {

        Category category = categoryRepository.findByCategoryName(uploadPostForm.getCategory());

        Member member = memberService.findOne(memberId);


        //확장자 확인
        String extension = fileExtension(uploadFiles.get(0));
        if (extension.equals("pdf") || extension.equals("jpg") || extension.equals("png")) {
            int pageCount;
            if (extension.equals("pdf")) {
                PDDocument document = PDDocument.load(uploadFiles.get(0).getInputStream());
                pageCount = document.getNumberOfPages();
            } else {
                pageCount = uploadFiles.size();
            }
            Post post = Post.builder()
                    .title(uploadPostForm.getTitle())
                    .member(member)
                    .contestName(uploadPostForm.getContestName())
                    .category(category)
                    .youtubeLink(uploadPostForm.getYoutubeLink())
                    .contestAwardType(uploadPostForm.getContestAwardType())
                    .pageCount(pageCount)
                    .postingTime(LocalDateTime.now())
                    .likeCount(0)
                    .views(0)
                    .scrapCount(0)
                    .deleted(false)
                    .build();

            fileService.saveUploadFiles(post, thumbnail, uploadFiles);
            return post;
        } else {
            throw new IOException();
        }


    }

    public Page<Post> sortAndPaginatePosts(String sort, int pageNumber, String title) {
        Page<Post> Posts;
        Pageable pageable;
        switch (sort) {
            case "like":
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("likeCount").descending());
                Posts = postPagingRepository.findByTitleContaining(pageable, title);
                break;
            case "scrap":
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("scrapCount").descending());
                Posts = postPagingRepository.findByTitleContaining(pageable, title);
                break;
            case "view":
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("views").descending());
                Posts = postPagingRepository.findByTitleContaining(pageable, title);
                break;
            default:
                //sort 문자열이 잘못됐을 경우 default인 최신순으로 정렬
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("postingTime").descending());
                Posts = postPagingRepository.findByTitleContaining(pageable, title);
                break;
        }
        return Posts;
    }

    public Page<Post> sortAndPaginatePostsByCategory(String category, String sort, int pageNumber, String title) {
        Page<Post> Posts;
        Pageable pageable;
        switch (sort) {
            case "like":
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("likeCount").descending());
                break;
            case "scrap":
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("scrapCount").descending());
                break;
            case "view":
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("views").descending());
                break;
            default:
                //sort 문자열이 잘못됐을 경우 default인 최신순으로 정렬
                pageable = PageRequest.of(pageNumber, HOME_PAGE_SIZE, Sort.by("postingTime").descending());
                break;
        }
        Posts = postPagingRepository.findByCategoryAndTitleContaining(pageable, categoryRepository.findByCategoryName(category), title);
        return Posts;
    }

    public PostScarp getPostScrapByMemberIdAndPostId(Long memberId, Long postId) {
        return postScrapRepository.findByMemberMemberIdAndPostPostId(memberId, postId);
    }

    @Transactional
    public void scrapPost(Long memberId, Member myMember, Long referenceId) {
        Post post = findOne(referenceId);
        Integer postScrapCount = post.getScrapCount(); // 이 게시물을 스크랩한 수

        // scrapPost를 db에서 조회해보고 조회 결과가 null이면 scrapCount += 1, PostScrap 생성
        // null이 아니면 scrapCount -= 1, 조회결과인 해당 PostScrap 삭제
        PostScarp postScarp = getPostScrapByMemberIdAndPostId(memberId, referenceId);
        if (postScarp == null) {
            post.setScrapCount(postScrapCount + 1); // 스크랩 수 1 증가
            PostScarp postScrapObj = PostScarp.createPostScrap(myMember, post);
            postScrapRepository.save(postScrapObj);
        } else {
            post.setScrapCount(post.getScrapCount() - 1); // 스크랩 수 1 차감
            postScrapRepository.deleteById(postScarp.getPostScrapId()); // db에서 삭제
        }
    }

    public Page<PostScarp> findScrapedPost(int size, Member member) {
        Pageable pageable = PageRequest.of(0, size);
        return postScrapRepository.findByMemberOrderByScrapTimeDesc(pageable, member);
    }

    public List<Post> findRecentTwelveScrapedPost(Member member) {
        return postPagingRepository.findByMemberRecentTwelve(member);
    }

    public boolean checkMemberPost(Member myMember, Long postId){
        // postId를 이용해 Post 엔티티에 등록된 Member를 불러오고 이 Member가 컨트롤러에서 불러온 Member와 맞는지 확인하고 true/false 반환
        Post post = findOne(postId);
        Member member = post.getMember();
        return myMember == member;
    }


    @Transactional
    public void deleteReference(Long postId){
        postRepository.deletePost(postId);
    }
}