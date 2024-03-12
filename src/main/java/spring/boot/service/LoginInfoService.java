package spring.boot.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.transaction.Transactional;
import spring.boot.model.user.LoginInfo;
import spring.boot.model.user.LoginInfoKey;
import spring.boot.service.repository.LoginInfoRepository;

//ここらはDB！
@Service
public class LoginInfoService {
	@Autowired
	private LoginInfoRepository repository;

	@Autowired
	private EntityManagerFactory factory;
	private EntityManager entityManager;

	@PostConstruct
	private void init() {
		entityManager = factory.createEntityManager();
	}

	//ユーザーID重複してるか？
	public boolean isRegistUserId(String userId) {
		if (repository.isResultUserId(userId) == 0) {
			return false;
		}
		return true;
	}

	//メール重複してるか？
	public boolean isRegistUserEmail(String email) {
		if (repository.isResultUserEmail(email) == 0) {
			return false;
		}
		return true;
	}

	@Transactional
	public LoginInfo save(LoginInfo loginInfo) {
		return repository.save(loginInfo);
	}

	public String findUser(LoginInfo loginInfo) {
		return repository.findUser(loginInfo.getEmail(), loginInfo.getPasswd());
	}

	public List<LoginInfo> getAllData() {
		return repository.findAll();
	}

	@Transactional
	public void delete(LoginInfoKey infoKey) {
		repository.deleteById(infoKey);//logininfokeyにつながってれば勝手に消してくれる
	}

	@Transactional
	public void blogUserRegist(LoginInfo loginInfo) throws Exception {//テーブルを作成しつつログインINFOに
		//カテゴリーテーブル作成　StringBuilderで
		var createSqlCategory = new StringBuilder();
		createSqlCategory.append("CREATE TABLE IF NOT EXISTS");//category_username って感じでテーブルが作られる
		createSqlCategory.append(String.format(" category_%s(", loginInfo.getUserId()));//カテゴリーの後ろのスペースつけよう
		createSqlCategory.append("id INT(3) AUTO_INCREMENT PRIMARY KEY,");
		createSqlCategory.append("name VARCHAR(10) NOT NULL,");
		createSqlCategory.append("description TEXT)");//最後のセミコロンはいらないのか？
		//カテゴリーテーブルにデータ登録
		var insertSql = String.format("INSERT INTO category_%s(name, description) values('なんでも','とりあえず用')",
				loginInfo.getUserId());

		var createSqlBlog = new StringBuilder();//ブログテーブル作成
		createSqlBlog.append(String.format("CREATE TABLE IF NOT EXISTS blog_%s(", loginInfo.getUserId()));
		createSqlBlog.append("id INT(3) AUTO_INCREMENT PRIMARY KEY,");
		createSqlBlog.append("category_id INT (3) DEFAULT 1,");
		createSqlBlog.append("title VARCHAR(30) NOT NULL,");
		createSqlBlog.append("body TEXT,");
		createSqlBlog.append("regist_date DATETIME NOT NULL DEFAULT now(),");
		createSqlBlog.append("update_date DATETIME,");
		createSqlBlog.append(
				String.format("FOREIGN KEY ctg_fk_%s(category_id) REFERENCES category_%s(id))", loginInfo.getUserId(),
						loginInfo.getUserId()));
		//CURD実行　さてSQLを実行しましょうか
		EntityTransaction entityTransaction = null;
		try {
			entityTransaction = entityManager.getTransaction();
			entityTransaction.begin();
			//ユーザ登録
			save(loginInfo);
			//カテゴリテーブル作成
			entityManager.createNativeQuery(createSqlCategory.toString()).executeUpdate();
			//カテゴリテーブルにデフォルト値登録
			entityManager.createNativeQuery(insertSql.toString()).executeUpdate();
			//ブログテーブル作成
			entityManager.createNativeQuery(createSqlBlog.toString()).executeUpdate();

			entityTransaction.commit();//beginしたらcommit
		} catch (Throwable e) {//begin~commitでエラーがあれば
			if (Objects.nonNull(entityTransaction) && entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			e.printStackTrace();
			throw e;
		}
	}

	@Transactional
	public void blogUserCancel(LoginInfoKey loginInfokey) throws Exception {//テーブルを作成しつつログインINFOに
		//カテゴリ DEL
		String cancelSqlCategory = String.format("drop table category_%s", loginInfokey.getUserId());
		//ブログ DEL
		String cancelSqlBlog = String.format("drop table blog_%s", loginInfokey.getUserId());
		//CURD実行　さてSQLを実行しましょうか
		EntityTransaction entityTransaction = null;
		try {
			entityTransaction = entityManager.getTransaction();
			entityTransaction.begin();
			//ユーザ登録
			delete(loginInfokey);
			//ブログテーブル DEL
			entityManager.createNativeQuery(cancelSqlBlog.toString()).executeUpdate();
			//カテゴリテーブル DEL
			entityManager.createNativeQuery(cancelSqlCategory.toString()).executeUpdate();
			entityTransaction.commit();//beginしたらcommit
		} catch (Throwable e) {//begin~commitでエラーがあれば
			if (Objects.nonNull(entityTransaction) && entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			e.printStackTrace();
			throw e;
		}
	}
}
