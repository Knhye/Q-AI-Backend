package com.example.qnai.service;

import com.example.qnai.dto.qna.request.AnswerUpdateRequest;
import com.example.qnai.dto.qna.request.FeedbackGenerateRequest;
import com.example.qnai.dto.qna.request.QnaGenerateRequest;
import com.example.qnai.dto.qna.response.*;
import com.example.qnai.entity.QnA;
import com.example.qnai.entity.Users;
import com.example.qnai.global.exception.*;
import com.example.qnai.repository.QnaRepository;
import com.example.qnai.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class QnaService {
    private final QnaRepository qnaRepository;
    private final UserRepository userRepository;
    private final GptOssService gptOssService;

    //질문 생성
    @Transactional
    public QnaGenerateResponse generateQuestion(HttpServletRequest httpServletRequest, QnaGenerateRequest request) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Users user = userRepository.findByEmailAndIsDeletedFalse(email)
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 유저입니다."));

        String question = gptOssService.generateQuestion(request);

        if(question == null){
            throw new InternalException("AI로부터 응답을 받지 못했습니다.");
        }

        QnA newQna = QnA.builder()
                .question(question)
                .answer(null)
                .createdAt(LocalDateTime.now())
                .feedback(null)
                .level(request.getLevel())
                .subject(request.getSubject())
                .subjectDetail(request.getSubjectDetail())
                .user(user)
                .build();

        qnaRepository.save(newQna);

        return QnaGenerateResponse.builder()
                .qnaId(newQna.getId())
                .question(question)
                .subject(request.getSubject())
                .subjectDetail(request.getSubjectDetail())
                .level(request.getLevel())
                .build();
    }

    //질의응답 조회
    @Transactional(readOnly = true)
    public QnaDetailResponse getQnaById(HttpServletRequest httpServletRequest, Long id) {
        QnA qnA = qnaRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 질의응답이 존재하지 않습니다."));

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        if(!email.equals(qnA.getUser().getEmail())){
            throw new NotAcceptableUserException("다른 유저의 질의응답은 조회할 수 없습니다.");
        }

        return QnaDetailResponse.builder()
                .id(qnA.getId())
                .question(qnA.getQuestion())
                .answer(qnA.getAnswer())
                .feedback(qnA.getFeedback())
                .subject(qnA.getSubject())
                .subjectDetail(qnA.getSubjectDetail())
                .level(qnA.getLevel())
                .createdAt(qnA.getCreatedAt())
                .updatedAt(qnA.getUpdatedAt())
                .build();

    }

    //최근 업데이트 된 순으로 질문 조회
    @Transactional(readOnly = true)
    public List<QuestionTitlesResponse> getRecentQuestionTitles(HttpServletRequest httpServletRequest) {

        List<QnA> qnAList = qnaRepository.findAllByUserEmailOrderByUpdatedAtDesc( // 최신순 정렬 메서드
                SecurityContextHolder.getContext()
                        .getAuthentication()
                        .getName()
        );

        return qnAList.stream()
                .filter(qna -> !qna.isDeleted())
                .map(qna -> QuestionTitlesResponse.builder()
                        .id(qna.getId())
                        .question(qna.getQuestion())
                        .build()
                )
                .toList();
    }

    //응답 수정
    @Transactional
    public AnswerUpdateResponse updateAnswer(HttpServletRequest httpServletRequest, Long id, AnswerUpdateRequest request) {
        QnA qnA = qnaRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 질의응답이 존재하지 않습니다."));

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        if(!email.equals(qnA.getUser().getEmail())){
            throw new NotAcceptableUserException("다른 유저의 질의응답은 수정할 수 없습니다.");
        }

        qnA.updateAnswer(request.getAnswer());
        qnaRepository.save(qnA);

        return AnswerUpdateResponse.builder()
                .id(qnA.getId())
                .answer(qnA.getAnswer())
                .updatedAt(qnA.getUpdatedAt())
                .build();
    }

    //피드백 생성
    @Transactional
    public FeedbackGenerateResponse generateFeedback(HttpServletRequest httpServletRequest, FeedbackGenerateRequest request) {
        QnA qnA = qnaRepository.findByIdAndIsDeletedFalse(request.getQnaId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 질의응답이 존재하지 않습니다."));

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        if(!email.equals(qnA.getUser().getEmail())){
            throw new NotAcceptableUserException("다른 유저의 질의응답에 피드백을 생성할 수 없습니다.");
        }

        if(!qnA.getQuestion().equals(request.getQuestion())){
            throw new ResourceNotFoundException("해당 질의응답이 존재하지 않습니다.");
        }

        String feedback = gptOssService.generateFeedback(request.getQuestion(), request.getAnswer());

        qnA.updateFeedback(feedback);
        qnaRepository.save(qnA);

        return FeedbackGenerateResponse.builder()
                .qnaId(qnA.getId())
                .feedback(qnA.getFeedback())
                .updatedAt(qnA.getUpdatedAt())
                .build();
    }

    //질의응답 삭제
    @Transactional
    public void deleteQna(HttpServletRequest httpServletRequest, Long id) {
        QnA qnA = qnaRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 질의응답이 존재하지 않습니다."));

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        if(!Objects.equals(qnA.getUser().getEmail(), email)){
            throw new NotAcceptableUserException("다른 유저의 QnA는 삭제할 수 없습니다.");
        }

        qnA.delete();
    }
}
