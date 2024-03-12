package spring.boot.service;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.Getter;
import spring.boot.model.Blog;
import spring.boot.model.Category;
import spring.boot.service.repository.CategoryRepository;

@Service
public class CategoryService {
	private static final int NO_DELETE_NO = 0;
	@Autowired
	private CategoryRepository repository;

	@Autowired
	private EntityManagerFactory factory;
	private EntityManager entityManager;

	@Getter
	private String tableName;

	@PostConstruct
	private void init() {
		//		categoryNameList = findAll();
		entityManager = factory.createEntityManager();
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
		categoryNameList = findAll();
	}

	@Getter
	private List<Category> categoryNameList;

	//	@PostConstruct
	//	private void init() {
	//		//setCaterogyNameList();
	//		categoryNameList = findAll();
	//	}

	@SuppressWarnings("unchecked")
	public List<Category> findAll() {
		if (Objects.isNull(tableName)) {
			return repository.findAll();
		}
		List<Category> list = null;
		try {
			entityManager.clear();//UPDATEのバグがあるのでこれをつけとく　番号をつけなおすらしい
			Query query = entityManager.createNativeQuery(String.format("select * from category_%s", tableName),
					Category.class);
			list = query.getResultList();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	//一件だけ抽出
	public Category getReferenceById(int id) {
		if (Objects.isNull(tableName)) {
			return repository.getReferenceById(id);
		}
		String selectSql = "select * from category_%s where id = ?";
		Query query = entityManager.createNativeQuery(String.format(selectSql, tableName), Category.class);
		return (Category) query.setParameter(1, id).getSingleResult();
	}

	//登録・更新
	public Category save(Category category) {
		return repository.save(category);
	}

	//登録 登録テーブル可変　対応
	@Transactional
	public int regist(Category category) throws Exception {
		String insertSql = "insert into category_%s(name, description) values(?, ?)";
		Query query = entityManager.createNativeQuery(String.format(insertSql, tableName), Category.class);
		query.setParameter(1, category.getName());
		query.setParameter(2, category.getDescription());
		//CRUD実行
		int result = 0;
		EntityTransaction entityTransaction = null;
		try {
			entityTransaction = entityManager.getTransaction();
			entityTransaction.begin();
			result = query.executeUpdate();//SQL実行
			entityTransaction.commit();//eT閉じる
		} catch (Throwable e) {
			if (Objects.nonNull(entityTransaction) && entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			throw e;
		}
		return result;
	}

	@Transactional
	public int update(Category category) throws Exception {
		String insertSql = "update category_%s set name = ?, description = ? where id = ?";
		Query query = entityManager.createNativeQuery(String.format(insertSql, tableName), Category.class);
		query.setParameter(1, category.getName());
		query.setParameter(2, category.getDescription());
		query.setParameter(3, category.getId());
		//CRUD実行
		int result = 0;
		EntityTransaction entityTransaction = null;
		try {
			entityTransaction = entityManager.getTransaction();
			entityTransaction.begin();
			result = query.executeUpdate();//SQL実行
			entityTransaction.commit();//eT閉じる
		} catch (Throwable e) {
			if (Objects.nonNull(entityTransaction) && entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			throw e;
		}
		return result;
	}

	@Transactional
	public void deleteById(int id) throws Exception {
		if (id == NO_DELETE_NO) {//idが1は消せない！
			return;
		}
		if (Objects.isNull(tableName)) {
			//			repository.updateBlogCategoryId(id);
			repository.deleteById(id);
			return;
		}
		String updateSql = "update blog_%s set category_id=1 where id = ?";
		String deleteSql = "delete from category_%s where id = ?";
		Query updateQuery = entityManager.createNativeQuery(String.format(updateSql, tableName), Blog.class);
		updateQuery.setParameter(1, id);//UPDATE
		Query deleteQuery = entityManager.createNativeQuery(String.format(deleteSql, tableName), Category.class);
		deleteQuery.setParameter(1, id);//DELETE
		//CRUD実行
		EntityTransaction entityTransaction = null;
		try {
			entityTransaction = entityManager.getTransaction();
			entityTransaction.begin();
			updateQuery.executeUpdate();//SQL実行
			deleteQuery.executeUpdate();//SQL実行
			entityTransaction.commit();//eT閉じる
		} catch (Throwable e) {
			if (Objects.nonNull(entityTransaction) && entityTransaction.isActive()) {
				entityTransaction.rollback();
			}
			throw e;
		}
	}

	@Transactional
	public void deleteById_(int id) {
		if (id != NO_DELETE_NO) {
			repository.updateBlogCategoryId(id);
			repository.deleteById(id);
		}
	}

	//	private void setCaterogyNameList() {
	//		var categoryName = new ArrayList<String>();
	//		findAll().forEach(item -> categoryName.add(item.getName()));//itemにひとつづつ入れる
	//		categoryNameList = categoryName.toArray(new String[] {});
	//	}
}
