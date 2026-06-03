package com.vocabverse.notification.email;

import com.vocabverse.async.event.ReviewDueEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final EmailSender emailSender;

    @Value("${notification.review-link:/reviews/today}")
    private String reviewLink;

    public void sendReviewDueEmail(ReviewDueEvent event) {
        String displayName = event.fullName() == null || event.fullName().isBlank()
                ? "there"
                : event.fullName();
        String subject = "Your VocabVerse review is due";
        String body = """
                Hi %s,

                You have %d vocabularies due for review today.

                Review link: %s

                A short review today helps keep these words fresh. See you in VocabVerse.
                """.formatted(displayName, event.totalDueVocabularies(), reviewLink);

        emailSender.send(event.email(), subject, body);
    }
}
