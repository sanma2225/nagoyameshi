package com.example.nagoyameshi.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.Reservation;
import com.example.nagoyameshi.entity.Restaurant;
import com.example.nagoyameshi.form.ReservationRegisterForm;
import com.example.nagoyameshi.repository.ReservationRepository;
import com.example.nagoyameshi.repository.RestaurantRepository;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;  
    private final RestaurantRepository restaurantRepository;  
    private final UserRepository userRepository;  

    @Autowired
    public ReservationService(ReservationRepository reservationRepository, RestaurantRepository restaurantRepository, UserRepository userRepository) {
        this.reservationRepository = reservationRepository;  
        this.restaurantRepository = restaurantRepository;  
        this.userRepository = userRepository;  
    } 

    // 人数が定員以下かどうかをチェックする
    public boolean isWithinCapacity(Integer reservationCount, Integer reservationCapacity) {
        return reservationCount <= reservationCapacity;
    }
    
    @Transactional
    public void create(UserDetailsImpl userDetailsImpl, ReservationRegisterForm reservationRegisterForm) {
        Reservation reservation = new Reservation();
        Restaurant restaurant = restaurantRepository.getReferenceById(reservationRegisterForm.getRestaurantId());
        Integer user = userDetailsImpl.getUser().getId();
        
        reservation.setRestaurant(restaurant);
        reservation.setUser(user);
        reservation.setReservationDateTime(reservationRegisterForm.getReservationDateTime());
        reservation.setReservationCount(reservationRegisterForm.getReservationCount());
        
        // Save the reservation to the database
        reservationRepository.save(reservation);
    }

    @Transactional
    public void cancelReservationById(Integer id) {
        Optional<Reservation> optionalReservation = reservationRepository.findById(id);
        
        // Check if the reservation exists
        if (optionalReservation.isPresent()) {
            Reservation reservation = optionalReservation.get();
            reservationRepository.delete(reservation);  // Delete the reservation
        } else {
            // Handle the case where no reservation is found with the given ID (optional)
            throw new IllegalArgumentException("予約情報が見つかりませんでした");
        }
    }
}