package spring.boot.model.mail;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class Mail {
	private String[] to;
	private String from;
	private String[] cc;
	private String[] bcc;
	private String subject;
	private String mailText;
}
