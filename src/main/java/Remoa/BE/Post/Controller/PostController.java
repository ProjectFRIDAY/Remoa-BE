package Remoa.BE.Post.Controller;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Service.MemberService;
import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Service.FileService;
import Remoa.BE.Post.Service.PostService;
import Remoa.BE.Post.form.Request.UploadPostForm;
import Remoa.BE.Post.form.Response.ResReferenceDto;
import Remoa.BE.Post.form.Response.ResRegistCommentDto;
import Remoa.BE.exception.CustomBody;
import Remoa.BE.exception.CustomMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static Remoa.BE.exception.CustomBody.errorResponse;
import static Remoa.BE.exception.CustomBody.successResponse;
import static Remoa.BE.utill.MemberInfo.authorized;
import static Remoa.BE.utill.MemberInfo.getMemberId;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PostController {

    private final FileService fileService;

    private final PostService postService;

    private final MemberService memberService;

    //Todo 게시글 작성중 파일 업로드만 작성
    @PostMapping("/post")
    public void posting(Post post, @RequestParam("files") List<MultipartFile> multipartFile){
        fileService.saveUploadFiles(post,multipartFile);
    }

    /**
     * @param fileId file PK
     * @return file이 저장된 url 반환
     */
    @GetMapping("/post/file/{fileId}/url")
    public String getFileUrl(@PathVariable("fileId") Long fileId ){
        return fileService.getUrl(fileId);
    }

    /**
     * @param fileId file PK
     * @return file을 바로 다운로드 할 수 있다
     */
    @GetMapping("/post/file/{fileId}")
    public ResponseEntity<byte[]> getFileDownload(@PathVariable("fileId") Long fileId ) throws IOException {
        return fileService.getObject(fileId);
    }

    @PostMapping("/reference")  // 게시물 등록
    public ResponseEntity<Object> share(@RequestPart UploadPostForm uploadPostForm,
                      @RequestPart List<MultipartFile> uploadFiles, HttpServletRequest request){
        //TODO postingTime 설정. 로그인 여부 거르는 건 Spring Security 설정으로 가능해서 우선 없어도 괜찮을듯함.
        if(authorized(request)){
            Long memberId = getMemberId();
            Member myMember = memberService.findOne(memberId);
            ResReferenceDto resReferenceDto = postService.registPost(uploadPostForm,uploadFiles,myMember);
            return successResponse(CustomMessage.OK,resReferenceDto);
        }

        return errorResponse(CustomMessage.UNAUTHORIZED);
    }

    @PostMapping("/reference/{reference_id}/comment")
    public ResponseEntity<Object> registComment(@RequestParam Map<String, String> comment, @PathVariable("reference_id") Long postId, HttpServletRequest request){
        String myComment = comment.get("comment");
        if(authorized(request)){
            Long memberId = getMemberId();
            Member myMember = memberService.findOne(memberId);
            ResRegistCommentDto resRegistCommentDto = postService.registComment(myMember, myComment, postId);
            return successResponse(CustomMessage.OK, resRegistCommentDto);
        }
        return errorResponse(CustomMessage.UNAUTHORIZED);
    }

}