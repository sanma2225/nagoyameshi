package com.example.nagoyameshi.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.nagoyameshi.entity.Restaurant;

 public interface RestaurantRepository extends JpaRepository<Restaurant, Integer> {
	public Page<Restaurant> findByVenueNameLike(String keyword, Pageable pageable);
	public Page<Restaurant> findByCategoryId(Integer categoryId, Pageable pageable);
	@Query("SELECT r FROM Restaurant r WHERE r.venueName LIKE %:keyword% OR r.description LIKE %:keyword% ORDER BY r.budgetRange ASC")
	Page<Restaurant> findByVenueNameOrDescriptionLikeOrderByBudgetRangeAsc(@Param("keyword") String keyword, Pageable pageable);
	@Query("SELECT r FROM Restaurant r WHERE r.venueName LIKE %:keyword% OR r.description LIKE %:keyword% ORDER BY r.budgetRange DESC")
	Page<Restaurant> findByVenueNameOrDescriptionLikeOrderByBudgetRangeDesc(@Param("keyword") String keyword, Pageable pageable);
	@Query("SELECT r FROM Restaurant r WHERE r.venueName LIKE %:keyword% OR r.description LIKE %:keyword% ORDER BY r.createdAt DESC")
	Page<Restaurant> findByVenueNameOrDescriptionLikeOrderByCreatedAtDesc(@Param("keyword") String keyword, Pageable pageable);
	@Query("SELECT r FROM Restaurant r " +
		       "LEFT JOIN Review rv ON r.id = rv.restaurant.id " +
		       "WHERE r.venueName LIKE %:keyword% OR r.description LIKE %:keyword% " +
		       "GROUP BY r.id " +
		       "ORDER BY COUNT(rv.id) DESC")
	Page<Restaurant> findByVenueNameOrDescriptionLikeSortedByReviewCount(@Param("keyword") String keyword, Pageable pageable);
	@Query("SELECT r FROM Restaurant r " +
		       "LEFT JOIN Review rv ON r.id = rv.restaurant.id " +
		       "WHERE r.venueName LIKE %:keyword% OR r.description LIKE %:keyword% " +
		       "GROUP BY r.id " +
		       "ORDER BY AVG(rv.starId) DESC")
	Page<Restaurant> findByVenueNameOrDescriptionLikeSortedByAverageScore(@Param("keyword") String keyword, Pageable pageable);
//	
	public Page<Restaurant> findByCategoryIdOrderByBudgetRangeAsc(Integer categoryId, Pageable pageable);
	public Page<Restaurant> findByCategoryIdOrderByBudgetRangeDesc(Integer categoryId, Pageable pageable);
	public Page<Restaurant> findByCategoryIdOrderByCreatedAtDesc (Integer categoryId, Pageable pageable);
	@Query("SELECT r FROM Restaurant r " +
			   "LEFT JOIN Review rv ON r.id = rv.restaurant.id " +
			   "WHERE r.category.id = :categoryId " +
			   "GROUP BY r.id " +
	           "ORDER BY COUNT(rv.id) DESC")
	public Page<Restaurant> findByCategoryIdSortedByReviewCount(Integer categoryId, Pageable pageable);
	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN Review rv ON r.id = rv.restaurant.id " +
			"WHERE r.category.id = :categoryId " +
			"GROUP BY r.id ORDER BY AVG(rv.starId) DESC")
	public Page<Restaurant> findByCategoryIdSortedByAverageScore(Integer categoryId, Pageable pageable);	
//	
	public Page<Restaurant> findByBudgetRangeOrderByBudgetRangeAsc(Integer budgetRange, Pageable pageable); 
    public Page<Restaurant> findByBudgetRangeOrderByBudgetRangeDesc(Integer budgetRange, Pageable pageable); 
    public Page<Restaurant> findByBudgetRangeOrderByCreatedAtDesc(Integer budgetRange, Pageable pageable);  
    @Query("SELECT r FROM Restaurant r " +
 		   "LEFT JOIN Review rv ON r.id = rv.restaurant.id " +
 		   "WHERE r.budgetRange = :budgetRange " + 
            "GROUP BY r.id " +
            "ORDER BY COUNT(rv.id) DESC")
    public Page<Restaurant> findByBudgetRangeSortedByReviewCount(Integer budgetRange, Pageable pageable);
    @Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN Review rv ON r.id = rv.restaurant.id " +
			"WHERE r.budgetRange = :budgetRange " + 
			"GROUP BY r.id ORDER BY AVG(rv.starId) DESC")
    public Page<Restaurant> findByBudgetRangeSortedByAverageScore(Integer budgetRange, Pageable pageable);
//    
    public Page<Restaurant> findAllByOrderByBudgetRangeAsc(Pageable pageable);
    public Page<Restaurant> findAllByOrderByBudgetRangeDesc(Pageable pageable);  
    public Page<Restaurant> findAllByOrderByCreatedAtDesc(Pageable pageable); 
	public Restaurant getRestaurantById(Integer id);
	@Query("SELECT r FROM Restaurant r " +
		   "LEFT JOIN Review rv ON r.id = rv.restaurant.id " +
           "GROUP BY r.id " +
           "ORDER BY COUNT(rv.id) DESC")
    Page<Restaurant> findAllSortedByReviewCount(Pageable pageable);
	@Query("SELECT r FROM Restaurant r " +
			"LEFT JOIN Review rv ON r.id = rv.restaurant.id " +
			"GROUP BY r.id ORDER BY AVG(rv.starId) DESC")
	Page<Restaurant> findAllSortedByAverageScore(Pageable pageable);
	
	public List<Restaurant> findTop8RestaurantsByOrderByCreatedAtDesc();   
	
	@Query("SELECT r FROM Restaurant r " +
	 		   "LEFT JOIN Review rv ON r.id = rv.restaurant.id " +
	            "GROUP BY r.id " +
	            "ORDER BY COUNT(rv.id) DESC")
	public List<Restaurant> findTop3RestaurantsByOrderByReviewCountDesc(Pageable pageable);
	    
 }