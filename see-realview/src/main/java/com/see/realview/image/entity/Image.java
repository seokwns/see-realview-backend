package com.see.realview.image.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "image_tb")
@Getter
@NoArgsConstructor
public class Image {

    @Id
    @Column(length = 500)
    private String link;

    @Column(nullable = false)
    private Boolean advertisement;

    @Column(nullable = false)
    private Long count;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;


    @Builder
    public Image(String link, Boolean advertisement, Long count, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.link = link;
        this.advertisement = advertisement;
        this.count = count;
        this.createdAt = (createdAt == null? LocalDateTime.now() : createdAt);
        this.updatedAt = updatedAt;
    }

    public static Image of(String link, Boolean advertisement) {
        return Image.builder()
                .link(link)
                .advertisement(advertisement)
                .count(1L) // batch update 시에 중복 레코드는 count + 1로 업데이트하기 때문에 디폴트는 항상 1로 고정
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void addCount(Long count) {
        this.count += count;
    }
}
