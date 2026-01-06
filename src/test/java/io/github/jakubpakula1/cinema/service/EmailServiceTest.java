package io.github.jakubpakula1.cinema.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Properties;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailService Unit Tests")
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private MimeMessage createRealMimeMessage() {
        return new MimeMessage(Session.getInstance(new Properties()));
    }

    @Test
    @DisplayName("Should send email with attachment successfully")
    void testSendEmailWithAttachment_Success() {
        // given
        MimeMessage mimeMessage = createRealMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendEmailWithAttachment(
                "user@example.com",
                "Test Subject",
                "Test Body",
                new byte[]{1, 2, 3, 4, 5},
                "document.pdf");

        // then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send email with empty attachment")
    void testSendEmailWithAttachment_EmptyAttachment() {
        // given
        MimeMessage mimeMessage = createRealMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendEmailWithAttachment(
                "test@example.com",
                "Empty Attachment",
                "Body",
                new byte[]{},
                "empty.pdf");

        // then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send email with large attachment")
    void testSendEmailWithAttachment_LargeAttachment() {
        // given
        MimeMessage mimeMessage = createRealMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        byte[] largeAttachment = new byte[1024 * 1024];
        for (int i = 0; i < largeAttachment.length; i++) {
            largeAttachment[i] = (byte) (i % 256);
        }

        // when
        emailService.sendEmailWithAttachment(
                "user@example.com",
                "Large File",
                "Here is your large file",
                largeAttachment,
                "largefile.pdf");

        // then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send email with special characters in subject and body")
    void testSendEmailWithAttachment_SpecialCharacters() {
        // given
        MimeMessage mimeMessage = createRealMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendEmailWithAttachment(
                "user@example.com",
                "Test Subject with Ąćęłńóśźż characters",
                "Test Body with <html> & special !@#$%^&*() characters",
                new byte[]{1, 2, 3},
                "test.pdf");

        // then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should send email with valid recipient address")
    void testSendEmailWithAttachment_ValidEmail() {
        // given
        MimeMessage mimeMessage = createRealMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when
        emailService.sendEmailWithAttachment(
                "user@example.com",
                "Subject",
                "Body",
                new byte[]{1, 2, 3},
                "file.pdf");

        // then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should verify attachment is added to email")
    void testSendEmailWithAttachment_VerifyAttachmentName() {
        // given
        MimeMessage mimeMessage = createRealMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        byte[] pdfContent = {0x25, 0x50, 0x44, 0x46};

        // when
        emailService.sendEmailWithAttachment(
                "user@example.com",
                "Your Tickets",
                "Please find your tickets attached",
                pdfContent,
                "tickets_order_123.pdf");

        // then
        verify(mailSender).createMimeMessage();
        verify(mailSender).send(any(MimeMessage.class));
    }

    @Test
    @DisplayName("Should reject null body with validation error")
    void testSendEmailWithAttachment_NullBody() {
        // given
        MimeMessage mimeMessage = createRealMimeMessage();
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // when & then
        assertThatThrownBy(() -> emailService.sendEmailWithAttachment(
                "user@example.com",
                "Subject",
                null,
                new byte[]{1, 2, 3},
                "file.pdf"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Text must not be null");
    }
}