package com.playdata.study.repository;

import com.playdata.study.entity.Group;
import com.playdata.study.entity.Idol;
import com.playdata.study.entity.QIdol;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.playdata.study.entity.QIdol.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class QueryDslSortTest {

    @Autowired
    IdolRepository idolRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    JPAQueryFactory factory;


    @BeforeEach
    void setUp() {

        //given
        Group leSserafim = new Group("르세라핌");
        Group ive = new Group("아이브");

        groupRepository.save(leSserafim);
        groupRepository.save(ive);

        Idol idol1 = new Idol("김채원", 24, "여", leSserafim);
        Idol idol2 = new Idol("사쿠라", 26, "여", leSserafim);
        Idol idol3 = new Idol("가을", 22, "여", ive);
        Idol idol4 = new Idol("리즈", 20, "여", ive);

        idolRepository.save(idol1);
        idolRepository.save(idol2);
        idolRepository.save(idol3);
        idolRepository.save(idol4);

    }

    @Test
    @DisplayName("기본 정렬하기")
    void sortingTest() {
        // when
        List<Idol> sortedIdol = factory
                .selectFrom(idol)
                .orderBy(
                        idol.age.desc(),
                        idol.idolName.asc()
                )
                .fetch();

        // then
        System.out.println("\n\n\n ====== result ======");
        sortedIdol.forEach(System.out::println);

        Assertions.assertThat(sortedIdol.get(0).getIdolName()).isEqualTo("사쿠라");
    }

    @Test
    @DisplayName("페이징 처리하기")
    void pagingBasicTest() {
        // given
        int page = 3; // 페이지 번호
        int size = 2; // 조회할 데이터 수

        int offset = (page - 1) * size;

        // when
        List<Idol> result = factory
                .selectFrom(idol)
                .orderBy(idol.age.desc())
                .offset(offset)
                .limit(size)
                .fetch();

        // then
        System.out.println("\n\n\n ====== result ======");
        result.forEach(System.out::println);
    }

}












