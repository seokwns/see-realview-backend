package com.see.realview.image.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.see.realview.image.entity.ParsedImage;
import com.see.realview.image.entity.QParsedImage;
import com.see.realview.image.repository.ParsedImageRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ParsedImageRepositoryImpl implements ParsedImageRepository {

    private final EntityManager entityManager;

    private final JPAQueryFactory jpaQueryFactory;

    private final static QParsedImage TABLE = QParsedImage.parsedImage;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final String PARSED_IMAGE_TABLE = "parsed_image_tb";

    private final static int IMAGE_CACHING_SIZE = 100;


    public ParsedImageRepositoryImpl(@Autowired EntityManager entityManager,
                                     @Autowired JPAQueryFactory jpaQueryFactory,
                                     @Autowired NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.entityManager = entityManager;
        this.jpaQueryFactory = jpaQueryFactory;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    @Override
    public List<ParsedImage> findAllByUrlIn(List<String> urls) {
        return jpaQueryFactory
                .selectFrom(TABLE)
                .where(TABLE.link.in(urls))
                .fetch();
    }

    @Override
    public List<ParsedImage> findCachingImages() {
        return jpaQueryFactory
                .selectFrom(TABLE)
                .orderBy(TABLE.count.desc())
                .limit(IMAGE_CACHING_SIZE)
                .fetch();
    }

    @Override
    public void save(ParsedImage image) {
        entityManager.persist(image);
    }

    @Override
    public void saveAll(List<ParsedImage> images) {
        String sql = String.format("""
                INSERT INTO `%s` (link, advertisement, count)
                VALUES (:link, :advertisement, :count)
                ON DUPLICATE KEY UPDATE link = :link, advertisement = :advertisement, count = count + :count
                """, PARSED_IMAGE_TABLE);

        SqlParameterSource[] parameterSources = images
                .stream()
                .map(BeanPropertySqlParameterSource::new)
                .toArray(SqlParameterSource[]::new);

        namedParameterJdbcTemplate.batchUpdate(sql, parameterSources);
    }

    @Override
    public Boolean isWellKnownURL(String url) {
        return url.contains("blogmall.net/campaign/blogWidget/")
                || url.contains("xn--939au0g4vj8sq.net/_sp/wg.php")
                || url.contains("www.revu.net/campaign/img.php");
    }
}
