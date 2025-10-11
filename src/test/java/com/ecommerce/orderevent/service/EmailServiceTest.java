package com.ecommerce.orderevent.service;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

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
    void testSendEmail_ShouldCallMailSender() throws Exception {
        // given
        String to = "test@example.com";
        String subject = "Order Confirmation";
        String htmlContent = "<h1>Your order has been placed!</h1>";

        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendEmail(to, subject, htmlContent);

        // then
        ArgumentCaptor<MimeMessage> messageCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(messageCaptor.capture());

        MimeMessage sentMessage = messageCaptor.getValue();
        // Cannot read subject/content from mocked MimeMessage, just ensure send() was called
        assertThat(sentMessage).isNotNull();
    }
}
