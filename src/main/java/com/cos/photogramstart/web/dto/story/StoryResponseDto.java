package com.cos.photogramstart.web.dto.story;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StoryResponseDto {

    private List<StoryDto> content; // 가공된 스토리 리스트

    private boolean last;           // 마지막 페이지 여부 (JS에서 다음 페이지 호출 판별용)

    private int number;             // 현재 페이지 번호
}
