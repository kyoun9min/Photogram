package com.cos.photogramstart.domain.image;

import com.cos.photogramstart.domain.likes.Likes;
import com.cos.photogramstart.domain.user.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "user")
@Data
@Entity
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String caption; // 오늘 나 너무 피곤해!!

    private String postImageUrl; // 사진을 전송받아서 그 사진을 서버에 특정 폴더에 저장 = DB에 그 저장된 경로를 insert

    @JsonIgnoreProperties({"images"})
    @JoinColumn(name = "userId")
    @ManyToOne(fetch = FetchType.EAGER) // 이미지를 SELECT 하면 조인해서 User 정보를 같이 들고옴
    private User user;

    @OneToMany(mappedBy = "image")
    @JsonIgnoreProperties({"image"})
    private List<Likes> likes;

    private LocalDateTime createDate;

    @Transient // DB에 컬럼이 만들어지지 않는다.
    private boolean likeState;

    @Transient
    private int likeCount;
    @PrePersist
    public void createDate() {
        this.createDate = LocalDateTime.now();
    }

    // 객체 무한참조를 방지하기 위해 toString()에서 user값을 제외. 어노테이션으로 대체
//    @Override
//    public String toString() {
//        return "Image{" +
//                "id=" + id +
//                ", caption='" + caption + '\'' +
//                ", postImageUrl='" + postImageUrl + '\'' +
//                ", createDate=" + createDate +
//                '}';
//    }
}
