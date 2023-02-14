package Remoa.BE.Post.Service;

import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Domain.UploadFile;
import Remoa.BE.Post.Repository.PostRepository;
import Remoa.BE.Post.Repository.UploadFileRepository;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final AmazonS3 amazonS3;
    private final UploadFileRepository uploadFileRepository;
    private final PostRepository postRepository;
    private final List<UploadFile> uploadFileList;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /**
     *
     * @param post 게시글
     * @param multipartFile 해당 게시글의 파일 리스트
     * 파일들을 저장해준다
     */
    @Transactional
    public void saveUploadFiles(Post post,List<MultipartFile> multipartFile){

        multipartFile.forEach(item -> saveUploadFile(post,item));

        post.setUploadFiles(uploadFileList);
        postRepository.savePost(post);

        uploadFileList.clear();
    }

    /**
     *
     * @param post 게시글
     * @param multipartFile 파일
     * saveUploadFiles 에서 파일 하나씩 가져와서 s3에 넣는다
     */
    @Transactional
    public void saveUploadFile(Post post, MultipartFile multipartFile){

        //파일 타입과 사이즈 저장
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        //파일 이름
        String originalFilename = multipartFile.getOriginalFilename();

        //파일 이름이 비어있으면 (assert 오류 반환)
        assert originalFilename != null;
        //확장자
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        //파일 이름이 겹치지 않게
        String uuid = UUID.randomUUID().toString();

        //postId 폴더에 따로 넣어서 보관
        String s3name = uuid+"_"+originalFilename;

        try (InputStream inputStream = multipartFile.getInputStream()) {
            amazonS3.putObject(new PutObjectRequest(bucket, s3name, inputStream, objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
        } catch (IOException e) {
            //파일을 제대로 받아오지 못했을때
            //Todo 예외처리 custom 따로 만들기
            throw new RuntimeException(e);
        }

        //파일 보관 url
        String storeFileUrl = amazonS3.getUrl(bucket,s3name).toString().replaceAll("\\+", "+");
        UploadFile uploadFile = new UploadFile();
        uploadFile.setPost(post);
        uploadFile.setOriginalFileName(originalFilename);
        uploadFile.setSaveFileName(s3name);
        uploadFile.setStoreFileUrl(storeFileUrl);
        uploadFile.setExtension(ext);
        uploadFileList.add(uploadFile);
        log.info(storeFileUrl);
        uploadFileRepository.saveFile(uploadFile);
    }

    public String getUrl(Long fileId){
        Optional<UploadFile> file = uploadFileRepository.findById(fileId);
        if(file.isPresent()){
            return file.get().getStoreFileUrl();
        }
        else{
            //해당 파일이 없을떄 예외처리
            //Todo 예외처리 custom 따로 만들기
            throw new RuntimeException();
        }
    }


    public ResponseEntity<byte[]> getObject(Long fileId) throws IOException {

        Optional<UploadFile> file = uploadFileRepository.findById(fileId);
        if(file.isPresent()){
            String s3name = file.get().getSaveFileName();

            S3Object o = amazonS3.getObject(new GetObjectRequest(bucket, s3name));
            S3ObjectInputStream objectInputStream = o.getObjectContent();
            byte[] bytes = IOUtils.toByteArray(objectInputStream);

            String fileOriginalName = file.get().getOriginalFileName();
            String fileNameFix = URLEncoder.encode(fileOriginalName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            httpHeaders.setContentLength(bytes.length);
            httpHeaders.setContentDispositionFormData("attachment", fileNameFix);

            return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
        }
        else{
            //해당 파일이 없을떄 예외처리
            //Todo 예외처리 custom 따로 만들기
            throw new RuntimeException();
        }


    }
}