package spring.boot.model;

import org.hibernate.validator.constraints.Length;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table(name = "blog")
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Blog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private int categoryId;
	@Transient
	private String category;
	@NotEmpty
	@Length(min = 1, max = 30)
	private String title;
	@NotEmpty
	private String body;
	@Column(insertable = false, updatable = false)
	private String registDate;
	@Column(insertable = false)
	private String updateDate;
}
