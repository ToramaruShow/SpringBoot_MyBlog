package spring.boot.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import spring.boot.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer>{
	@Modifying
	@Query(value ="update blog set category_id=1 where category_id= :id",nativeQuery = true)
	public int updateBlogCategoryId(@Param("id")int id);
}
