package com.lounge.domain.diagnosis.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(
        description = "진단 완료 요청. 문항 번호와 선택지 번호만 보냅니다.",
        example = """
                {
                  "answers": [
                    { "questionNo": 1, "answerNo": 2 },
                    { "questionNo": 2, "answerNo": 3 },
                    { "questionNo": 3, "answerNo": 4 },
                    { "questionNo": 4, "answerNo": 2 },
                    { "questionNo": 5, "answerNo": 2 },
                    { "questionNo": 6, "answerNo": 1 },
                    { "questionNo": 7, "answerNo": 1 }
                  ]
                }
                """
)
public record DiagnosisCompleteRequest(

        @Valid
        @NotNull(message = "진단 답변은 필수입니다.")
        @Size(min = 7, max = 7, message = "진단 답변은 7개여야 합니다.")
        List<AnswerRequest> answers
) {

    public record AnswerRequest(

            @NotNull(message = "문항 번호는 필수입니다.")
            @Min(value = 1, message = "문항 번호는 1 이상이어야 합니다.")
            Integer questionNo,

            @NotNull(message = "답변 번호는 필수입니다.")
            @Min(value = 1, message = "답변 번호는 1 이상이어야 합니다.")
            Integer answerNo
    ) {
    }
}
