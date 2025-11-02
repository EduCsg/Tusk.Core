package com.hydra.core.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailSender {

	private final JavaMailSender mailSender;

	public EmailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void sendHtmlMail(String to, String subject, String htmlBody) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			helper.setFrom("seu@email.com");
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlBody, true);
			mailSender.send(mimeMessage);
		} catch (MessagingException e) {
			throw new RuntimeException("Erro ao enviar email HTML", e);
		}
	}

}