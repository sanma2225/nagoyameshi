package com.example.nagoyameshi.service;
 
 import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.RestaurantEditForm;
import com.example.nagoyameshi.form.RestaurantRegisterForm;
import com.example.nagoyameshi.repository.FavoriteRepository;
import com.example.nagoyameshi.repository.ReservationRepository;
import com.example.nagoyameshi.repository.RestaurantRepository;
import com.example.nagoyameshi.repository.ReviewRepository;
 
 @Service
 public class RestaurantService {
     private final RestaurantRepository restaurantRepository;  
     private final FavoriteRepository favoriteRepository; 
     private final ReservationRepository reservationRepository; 
     private final ReviewRepository reviewRepository; 
     
     public RestaurantService(RestaurantRepository restaurantRepository,
    		 FavoriteRepository favoriteRepository,
    		 ReservationRepository reservationRepository,
    		 ReviewRepository reviewRepository) {
         this.restaurantRepository = restaurantRepository;  
         this.reviewRepository = reviewRepository;
		 this.reservationRepository = reservationRepository;
		 this.favoriteRepository = favoriteRepository;
     }    
     
     @Transactional
     public void create(RestaurantRegisterForm restaurantRegisterForm) {
    	 Restaurant restaurant = new Restaurant();        
         MultipartFile imageFile = restaurantRegisterForm.getImageFile();
         
         if (!imageFile.isEmpty()) {
             String imageName = imageFile.getOriginalFilename(); 
             String hashedImageName = generateNewFileName(imageName);
             Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
             copyImageFile(imageFile, filePath);
             restaurant.setImageName(hashedImageName);
         }
         
         restaurant.setVenueName(restaurantRegisterForm.getVenueName()); 
         restaurant.setCategory(restaurantRegisterForm.getCategory());
         restaurant.setDescription(restaurantRegisterForm.getDescription());
         restaurant.setPostalCode(restaurantRegisterForm.getPostalCode());
         restaurant.setAddress(restaurantRegisterForm.getAddress());
         restaurant.setOpeningHour(restaurantRegisterForm.getOpeningHour());
         restaurant.setClosingHour(restaurantRegisterForm.getClosingHour());
         restaurant.setReservationCapacity(restaurantRegisterForm.getReservationCapacity());
         restaurant.setBudgetRange(restaurantRegisterForm.getBudgetRange());
                     
         restaurantRepository.save(restaurant);
     }  
     
     @Transactional
     public void update(RestaurantEditForm restaurantEditForm) {
    	 Restaurant restaurant = restaurantRepository.getReferenceById(restaurantEditForm.getId());
         MultipartFile imageFile = restaurantEditForm.getImageFile();
         
         if (!imageFile.isEmpty()) {
             String imageName = imageFile.getOriginalFilename(); 
             String hashedImageName = generateNewFileName(imageName);
             Path filePath = Paths.get("src/main/resources/static/storage/" + hashedImageName);
             copyImageFile(imageFile, filePath);
             restaurant.setImageName(hashedImageName);
         }
         
         restaurant.setVenueName(restaurantEditForm.getVenueName()); 
         restaurant.setCategory(restaurantEditForm.getCategory());
         restaurant.setDescription(restaurantEditForm.getDescription());
         restaurant.setPostalCode(restaurantEditForm.getPostalCode());
         restaurant.setAddress(restaurantEditForm.getAddress());
         restaurant.setOpeningHour(restaurantEditForm.getOpeningHour());
         restaurant.setClosingHour(restaurantEditForm.getClosingHour());
         restaurant.setReservationCapacity(restaurantEditForm.getReservationCapacity());
         restaurant.setBudgetRange(restaurantEditForm.getBudgetRange());
                     
         restaurantRepository.save(restaurant);
     }    
     
     // UUIDを使って生成したファイル名を返す
     public String generateNewFileName(String fileName) {
         String[] fileNames = fileName.split("\\.");                
         for (int i = 0; i < fileNames.length - 1; i++) {
             fileNames[i] = UUID.randomUUID().toString();            
         }
         String hashedFileName = String.join(".", fileNames);
         return hashedFileName;
     }     
     
     // 画像ファイルを指定したファイルにコピーする
     public void copyImageFile(MultipartFile imageFile, Path filePath) {           
         try {
             Files.copy(imageFile.getInputStream(), filePath);
         } catch (IOException e) {
             e.printStackTrace();
         }          
     }

     public Page<Restaurant> findAllByOrderByReviewCountDesc(Pageable pageable) {
    	 return restaurantRepository.findAllSortedByReviewCount(pageable);
    	}

    public Page<Restaurant> findAllByOrderByAverageScoreDesc(Pageable pageable) {
    	return restaurantRepository.findAllSortedByAverageScore(pageable);
		}
    
 // 作成日時が新しい順に8件を取得する
    public List<Restaurant> findTop8RestaurantsByOrderByCreatedAtDesc() {
        return restaurantRepository.findTop8RestaurantsByOrderByCreatedAtDesc();
    }

    // 予約数が多い順に3件を取得する
    public List<Restaurant> findTop3RestaurantsByOrderByReviewCountDesc() {
        return restaurantRepository.findTop3RestaurantsByOrderByReviewCountDesc(PageRequest.of(0, 3));
    }  
    
    @Transactional
    public void deleteRestaurant(Integer id) {
        // First, delete all favorites associated with the restaurant
    	 favoriteRepository.deleteByRestaurantId(id);
	   	 reviewRepository.deleteByRestaurantId(id);
	   	 reservationRepository.deleteByRestaurantId(id);

        // Now, delete the restaurant itself
         restaurantRepository.deleteById(id);
    }
   
 }