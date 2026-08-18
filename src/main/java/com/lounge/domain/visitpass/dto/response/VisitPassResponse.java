package com.lounge.domain.visitpass.dto.response;

import com.lounge.domain.visitpass.entity.VisitPass;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class VisitPassResponse {

    private Long visitPassId;
    private String username;
    private LocalDate diagnosedAt;
    private String qrImageUrl;

    public static VisitPassResponse from(VisitPass visitPass) {
        return new VisitPassResponse(
                visitPass.getId(),
                visitPass.getUser().getUsername(),
                visitPass.getIssuedDate(),
                visitPass.getQrCodeUrl()
        );
    }
}
