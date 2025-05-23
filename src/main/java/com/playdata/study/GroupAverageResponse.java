package com.playdata.study;

import com.playdata.study.entity.QIdol;
import com.querydsl.core.Tuple;
import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class GroupAverageResponse {

    private String groupName;
    private double averageAge;

    // Tuple을 DTO로 변환하는 메서드
    public static GroupAverageResponse from(Tuple tuple) {
        return GroupAverageResponse.builder()
                .groupName(tuple.get(QIdol.idol.group.groupName))
                .averageAge(tuple.get(QIdol.idol.age.avg()))
                .build();
    }

}