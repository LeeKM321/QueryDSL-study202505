package com.playdata.study.repository;

import com.playdata.study.entity.Album;
import com.playdata.study.entity.Group;
import com.playdata.study.entity.Idol;
import com.playdata.study.entity.QAlbum;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.playdata.study.entity.QAlbum.album;
import static com.playdata.study.entity.QGroup.group;
import static com.playdata.study.entity.QIdol.idol;

@SpringBootTest
@Transactional
class QueryDslSubqueryTest {

    @Autowired
    IdolRepository idolRepository;

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    AlbumRepository albumRepository;

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
        Idol idol12 = new Idol("김종국", 48, "남", null);
        Idol idol13 = new Idol("아이유", 31, "여", null);


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
        idolRepository.save(idol12);
        idolRepository.save(idol13);


        Album album1 = new Album("MAP OF THE SOUL 7", 2020, bts);
        Album album2 = new Album("FEARLESS", 2022, leSserafim);
        Album album3 = new Album("UNFORGIVEN", 2023, bts);
        Album album4 = new Album("ELEVEN", 2021, ive);
        Album album5 = new Album("LOVE DIVE", 2022, ive);
        Album album6 = new Album("OMG", 2023, newjeans);

        albumRepository.save(album1);
        albumRepository.save(album2);
        albumRepository.save(album3);
        albumRepository.save(album4);
        albumRepository.save(album5);
        albumRepository.save(album6);


    }

    @Test
    @DisplayName("특정 그룹의 평균 나이보다 많은 아이돌 조회")
    void subqueryTest1() {
        // given
        String groupName = "르세라핌";

        // when
        List<Idol> idolList = factory
                .selectFrom(idol)
                .where(idol.age.gt(
                        JPAExpressions // 서브쿼리를 사용할 수 있게 해 주는 클래스
                                .select(idol.age.avg())
                                .from(idol)
                                .innerJoin(idol.group, group)
                                .where(group.groupName.eq(groupName))
                ))
                .fetch();


        // then
        idolList.forEach(System.out::println);
    }

    @Test
    @DisplayName("그룹별로 가장 최근에 발매된 앨범 정보 조회")
    void subqueryTest2() {
        /*

            SELECT G.group_name, A.album_name, A.release_year
            FROM tbl_group G
            INNER JOIN tbl_album A
            ON G.group_id = A.group_id
            WHERE A.album_id IN (
                    SELECT S.album_id
                    FROM tbl_album S
                    WHERE S.group_id = A.group_id
                        AND s.release_year = (
                            SELECT MAX(release_year)
                            FROM tbl_album
                            WHERE S.group_id = A.group_id
                        )
            )

         */


        // given
        // 동일 테이블에서 서로 다른 영역의 서브쿼리를 별도로 적용하려면
        // 별도의 QClass를 생성해야 합니다.
        QAlbum albumA = new QAlbum("albumA");
        QAlbum albumS = new QAlbum("albumS");

        // when
        List<Tuple> tupleList = factory
                .select(group.groupName, albumA.albumName, albumA.releaseYear)
                .from(group)
                .innerJoin(group.albums, albumA)
                .where(albumA.id.in(
                        JPAExpressions
                                .select(albumS.id)
                                .from(albumS)
                                .where(albumS.group.id.eq(albumA.group.id)
                                        .and(
                                                albumS.releaseYear.eq(
                                                        JPAExpressions
                                                                .select(albumS.releaseYear.max())
                                                                .from(albumS)
                                                                .where(albumS.group.id.eq(albumA.group.id))
                                                )
                                        ))
                )).fetch();

        // then
        for (Tuple tuple : tupleList) {
            String groupName = tuple.get(group.groupName);
            String albumName = tuple.get(albumA.albumName);
            Integer releaseYear = tuple.get(albumA.releaseYear);

            System.out.println("groupName = " + groupName);
            System.out.println("albumName = " + albumName);
            System.out.println("releaseYear = " + releaseYear);
            System.out.println("-------------------------------");
        }
    }

    @Test
    @DisplayName("그룹이 존재하지 않는 아이돌 조회")
    void subqueryTest3() {
        // 서브쿼리: 아이돌이 특정 그룹에 속하는지 확인
        JPQLQuery<Long> subQuery = JPAExpressions
                .select(group.id)
                .from(group)
                .where(group.id.eq(idol.group.id));

        // 메인쿼리: 서브쿼리 결과가 존재하지 않는 아이돌 조회
        List<Idol> result = factory
                .selectFrom(idol)
                .where(subQuery.notExists())
                .fetch();

        result.forEach(System.out::println);
    }

    @Test
    @DisplayName("아이브의 평균 나이보다 나이가 많은 여자 아이돌 조회")
    void subqueryTest4() {
        // given
        String groupName = "아이브";

        // when
        JPQLQuery<Double> subquery = JPAExpressions
                .select(idol.age.avg())
                .from(idol)
                .where(idol.group.groupName.eq(groupName));

        List<Idol> idolList = factory
                .selectFrom(idol)
                .where(idol.gender.eq("여").and(idol.age.gt(subquery)))
                .fetch();

        // then
        idolList.forEach(System.out::println);
    }

    // JPAExpressions는 from절을 제외하고 select와 where절에서 사용이 가능
    // JPQL도 마찬가지로 from절 서브쿼리 사용이 불가.
    // JPA는 반드시 FROM절에 엔터티가 오는 것을 강제.
    // -> Native SQL을 작성하던지, JdbcTemplate 이용, 따로따로 두 번 조회도 사용.


}












