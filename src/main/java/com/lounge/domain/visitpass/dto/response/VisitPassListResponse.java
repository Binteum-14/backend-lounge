package com.lounge.domain.visitpass.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class VisitPassListResponse {

    private List<VisitPassResponse> visitPasses;
}
