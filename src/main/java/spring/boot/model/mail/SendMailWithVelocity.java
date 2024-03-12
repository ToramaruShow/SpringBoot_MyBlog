package spring.boot.model.mail;

import java.io.StringWriter;
import java.util.ResourceBundle;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;
import spring.boot.model.BlogConfig;
import spring.boot.model.user.LoginInfo;
import spring.boot.model.user.LoginInfoKey;

@Component
public class SendMailWithVelocity extends SendMail {
	@Autowired
	private VelocityEngine engine;

	@Getter
	@Setter
	private ResourceBundle resource;

	public SendMailWithVelocity() {
		resource = ResourceBundle.getBundle(BlogConfig.RESOURCE_NAME);
	}
	/***ユーザ登録時にメール送信*/
	public void userRegistMail(LoginInfo loginInfo) {
		var mail = new Mail(
				new String[] { loginInfo.getEmail() },
				resource.getString("mail.webmaster"),
				new String[] { resource.getString("mail.webmaster") },
				null,
				resource.getString("mail.regist.subject"),
				null);
		var context = new VelocityContext();
		context.put("name", loginInfo.getUserId());//vmの$nameに突っ込む
		context.put("webMasterEmail", resource.getString("mail.webmaster"));//vmの$webMasterEmailに突っ込む
		var writer = new StringWriter();
		engine.mergeTemplate(resource.getString("mail.temp.user.regist"), "utf-8", context, writer);//vmはメールのテンプレ？
		send(mail,writer);
	}
	/***ユーザ登録時にメール送信*/
	public void userCancelMail(LoginInfoKey loginInfo) {
		var mail = new Mail(
				new String[] { loginInfo.getEmail() },
				resource.getString("mail.webmaster"),
				new String[] { resource.getString("mail.webmaster") },
				null,
				resource.getString("mail.cancel.subject"),
				null);
		var context = new VelocityContext();
		context.put("name", loginInfo.getUserId());//vmの$nameに突っ込む
		context.put("webMasterEmail", resource.getString("mail.webmaster"));//vmの$webMasterEmailに突っ込む
		var writer = new StringWriter();
		engine.mergeTemplate(resource.getString("mail.temp.user.cancel"), "utf-8", context, writer);//vmはメールのテンプレ？
		send(mail,writer);
	}
}
