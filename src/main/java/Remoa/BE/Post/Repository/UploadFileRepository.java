package Remoa.BE.Post.Repository;

import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Domain.UploadFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UploadFileRepository {

    private final EntityManager em;

    public Optional<UploadFile> findById(Long fileId){
        return Optional.ofNullable(em.find(UploadFile.class, fileId));
    }

    public void saveFile(UploadFile file) {
        em.persist(file);
    }

    public List<String> findFilesByPost(Post post) {
        return em.createQuery("select uf from UploadFile uf where uf.post = :post", UploadFile.class)
                .setParameter("post", post)
                .getResultList()
                .stream()
                .map(file -> file.getSaveFileName())
                .collect(Collectors.toList());
    }

    public void deleteById(UploadFile file) {
        em.createQuery("delete from UploadFile u where u.uploadFileId = :id")
                .setParameter("id", file.getUploadFileId())
                .executeUpdate();
    }

}
