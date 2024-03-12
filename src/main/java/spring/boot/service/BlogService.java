package spring.boot.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.Setter;
import spring.boot.model.Blog;
import spring.boot.service.repository.BlogRepository;

@Service
public class BlogService {
	@Autowired
	private BlogRepository repository;

	@Autowired
	private EntityManagerFactory factory;
	private EntityManager entityManager;

	@Getter
	@Setter
	private String tableName;

	@PostConstruct
	private void init() {
		entityManager = factory.createEntityManager();
	}

	//DBから一覧取得
	@SuppressWarnings("unchecked")
	public List<Blog> findAll() {
		if (Objects.isNull(tableName)) {
			return repository.findAll();
		}
		List<Blog> list = null;
		try {
			entityManager.clear();//UPDATEのバグがあるのでこれをつけとく　番号をつけなおすらしい
			Query query = entityManager.createNativeQuery(String.format("select * from blog_%s", tableName),
					Blog.class);
			list = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public Page<Blog> findAll(Pageable pageable) {//一度取得したテーブルデータをPAGEクラスにする
		List<Blog> list = findAll();
		int start = (int) pageable.getOffset();
		int end = Math.min((start + pageable.getPageSize()), list.size());
		List<Blog> pageContent = list.subList(start, end);
		return new PageImpl<>(pageContent, pageable, list.size());
	}

	public Page<Blog> findAllWithBlog(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public Blog getReferenceById(int id) {
		if (Objects.isNull(tableName)) {
			return repository.getReferenceById(id);
		}
		Query query = entityManager.createNativeQuery(String.format("select * from blog_%s where id = ?", tableName),
				Blog.class);
		return (Blog) query.setParameter(1, id).getSingleResult();
	}

	//セーブ
	public Blog save(Blog blog) throws Exception {//登録した内容が反映・戻る　（JDBCは数字らしい）
		//日付取得
		LocalDateTime nowTime = LocalDateTime.now();
		blog.setUpdateDate(nowTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
		return repository.save(blog);
	}

	@Transactional //一件しかないけれども...
	public int regist(Blog blog) {
		String insertSql = "insert into blog_%s (category_id, title, body) values(?, ?, ?)";
		Query query = entityManager.createNativeQuery(String.format(insertSql, tableName), Blog.class)
				.setParameter(1, blog.getCategoryId())
				.setParameter(2, blog.getTitle())
				.setParameter(3, blog.getBody());
		int result = runsql(query);
		return result;
	}

	@Transactional //一件しかないけれども...
	public int update(Blog blog) {
		//update blog set category_id=1 where category_id= :id
		String updateSql = "UPDATE blog_%s SET category_id = ?, title = ?, body = ?, update_date = now() WHERE id = ?";
		Query query = entityManager.createNativeQuery(String.format(updateSql, tableName), Blog.class)
				.setParameter(1, blog.getCategoryId())
				.setParameter(2, blog.getTitle())
				.setParameter(3, blog.getBody())
				.setParameter(4, blog.getId());
		int result = runsql(query);
		return result;
	}

	@Transactional //一件しかないけれども...
	public int delete(Blog blog) {
		if (Objects.isNull(blog.getId())) {
			repository.deleteById(blog.getId());
			return blog.getId();
		}
		//update blog set category_id=1 where category_id= :id
		String deleteSql = "delete from blog_%s where id = ?";
		Query query = entityManager.createNativeQuery(String.format(deleteSql, tableName), Blog.class)
				.setParameter(1, blog.getId());
		int result = runsql(query);
		return result;
	}

	private int runsql(Query query) {
		int result = 0;
		EntityTransaction entityTransaction = null;
		try {
			entityTransaction = entityManager.getTransaction();
			entityTransaction.begin();
			result = query.executeUpdate();
			entityTransaction.commit();//どこかでbegin commitやったね～
		} catch (Throwable e) {
			if (Objects.nonNull(entityTransaction) && entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			throw e;
		}
		return result;
	}

	//ID使って削除
	public void deleteById(int id) {
		repository.deleteById(id);
	}
}

//@Transactional //一件しかないけれども...
//public int regist(Blog blog) {
//	String insertSql = "insert into blog_%s (category_id, title, body) values(?, ?, ?)";
//	Query query = entityManager.createNativeQuery(String.format(insertSql, tableName), Blog.class)
//			.setParameter(1, blog.getCategoryId())
//			.setParameter(2, blog.getTitle())
//			.setParameter(3, blog.getBody());
//	int result = 0;
//	EntityTransaction entityTransaction = null;
//	try {
//		entityTransaction = entityManager.getTransaction();
//		entityTransaction.begin();
//		result = query.executeUpdate();
//		entityTransaction.commit();//どこかでbegin commitやったね～
//	} catch (Throwable e) {
//		if (Objects.nonNull(entityTransaction) && entityTransaction.isActive()) {
//			entityTransaction.rollback();
//		}
//		throw e;
//	}
//	return result;
//}
//
//@Transactional //一件しかないけれども...
//public int update(Blog blog) {
//	//update blog set category_id=1 where category_id= :id
//	String insertSql = "UPDATE blog_%s SET title = ?, body = ? WHERE category_id = ?";
//	Query query = entityManager.createNativeQuery(String.format(insertSql, tableName), Blog.class)
//			.setParameter(1, blog.getTitle())
//			.setParameter(2, blog.getBody())
//			.setParameter(3, blog.getCategoryId());
//	int result = 0;
//	EntityTransaction entityTransaction = null;
//	try {
//		entityTransaction = entityManager.getTransaction();
//		entityTransaction.begin();
//		result = query.executeUpdate();
//		entityTransaction.commit();//どこかでbegin commitやったね～
//	} catch (Throwable e) {
//		if (Objects.nonNull(entityTransaction) && entityTransaction.isActive()) {
//			entityTransaction.rollback();
//		}
//		throw e;
//	}
//	return result;
//}
//
//@Transactional //一件しかないけれども...
//public int delete(Blog blog) {
//	//update blog set category_id=1 where category_id= :id
//	String insertSql = "delete from blog_%s where category_id = ?";
//	Query query = entityManager.createNativeQuery(String.format(insertSql, tableName), Blog.class)
//			.setParameter(1, blog.getCategoryId());
//	int result = 0;
//	EntityTransaction entityTransaction = null;
//	try {
//		entityTransaction = entityManager.getTransaction();
//		entityTransaction.begin();
//		result = query.executeUpdate();
//		entityTransaction.commit();//どこかでbegin commitやったね～
//	} catch (Throwable e) {
//		if (Objects.nonNull(entityTransaction) && entityTransaction.isActive()) {
//			entityTransaction.rollback();
//		}
//		throw e;
//	}
//	return result;
//}
