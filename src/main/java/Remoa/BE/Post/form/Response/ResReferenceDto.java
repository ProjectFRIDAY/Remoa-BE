package Remoa.BE.Post.form.Response;

import Remoa.BE.Member.Domain.Member;
import lombok.Builder;

@Builder
public class ResReferenceDto {

    public Long postId;
    public String title;
    public String contestName;
    public String deadline;
    public Boolean ContestAward;
    public String contestAwareType;
    public Integer likeCount;
    public String postingTime;
    public Integer views = 0;
    public Boolean deleted = Boolean.FALSE;

}
