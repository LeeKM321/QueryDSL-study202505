package com.playdata.study.repository;

import com.playdata.study.dto.SeachDto;
import com.playdata.study.entity.Idol;
import com.playdata.study.entity.Member;
import com.playdata.study.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.playdata.study.entity.QIdol.idol;
import static com.playdata.study.entity.QMember.*;

// QueryDSL용 인터페이스의 구현체는 반드시 이름이 Impl로 끝나야 자동으로 인식되어서
// 원본 인터페이스 타입(MemberRepository)의 객체로도 사용이 가능합니다.
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {

    private final JPAQueryFactory factory;

    @Override
    public List<Member> findByName(String name) {
        return factory
                .selectFrom(member)
                .where(member.userName.eq(name))
                .fetch();
    }

    @Override
    public List<Idol> findIdolByCondition(SeachDto dto) {
        return factory
                .selectFrom(idol)
                .where(buildCondition(dto))
                .fetch();
    }

    private BooleanBuilder buildCondition(SeachDto dto) {
        BooleanBuilder builder = new BooleanBuilder();

        if (dto.getName() != null) {
            builder.and(idol.idolName.contains(dto.getName()));
        }

        if (dto.getGender() != null) {
            builder.and(idol.gender.eq(dto.getGender()));
        }

        if (dto.getMinAge() != null) {
            builder.and(idol.age.goe(dto.getMinAge()));
        }

        if (dto.getMaxAge() != null) {
            builder.and(idol.age.loe(dto.getMaxAge()));
        }

        return builder;
    }


}









