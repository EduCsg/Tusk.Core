package com.hydra.core.service;

import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailSenderTest {

	@Mock
	private JavaMailSender mailSender;

	@Mock
	private MimeMessage mimeMessage;

	@InjectMocks
	private EmailSender emailSender;

	@BeforeEach
	void setup() {
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
	}

	@Test
	void shouldSendHtmlEmail() throws Exception {
		String to = "test@example.com";
		String subject = "Test Subject";
		String body = "<h1>Hello</h1>";

		emailSender.sendHtmlMail(to, subject, body);

		verify(mailSender).createMimeMessage();
		verify(mailSender).send(mimeMessage);
	}

	@Test
	void shouldThrowRuntimeExceptionWhenMessagingFails() {
		when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Erro interno"));

		assertThrows(RuntimeException.class, () -> emailSender.sendHtmlMail("a@a.com", "Test", "<p>Body</p>"));
	}

	@Test
	void shouldThrowRuntimeExceptionWhenMessagingExceptionOccurs() throws Exception {
		doThrow(new MessagingException("falha simulada")).when(mimeMessage).setFrom((Address) any());

		assertThatThrownBy(() -> emailSender.sendHtmlMail("a@a.com", "Assunto", "<p>Body</p>")).isInstanceOf(
				RuntimeException.class).hasMessageContaining("Erro ao enviar email HTML").hasCauseInstanceOf(
				MessagingException.class);
	}

}