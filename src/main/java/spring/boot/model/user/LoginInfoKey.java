package spring.boot.model.user;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginInfoKey implements Serializable{
	private String userId;
	private String email;
}//主キーは２個！

