package Remoa.BE.Post.Repository;

import Remoa.BE.Member.Domain.CommentFeedback;
import Remoa.BE.Member.Domain.Member;

import java.util.List;

public interface CommentFeedbackCustomRepository {

    CommentFeedback findByMemberOrderByTime(Member member);
}
