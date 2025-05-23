package com.playdata.study.repository;

import com.playdata.study.GroupAverageResponse;
import com.playdata.study.entity.Group;
import com.playdata.study.entity.Idol;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.playdata.study.entity.QIdol.idol;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class QueryDslGroupingTest {

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
        Group bts = new Group("방탄소년단");
        Group newjeans = new Group("뉴진스");

        groupRepository.save(leSserafim);
        groupRepository.save(ive);
        groupRepository.save(bts);
        groupRepository.save(newjeans);

        Idol idol1 = new Idol("김채원", 24, "여", leSserafim);
        Idol idol2 = new Idol("사쿠라", 26, "여", leSserafim);
        Idol idol3 = new Idol("가을", 22, "여", ive);
        Idol idol4 = new Idol("리즈", 20, "여", ive);
        Idol idol5 = new Idol("장원영", 20, "여", ive);
        Idol idol6 = new Idol("안유진", 21, "여", ive);
        Idol idol7 = new Idol("카즈하", 21, "여", leSserafim);
        Idol idol8 = new Idol("RM", 29, "남", bts);
        Idol idol9 = new Idol("정국", 26, "남", bts);
        Idol idol10 = new Idol("해린", 18, "여", newjeans);
        Idol idol11 = new Idol("혜인", 16, "여", newjeans);

        idolRepository.save(idol1);
        idolRepository.save(idol2);
        idolRepository.save(idol3);
        idolRepository.save(idol4);
        idolRepository.save(idol5);
        idolRepository.save(idol6);
        idolRepository.save(idol7);
        idolRepository.save(idol8);
        idolRepository.save(idol9);
        idolRepository.save(idol10);
        idolRepository.save(idol11);
    }

    @Test
    @DisplayName("SELECT 절에서 원하는 컬럼만 지정 조회")
    void tupleTest() {
        // given

        // when
        // 여러개의 컬럼(필드)을 선택(select)할 때 사용하는 반환 타입
        // DTO로 매핑하지 않고 복수의 값을 직접 다룰 수 있게 해 주는 QueryDSL 전용 객체
        List<Tuple> idolList = factory
                .select(idol.idolName, idol.age)
                .from(idol)
                .fetch();

        // then
        for (Tuple tuple : idolList) {
            System.out.println("tuple = " + tuple);
            String name = tuple.get(idol.idolName);
            int age = tuple.get(idol.age);

            System.out.printf("이름: %s, 나이: %d세\n", name, age);
        }
    }

    @Test
    @DisplayName("그룹화 기본")
    void groupByTest() {
        // given

        // when
        Integer sum = factory
                .select(idol.age.sum())
                .from(idol)
                .fetchOne();

        // then
        System.out.println("sum = " + sum);
    }

    @Test
    @DisplayName("그룹별 인원수 세기")
    void groupByCountTest() {
        // given

        // when
        List<Tuple> idolCounts = factory
                .select(idol.group.groupName, idol.count())
                .from(idol)
                .groupBy(idol.group.id)
                .fetch();

        // then
        for (Tuple tuple : idolCounts) {
            String groupName = tuple.get(idol.group.groupName);
            Long count = tuple.get(idol.count());
            System.out.printf("그룹명: %s, 인원수: %d명\n", groupName, count);
        }
    }

    @Test
    @DisplayName("성별별 아이돌 인원수 세기")
    void groupByGenderTest() {
        // given

        // when
        List<Tuple> result = factory
                .select(idol.count(), idol.gender)
                .from(idol)
                .groupBy(idol.gender)
                .fetch();

        // then
        for (Tuple tuple : result) {
            String gender = tuple.get(idol.gender);
            Long count = tuple.get(idol.count());
            System.out.printf("성별: %s, 인원수: %d명\n", gender, count);
        }
    }

    @Test
    @DisplayName("그룹별로 그룹명과 평균나이를 조회 (나이가 20 ~ 25세만 대상으로)")
    void groupAvgAgeTest() {
        // given

        // when
        List<Tuple> idols = factory
                .select(idol.group.groupName, idol.age.avg())
                .from(idol)
                .groupBy(idol.group)
                .having(idol.age.avg().between(20, 25))
                .fetch();

        // then
        for (Tuple tuple : idols) {
            String groupName = tuple.get(idol.group.groupName);
            Double average = tuple.get(idol.age.avg());
            System.out.printf("그룹명: %s, 평균나이: %.2f세\n", groupName, average);
        }
    }

    @Test
    @DisplayName("그룹별로 그룹명과 평균나이를 조회하여 DTO로 처리")
    void groupAvgAgeDtoTest() {
        // given

        // when
        List<GroupAverageResponse> dtoList = factory
                .select(idol.group.groupName, idol.age.avg())
                .from(idol)
                .groupBy(idol.group)
                .having(idol.age.avg().between(20, 25))
                .fetch()
                .stream()
                .map(tuple -> GroupAverageResponse.from(tuple))
                .collect(Collectors.toList());

        // then
        dtoList.forEach(System.out::println);
    }

    @Test
    @DisplayName("그룹별로 그룹명과 평균나이를 조회하여 DTO로 처리 ver2")
    void groupAvgAgeDtoTestV2() {
        // given

        // when
        List<GroupAverageResponse> dtoList = factory
                .select(
                        // 조회 결과를 원하는 DTO 타입으로 바로 매핑할 때 사용하는 메서드
                        Projections.constructor(
                                GroupAverageResponse.class,
                                idol.group.groupName,
                                idol.age.avg()
                        )
                )
                .from(idol)
                .groupBy(idol.group)
                .having(idol.age.avg().between(20, 25))
                .fetch();

        // then
        dtoList.forEach(System.out::println);
    }

}










