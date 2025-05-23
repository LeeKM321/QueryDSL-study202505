package com.playdata.study.repository;

import com.playdata.study.entity.Group;
import com.playdata.study.entity.Idol;
import com.playdata.study.entity.QIdol;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
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
class QueryDslBasicTest {

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

        Idol idol1 = new Idol("김채원", 24, leSserafim);
        Idol idol2 = new Idol("사쿠라", 26, leSserafim);
        Idol idol3 = new Idol("가을", 22, ive);
        Idol idol4 = new Idol("리즈", 20, ive);

        idolRepository.save(idol1);
        idolRepository.save(idol2);
        idolRepository.save(idol3);
        idolRepository.save(idol4);

    }

    @Test
    @DisplayName("나이가 24세 이상인 아이돌은 존재한다.")
    void testAge() {
        // given
        int age = 24;

        // when
        List<Idol> result = factory
                .selectFrom(idol)
                .where(idol.age.goe(age))
                .fetch();

        // then
        assertFalse(result.isEmpty());

        for (Idol idol : result) {
            System.out.println("idol = " + idol);
            assertTrue(idol.getAge() >= age);
        }
    }

    @Test
    @DisplayName("이름에 '김'이 포함된 아이돌을 조회하고, 실제 '김'이 포함되었는지 확인.")
    void testNameCondition() {
        // given
        String substring = "김";

        // when
        List<Idol> result = factory
                .selectFrom(idol)
                .where(idol.idolName.contains(substring))
                .fetch();

        // then
        for (Idol idol : result) {
            System.out.println("idol = " + idol);
            assertTrue(idol.getIdolName().contains(substring));
        }
    }

    @Test
    @DisplayName("나이가 20세에서 25세 사이인 아이돌은 존재한다.")
    void testAge2() {
        // given
        int ageStart = 20;
        int ageEnd = 25;

        // when
        List<Idol> result = factory
                .selectFrom(idol)
                .where(idol.age.between(ageStart, ageEnd))
                .fetch();

        // then
        assertFalse(result.isEmpty());
        for (Idol idol : result) {
            System.out.println("idol = " + idol);
            assertTrue(idol.getAge() >= ageStart && idol.getAge() <= ageEnd);
        }
    }

    @Test
    @DisplayName("'르세라핌' 그룹에 속한 인원은 두명일 것이다.")
    void testGroupCondition() {
        // given
        String groupName = "르세라핌";

        // when
        List<Idol> result = factory
                .selectFrom(idol)
                .where(idol.group.groupName.eq(groupName))
                .fetch();

        // then
        assertEquals(2, result.size());
    }


}










