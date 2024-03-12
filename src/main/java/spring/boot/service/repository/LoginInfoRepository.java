package spring.boot.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import spring.boot.model.user.LoginInfo;
import spring.boot.model.user.LoginInfoKey;

@Repository
public interface LoginInfoRepository extends JpaRepository<LoginInfo, LoginInfoKey> {
	//ユーザーID検索
	@Query(value = "select count(*) from blog_login_info"
			+ "\nwhere user_id= :user_id", nativeQuery = true)
	public int isResultUserId(@Param("user_id") String user_id);

	//ユーザーID検索
	@Query(value = "select count(*) from blog_login_info"
			+ "\nwhere email= :email", nativeQuery = true)
	public int isResultUserEmail(@Param("email") String email);

	@Query(value = "select user_id from blog_login_info"
			+ "\nwhere email= :email and passwd= :passwd", nativeQuery = true)
	public String findUser(@Param("email") String email, @Param("passwd") String passwd);

	@Transactional
	@Modifying
	@Query(value = "update blog_login_info set passwd = :pass where user_id = :user_id", nativeQuery = true)
	public void changePass(@Param("pass") String pass, @Param("user_id")String a);
	
	@Transactional
	@Modifying
	@Query(value = "update blog_login_info set email = :email where user_id = :user_id", nativeQuery = true)
	public void changeMail(@Param("email") String mail, @Param("user_id")String a);
}
