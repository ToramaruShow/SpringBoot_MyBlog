package spring.boot.model.mail;

import java.io.StringWriter;
import java.util.Objects;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import jakarta.mail.internet.MimeMessage;

@Component
public class TestVelocity {
	@Autowired
	private JavaMailSender mailSender;
	@Autowired
	private VelocityEngine engine;

	@Async
	public void send(Mail mail) {
		try {
			MimeMessage message = mailSender.createMimeMessage();//外国対応で
			MimeMessageHelper helper = new MimeMessageHelper(message, true);

			//テンプレにデータをセット準備
			var context = new VelocityContext();
			context.put("name", "うさぎ");//vmの$nameに突っ込む
			context.put("webMasterEmail", "web@127.0.0.1");//vmの$webMasterEmailに突っ込む
			//メールにセット
			var writer = new StringWriter();
			engine.mergeTemplate("templates\\mail\\user_regist.vm", "utf-8", context, writer);//vmはメールのテンプレ？
			helper.setTo(mail.getTo());
			helper.setFrom(mail.getFrom());
			if (Objects.nonNull(mail.getCc())) {
				helper.setCc(mail.getCc());
			}
			if (Objects.nonNull(mail.getBcc())) {
				helper.setCc(mail.getBcc());
			}
			helper.setSubject(mail.getSubject());
			//helper.setText(mail.getMailText());
			helper.setText(writer.toString());
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
