package Remoa.BE.Member.Domain;

import Remoa.BE.Post.Domain.Post;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.*;

import static javax.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@Where(clause = "deleted = false")
public class Comment {

    @Id
    @GeneratedValue
    @Column(name = "comment_id")
    private Long commentId;

    /**
     * Comment가 속해있는 Post
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    /**
     * Comment를 작성한 Member
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    /**
     * Comment의 내용
     */
    private String comment;

    /**
     * Comment가 작성된 시간
     */
    @Column(name = "commented_time")
    private String commentedTime;

    /**
     * Comment의 좋아요 숫자
     */
    @Column(name = "comment_like_count")
    private Integer commentLikeCount;

    private Boolean deleted = Boolean.FALSE;
}