package Remoa.BE.Post.form.Request;

import Remoa.BE.Post.Domain.Category;
import Remoa.BE.Post.Domain.Post;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
public class UploadPostForm {

    private String title; // Post name

    private String contestName;

    private String category; // Category name

    private String contestAwardType;

    //private List<MultipartFile> uploadFiles;
}
