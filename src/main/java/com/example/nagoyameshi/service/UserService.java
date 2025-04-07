package com.example.nagoyameshi.service;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.PasswordResetForm;
import com.example.nagoyameshi.form.SignupForm;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;       
        this.passwordEncoder = passwordEncoder;
    }    
    
    @Transactional
    public User create(SignupForm signupForm) {
        User user = new User();
        boolean isPaidLicenseChecked = signupForm.isPaidLicense();
        
        user.setFullName(signupForm.getFullName());
        user.setKana(signupForm.getKana());
        user.setEmail(signupForm.getEmail());
        user.setPassword(passwordEncoder.encode(signupForm.getPassword()));
        user.setPaidLicense(isPaidLicenseChecked);
        user.setAdmin(false); 
        user.setEnabled(false);
        
        return userRepository.save(user);
    } 
    
    @Transactional
    public void update(UserEditForm userEditForm) {
        User user = userRepository.getReferenceById(userEditForm.getId());
        
        user.setFullName(userEditForm.getFullName());
        user.setKana(userEditForm.getKana());
        user.setEmail(userEditForm.getEmail());      
        
        userRepository.save(user);
    }    
    
    // メールアドレスが登録済みかどうかをチェックする
    public boolean isEmailRegistered(String email) {
        User user = userRepository.findByEmail(email);  
        return user != null;
    }  
    
    // パスワードとパスワード（確認用）の入力値が一致するかどうかをチェックする
    public boolean isSamePassword(String password, String passwordConfirmation) {
        return password.equals(passwordConfirmation);
    } 
    
    // ユーザーを有効にする
    @Transactional
    public void enableUser(User user) {
        user.setEnabled(true); 
        userRepository.save(user);
    } 
    
    // メールアドレスが変更されたかどうかをチェックする
    public boolean isEmailChanged(UserEditForm userEditForm) {
        User currentUser = userRepository.getReferenceById(userEditForm.getId());
        return !userEditForm.getEmail().equals(currentUser.getEmail());      
    }  
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    @Transactional
    public User createCheckoutSession(Map<String, String> paymentIntentObject) {
    	if (paymentIntentObject == null || !paymentIntentObject.containsKey("userName") || !paymentIntentObject.containsKey("signupDate")) {
            logger.error("Invalid payment intent object: {}", paymentIntentObject);
            throw new IllegalArgumentException("Invalid payment intent data.");
        }

        // Extract the username and other relevant data from the payment intent object
        String userName = paymentIntentObject.get("userName");
        String signupDate = paymentIntentObject.get("signupDate");

        // Retrieve the user by email (assuming userName is the email)
        User user = userRepository.findByEmail(userName);

        // Check if the user exists
        if (user != null) {
            user.setPaidLicense(true); // Update the user's membership status
            userRepository.save(user); // Save the updated user back to the repository
            logger.info("User membership status updated for: {}", userName);
            return user;
        } else {
            logger.warn("User not found for email: {}", userName);
            throw new UserNotFoundException("User not found for email: " + userName); // Or handle accordingly
        }
    }

	public boolean isEmailNotRegistered(String email) {
		return findByEmail(email) == null;
	}
	
	@Transactional
	public User updatePassword(User user, PasswordResetForm passwordResetForm) {
	    // Encode the new password before saving
	    String encodedPassword = passwordEncoder.encode(passwordResetForm.getPassword());
	    user.setPassword(encodedPassword);
	    
	    return userRepository.save(user);
	}
	
	public User findByEmail(String email) {
	    return userRepository.findByEmail(email);
	}

}