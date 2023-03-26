package Remoa.BE.Post.Repository;

import Remoa.BE.Member.Domain.CommentFeedback;
import Remoa.BE.Member.Domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentFeedbackRepository extends JpaRepository<CommentFeedback, Long>, CommentFeedbackCustomRepository {

    Page<CommentFeedback> findByMemberOrderByTimeDesc(Pageable pageable, Member member);

}
