package com.pbl.mapmo;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "article")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer views = 0;

    @Column(columnDefinition = "INT DEFAULT 0")
    private Integer likes = 0;

    @Column(name = "dislike", columnDefinition = "INT DEFAULT 0")
    private Integer dislikes = 0;

    @Column(length = 255)
    private String location;

    @Column(columnDefinition = "DATETIME DEFAULT NULL")
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "article_ibfk_1"))
    private User user;

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ArticleComment> articleComments;
}

