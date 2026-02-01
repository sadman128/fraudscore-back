package com.sajid.fraudscore.dto;

import com.sajid.fraudscore.model.Comment;
import com.sajid.fraudscore.model.Post;
import lombok.*;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FraudAnalysisDto {

    public String entityName;
    public List<Post> posts;
    public List<Comment> comments;

}
