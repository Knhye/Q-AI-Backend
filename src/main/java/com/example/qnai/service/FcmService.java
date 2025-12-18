package com.example.qnai.service;

import com.example.qnai.dto.fcm.request.MessagePushServiceRequest;
import com.example.qnai.entity.Users;
import com.example.qnai.repository.UserRepository;
import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.InternalException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FcmService implements ExternalPushService{

    private final UserRepository userRepository;
    private final FirebaseMessaging firebaseMessaging;
    private static final int BATCH_SIZE = 500;

    public void send(List<MessagePushServiceRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        if (FirebaseApp.getApps().isEmpty()) {
            return;
        }

        List<Long> userIds = requests.stream()
                .map(MessagePushServiceRequest::userId)
                .distinct()
                .toList();

        Map<Long, String> userTokenMap = userRepository.findAllById(userIds).stream()
                .filter(user -> user.getFcmToken() != null && !user.getFcmToken().isEmpty())
                .collect(Collectors.toMap(Users::getId, Users::getFcmToken));

        // 메시지 리스트 구성
        List<Message> messages = requests.stream()
                .filter(request -> userTokenMap.containsKey(request.userId()))
                .map(request -> buildMessage(userTokenMap.get(request.userId()), request))
                .toList();

        if (messages.isEmpty()) {
            return;
        }

        try {
            sendInBatches(messages);
        } catch (Exception e) {
            throw new InternalException("배치 알림 전송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private void sendInBatches(List<Message> messages) throws FirebaseMessagingException {
        for (int i = 0; i < messages.size(); i += BATCH_SIZE) {
            int end = Math.min(i + BATCH_SIZE, messages.size());
            List<Message> batch = messages.subList(i, end);

            firebaseMessaging.sendEach(batch); // FirebaseMessaging 사용
        }
    }

    private Message buildMessage(String fcmToken, MessagePushServiceRequest request) {
        Notification firebaseNotification = Notification.builder()
                .setTitle(request.title())
                .setBody(request.body())
                .build();

        return Message.builder()
                .setToken(fcmToken)
                .setNotification(firebaseNotification)
                .putData("notificationId", String.valueOf(request.notificationId()))
                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                .build();
    }
}
