package Remoa.BE.Post.Repository;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Member.Domain.QMember;
import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Domain.PostScarp;
import Remoa.BE.Post.Domain.QPost;
import Remoa.BE.Post.Domain.QPostScarp;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PostCustomRepositoryImpl implements PostCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;
    QMember member = QMember.member;
    QPost post = QPost.post;
    QPostScarp postScarp = QPostScarp.postScarp;
    
    @Override
    public List<Post> findByMemberRecentTwelve(Member member) {
        return jpaQueryFactory.select(postScarp)
                .from(postScarp)
                .join(postScarp.member, this.member)
                .where(this.member.eq(member))
                .orderBy(postScarp.scrapTime.desc())
                .limit(12L)
                .fetch()
                .stream().map(PostScarp::getPost)
                .collect(Collectors.toList());
        
    }
}
