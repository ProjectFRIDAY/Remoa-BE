package Remoa.BE.Post.Service;

import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Domain.UploadFile;
import Remoa.BE.Post.Repository.PostRepository;
import Remoa.BE.Post.Repository.UploadFileRepository;
import Remoa.BE.utill.FileExtension;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static Remoa.BE.utill.FileExtension.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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
    public void saveUploadFiles(Post post,MultipartFile thumbnail, List<MultipartFile> multipartFile) throws IOException {

        String extension = fileExtension(thumbnail);
        //썸네일 파일 저장 추가
        if(extension.equals("jpg") || extension.equals("png") || extension.equals("jpeg")) {
            saveUploadFile(thumbnail, post, "thumbnail");
            multipartFile.forEach(file -> saveUploadFile(file, post, "post"));
        }
        else{
            throw new IOException();
        }


        //새로운 인스턴스 만들어서 set하지 않으면 clear 되면서 null이 계속 저장됨.
        UploadFile uploadFile = uploadFileList.get(0);
        post.setThumbnail(uploadFile);

        post.setUploadFiles(new ArrayList<>(uploadFileList.subList(1, uploadFileList.size())));
        postRepository.savePost(post);

        uploadFileList.clear();
    }

    /**
     *
     * @param multipartFile 파일
     */
    @Transactional
    public void saveUploadFile(MultipartFile multipartFile, Post post,String folderName){

        //파일 타입과 사이즈 저장
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(multipartFile.getContentType());
        objectMetadata.setContentLength(multipartFile.getSize());

        log.info(multipartFile.getContentType());

        //파일 이름
        String originalFilename = multipartFile.getOriginalFilename();

        //파일 이름이 비어있으면 (assert 오류 반환)
        assert originalFilename != null;
        //확장자
        String ext = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        //파일 이름이 겹치지 않게
        String uuid = UUID.randomUUID().toString();

        //post 폴더에 따로 넣어서 보관
        String s3name = folderName+"/"+uuid+"_"+originalFilename;

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
        uploadFile.setOriginalFileName(originalFilename);
        uploadFile.setSaveFileName(s3name);
        uploadFile.setStoreFileUrl(storeFileUrl);
        uploadFile.setExtension(ext);
        uploadFile.setPost(post);

        uploadFileList.add(uploadFile);

        uploadFileRepository.saveFile(uploadFile);

        log.info(storeFileUrl);
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
            //encode 메서드에 두 번째 파라메터에 StandardCharsets.UTF_8만 쓰면 오류가 나서 뒤에 name을 임사방편으로 붙임. 기능상 문제는 없을듯 함
            String fileNameFix = URLEncoder.encode(fileOriginalName, StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");
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