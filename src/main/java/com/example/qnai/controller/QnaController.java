package com.example.qnai.controller;

import com.example.qnai.common.ApiResponse;
import com.example.qnai.dto.qna.request.AnswerUpdateRequest;
import com.example.qnai.dto.qna.request.FeedbackGenerateRequest;
import com.example.qnai.dto.qna.request.QnaGenerateRequest;
import com.example.qnai.dto.qna.response.*;
import com.example.qnai.service.QnaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/qna")
@Tag(name = "QnA Controller", description = "Q&A Controller API")
public class QnaController {
    private final QnaService qnaService;
    @PostMapping("/question")
    @Operation(summary = "Generate Question", description = "질문 생성 API")
    public ResponseEntity<ApiResponse<QnaGenerateResponse>> generateQuestion(@Valid @RequestBody QnaGenerateRequest request){
        QnaGenerateResponse response = qnaService.generateQuestion(request);
        return ApiResponse.ok(response, "질문이 생성되었습니다.");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Q&A Details", description = "Q&A 상세 조회 API")
    public ResponseEntity<ApiResponse<QnaDetailResponse>> getQnaById(HttpServletRequest httpServletRequest, @PathVariable Long id){
        QnaDetailResponse response = qnaService.getQnaById(httpServletRequest, id);
        return ApiResponse.ok(response, "질의응답 조회에 성공하였습니다.");
    }

    @GetMapping("/recent")
    @Operation(summary = "Get Question titles Order by asc", description = "최신순으로 질문 조회 API")
    public ResponseEntity<ApiResponse<List<QuestionTitlesResponse>>> getRecentQuestionTitles(HttpServletRequest httpServletRequest){
        List<QuestionTitlesResponse> response = qnaService.getRecentQuestionTitles(httpServletRequest);
        return ApiResponse.ok(response, "최근 질문 타이틀 조회에 성공하였습니다.");
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update Answer", description = "응답 수정 API")
    public ResponseEntity<ApiResponse<AnswerUpdateResponse>> updateAnswer(HttpServletRequest httpServletRequest, @PathVariable Long id, @Valid @RequestBody AnswerUpdateRequest request){
        AnswerUpdateResponse response = qnaService.updateAnswer(httpServletRequest, id, request);
        return ApiResponse.ok(response, "응답을 저장하였습니다.");
    }

    @PostMapping("/feedback")
    @Operation(summary = "Generate AI feedback", description = "피드백 생성 API")
    public ResponseEntity<ApiResponse<FeedbackGenerateResponse>> generateFeedback(HttpServletRequest httpServletRequest, @Valid @RequestBody FeedbackGenerateRequest request){
        FeedbackGenerateResponse response = qnaService.generateFeedback(httpServletRequest, request);
        return ApiResponse.ok(response, "피드백이 생성되었습니다.");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Q&A", description = "Q&A 삭제 API")
    public ResponseEntity<ApiResponse<Void>> deleteQna(HttpServletRequest httpServletRequest, @PathVariable Long id){
        qnaService.deleteQna(httpServletRequest, id);
        return ApiResponse.ok("질의응답이 삭제되었습니다.");
    }
}
