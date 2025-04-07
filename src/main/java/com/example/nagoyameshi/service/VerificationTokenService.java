package com.example.nagoyameshi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.repository.VerificationTokenRepository;

@Service
public class VerificationTokenService {
	@Autowired
    private final VerificationTokenRepository verificationTokenRepository;
    
    
    public VerificationTokenService(VerificationTokenRepository verificationTokenRepository) {        
        this.verificationTokenRepository = verificationTokenRepository;
    } 
    
    @Transactional
    public void create(User user, String token) {
        // Check if a token already exists for this user
        VerificationToken existingToken = verificationTokenRepository.findByUser(user);
        
        if (existingToken != null) {
            // If a token exists, update it
            existingToken.setToken(token);
            verificationTokenRepository.save(existingToken);
        } else {
            // If no token exists, create a new one
            VerificationToken verificationToken = new VerificationToken();
            verificationToken.setUser(user);
            verificationToken.setToken(token);
            
            verificationTokenRepository.save(verificationToken);
        }    
    }
    
    // トークンの文字列で検索した結果を返す
    public VerificationToken getVerificationToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

}
