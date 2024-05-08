package com.see.realview.search.repository.impl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.see.realview.search.entity.QSearchHistory;
import com.see.realview.search.entity.SearchHistory;
import com.see.realview.search.repository.SearchHistoryRepository;
import com.see.realview.user.entity.UserAccount;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class SearchHistoryRepositoryImpl implements SearchHistoryRepository {
    private final EntityManager entityManager;

    private final JPAQueryFactory jpaQueryFactory;

    private final static QSearchHistory TABLE = QSearchHistory.searchHistory;

    private final static int LATEST_HISTORY_SIZE = 10;


    public SearchHistoryRepositoryImpl(
            @Autowired EntityManager entityManager,
            @Autowired JPAQueryFactory jpaQueryFactory) {
        this.entityManager = entityManager;
        this.jpaQueryFactory = jpaQueryFactory;
    }

    @Override
    public void save(SearchHistory searchHistory) {
        entityManager.persist(searchHistory);
    }

    @Override
    public List<SearchHistory> findLatestHistories(UserAccount userAccount) {
        return jpaQueryFactory
                .selectFrom(TABLE)
                .where(TABLE.id.userAccount.eq(userAccount))
                .orderBy(TABLE.id.time.desc())
                .limit(LATEST_HISTORY_SIZE)
                .fetch();
    }
}
