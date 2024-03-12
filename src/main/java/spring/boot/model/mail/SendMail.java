package spring.boot.model.mail;

import java.io.StringWriter;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

@Component
public class SendMail {
	@Autowired
	private JavaMailSender mailSender;

	@Async
	public void send(Mail mail) {
		send(mail, null);
	}

	@Async
	public void send(Mail mail, StringWriter writer) {
		try {
			MimeMessage message = mailSender.createMimeMessage();//外国対応で
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(mail.getTo());
			helper.setFrom(mail.getFrom());
			if (Objects.nonNull(mail.getCc())) {
				helper.setCc(mail.getCc());
			}
			if (Objects.nonNull(mail.getBcc())) {
				helper.setCc(mail.getBcc());
			}
			helper.setSubject(mail.getSubject());
			String body = writer.toString();
			if (Objects.isNull(writer)) {
//				body = mail.getMailText();
				helper.setText(mail.getMailText());
			} else {
				helper.setText(body);
			}
			mailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void send() {
		try {
			MimeMessage message = mailSender.createMimeMessage();//外国対応で
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo("web@127.0.0.1");
			helper.setText("なかみ");
			helper.setSubject("タイトル");
			mailSender.send(message);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
