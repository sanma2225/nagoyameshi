package com.example.nagoyameshi.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.form.UserEditForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.StripeService;
import com.example.nagoyameshi.service.UserNotFoundException;
import com.example.nagoyameshi.service.UserService;
import com.stripe.model.checkout.Session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;

@Controller
@RequestMapping("/user")
public class UserController {
    private final UserRepository userRepository; 
    private final UserService userService;
    private final StripeService stripeService;
    
    public UserController(UserRepository userRepository, UserService userService, StripeService stripeService) {
        this.userRepository = userRepository;
        this.userService = userService; 
        this.stripeService = stripeService;
    }    
    
    @GetMapping
    public String index(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {         
        User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());  
        
        model.addAttribute("user", user);
        
        return "user/index";
    }
    
    @GetMapping("/edit")
    public String edit(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, Model model) {        
        User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());  
        UserEditForm userEditForm = new UserEditForm(user.getId(), 
        		user.getFullName(), 
        		user.getKana(), 
        		user.getEmail());
        
        model.addAttribute("userEditForm", userEditForm);
        
        return "user/edit";
    }
    
    @PostMapping("/update")
    public String update(@ModelAttribute @Validated UserEditForm userEditForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        // メールアドレスが変更されており、かつ登録済みであれば、BindingResultオブジェクトにエラー内容を追加する
        if (userService.isEmailChanged(userEditForm) && userService.isEmailRegistered(userEditForm.getEmail())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "すでに登録済みのメールアドレスです。");
            bindingResult.addError(fieldError);                       
        }
        
        if (bindingResult.hasErrors()) {
            return "user/edit";
        }
        
        userService.update(userEditForm);
        redirectAttributes.addFlashAttribute("successMessage", "会員情報を編集しました。");
        
        return "redirect:/user";
    } 
   
    
    @PostMapping("/create-checkout-session")
    public ResponseEntity<Map<String, String>> createCheckoutSession(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl, 
    		HttpServletRequest httpServletRequest,
    		Model model) {
        // Create Stripe session and handle the response
    	
    	System.out.println("Creating checkout session for user: " + userDetailsImpl.getUser().getEmail());
    	
    	User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String sessionId;
        try {
            sessionId = stripeService.createStripeSession(authentication.getName(), httpServletRequest);
            System.out.println("Successfully created sessionId: " + sessionId);
        } catch (Exception e) {
            System.err.println("Error creating Stripe session: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Failed to create checkout session"));
        }
        
        Map<String, String> response = new HashMap<>();
        response.put("sessionId", sessionId);
        // Redirect or respond as needed
        return ResponseEntity.ok(response); // Redirect to a success page or similar
    }
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
   
    @Transactional
    @GetMapping("/upgraded")
    public String upgraded(@RequestParam("session_id") String sessionId, Model model, RedirectAttributes redirectAttributes) {
        // You can fetch the session details from Stripe using the session ID
        Session session;
        try {
            session = Session.retrieve(sessionId);
            model.addAttribute("session", session); // Pass the session to the view
        } catch (Exception e) {
            // Handle error while retrieving the session
            model.addAttribute("error", "Unable to retrieve session details.");
            return "error"; // Return an error view
        }
        
        Map<String, String> paymentIntentObject = new HashMap<>();
        paymentIntentObject.put("customerEmail", session.getCustomerEmail());
        paymentIntentObject.put("subscriptionId", session.getSubscription());
        if (!paymentIntentObject.containsKey("customerEmail")) {
            logger.error("Invalid payment intent object: {}", paymentIntentObject);
            throw new IllegalArgumentException("Invalid payment intent data.");
        }

        // Extract the username and other relevant data from the payment intent object
        String userName = paymentIntentObject.get("customerEmail");
        String subscriptionId = paymentIntentObject.get("subscriptionId");

        // Retrieve the user by email (assuming userName is the email)
        User user = userRepository.findByEmail(userName);

        // Check if the user exists
        if (user != null) {
            // Assuming you have a field in User to track membership status
            user.setPaidLicense(true); 
            user.setSubscriptionId(subscriptionId); // Update the user's membership status
            userRepository.save(user); // Save the updated user back to the repository
            logger.info("User membership status updated for: {}", userName);
        } else {
            logger.warn("User not found for email: {}", userName);
            throw new UserNotFoundException("User not found for email: " + userName); // Or handle accordingly
        }
        
        UserDetailsImpl updatedUserDetails = new UserDetailsImpl(user, null); // Assuming this holds the new roles
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails, 
                null, 
                List.of(new SimpleGrantedAuthority("ROLE_PAID")));  // Add the new roles

        // Update the security context with the new authentication
        SecurityContextHolder.getContext().setAuthentication(newAuth);
        redirectAttributes.addFlashAttribute("upgradeSuccessMessage", "有料プランへの登録が完了しました！");

        return "redirect:/user"; // Return the success view where you can display session details
    }
    
    @Transactional
    @PostMapping("/downgrade")
    public String cancelSubscription(
//            @RequestParam("subscription_id") String subscriptionId,
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl, 
            Model model,
            RedirectAttributes redirectAttributes) {
        
        // Retrieve the user
    	 String subscriptionId = userDetailsImpl.getUser().getSubscriptionId();
    	 
        // Cancel the subscription
        try {
            stripeService.cancelSubscription(subscriptionId); // Call your Stripe service to cancel the subscription
        } catch (Exception e) {
            // Log the error for debugging purposes
            System.err.println("Error canceling subscription: " + e.getMessage());
            
            return "redirect:/user"; // Redirect with error
        }

        User user = userRepository.getReferenceById(userDetailsImpl.getUser().getId());
        user.setPaidLicense(false); // Set the paidLicense field to false (assuming this exists)
        user.setSubscriptionId(null); // Remove the subscription ID after cancellation
        userRepository.save(user);
        
        UserDetailsImpl updatedUserDetails = new UserDetailsImpl(user, null); 
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedUserDetails, 
                null, 
                List.of(new SimpleGrantedAuthority("ROLE_GENERAL"))); 
        
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        // Flash message for successful downgrade
        redirectAttributes.addFlashAttribute("downgradeSuccessMessage", "解約の手続きが完了しました。");
        
        // Redirect to user page
        return "redirect:/user";
    }
    
    
    @GetMapping("/upgrade")
    public String showUpgradePage() {
        return "user/upgrade"; 
    }
}
