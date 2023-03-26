package Remoa.BE.Post.Repository;

import Remoa.BE.Member.Domain.CommentFeedback;
import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Domain.QCommentFeedback;
import Remoa.BE.Member.Domain.QMember;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentFeedbackCustomRepositoryImpl implements CommentFeedbackCustomRepository{

    private final JPAQueryFactory jpaQueryFactory;

    QCommentFeedback commentFeedback = QCommentFeedback.commentFeedback;
    QMember member = QMember.member;

    @Override
    public CommentFeedback findByMemberOrderByTime(Member member) {
        return jpaQueryFactory.select(commentFeedback)
                .from(commentFeedback)
                .join(commentFeedback.member, this.member)
                .where(this.member.eq(member))
                .orderBy(commentFeedback.time.desc())
                .limit(1L)
                .fetch()
                .get(0);
    }
}
