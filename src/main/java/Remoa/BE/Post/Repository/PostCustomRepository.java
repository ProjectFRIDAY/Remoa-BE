package Remoa.BE.Post.Repository;

import Remoa.BE.Member.Domain.Member;
import Remoa.BE.Post.Domain.Post;
import Remoa.BE.Post.Domain.PostScarp;

import java.util.List;

public interface PostCustomRepository {

    List<Post> findByMemberRecentTwelve(Member member);
    
}
