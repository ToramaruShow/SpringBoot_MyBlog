package spring.boot.model.user;

import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "blog_login_info")
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@IdClass(value = LoginInfoKey.class)
public class LoginInfo {
	@Id
	@Column(updatable = false)
	@Length(max = 20)
	@NotEmpty
	private String userId;
	@NotEmpty
	private String passwd;
	@NotEmpty
	@Id
	@Email
	@Length(max = 30)
	private String email;
	@Column(insertable = false, updatable = false)
	private String registDate;
	@Column(insertable = false)
	private String updateDate;
}
