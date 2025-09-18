package com.ecommerce.orderevent.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

class EmailServiceTest {
    private JavaMailSender mailSender;
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        emailService = new EmailService(mailSender);
    }

    @Test
    void testSendEmail_ShouldSendCorrectMessage() {
        // given
        String to = "test@example.com";
        String subject = "Order Confirmation";
        String text = "Your order has been placed!";

        // when
        emailService.sendEmail(to, subject, text);

        // then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();

        assertThat(sentMessage.getTo()).containsExactly(to);
        assertThat(sentMessage.getSubject()).isEqualTo(subject);
        assertThat(sentMessage.getText()).isEqualTo(text);
        assertThat(sentMessage.getFrom()).isEqualTo("soumyadeepghosh9800@gmail.com");
    }

}
