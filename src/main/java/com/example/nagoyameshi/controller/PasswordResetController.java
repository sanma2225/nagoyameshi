package com.example.nagoyameshi.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.nagoyameshi.entity.User;
import com.example.nagoyameshi.entity.VerificationToken;
import com.example.nagoyameshi.event.PasswordEventPublisher;
import com.example.nagoyameshi.form.PasswordResetForm;
import com.example.nagoyameshi.form.PasswordResetVerifyForm;
import com.example.nagoyameshi.repository.UserRepository;
import com.example.nagoyameshi.security.UserDetailsImpl;
import com.example.nagoyameshi.service.UserService;
import com.example.nagoyameshi.service.VerificationTokenService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
public class PasswordResetController {
	private final UserService userService;
    private final PasswordEventPublisher passwordEventPublisher;
    private final VerificationTokenService verificationTokenService;

    public PasswordResetController(UserService userService, 
                                    PasswordEventPublisher passwordEventPublisher,
                                    VerificationTokenService verificationTokenService) {
        this.userService = userService;
        this.passwordEventPublisher = passwordEventPublisher;
        this.verificationTokenService = verificationTokenService;
    }

    @GetMapping("/pwreset")
    public String pwresetForm(Model model) {
        model.addAttribute("passwordResetVerifyForm", new PasswordResetVerifyForm());
        return "auth/pwreset";
    }

    @PostMapping("/pwreset")
    public String submitPwreset(
            @Valid PasswordResetVerifyForm passwordResetVerifyForm, 
            UserRepository userRepository,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request,
            Model model) {
    
        
        User user = userService.findByEmail(passwordResetVerifyForm.getEmail());
        if (userService.isEmailNotRegistered(passwordResetVerifyForm.getEmail())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "email", "このメールアドレスはまだ登録されていません。");
            bindingResult.addError(fieldError);  
            return "auth/pwreset";
        }  

        // Publish the password reset event
        String requestUrl = request.getRequestURL().toString();
        passwordEventPublisher.publishPasswordEvent(user, requestUrl);

        redirectAttributes.addFlashAttribute("successMessage", "認証メールを送信しました。メールのリンクを確認してください。");
        return "redirect:/"; 
    }
    
    @GetMapping("/newpw")
    public String newpw(@RequestParam(name = "token") String token, Model model) {        
        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
        
        if (verificationToken == null) {
            // Handle invalid token if necessary, e.g., redirecting or showing an error message
            return "redirect:/pwreset"; // Or some error page
        }
        
        // Create a new PasswordResetForm and set the email
        PasswordResetForm passwordResetForm = new PasswordResetForm();
        passwordResetForm.setEmail(verificationToken.getUser().getEmail());
        
        model.addAttribute("passwordResetForm", passwordResetForm);
        model.addAttribute("token", token); // Add token to model if needed for form submission
        return "auth/newpw";  // Ensure this corresponds to the correct HTML file
    }   
    
    @PostMapping("/newpw")
    public String newpw(@RequestParam(name = "token") String token,
            @ModelAttribute @Validated PasswordResetForm passwordResetForm, 
            BindingResult bindingResult, 
            RedirectAttributes redirectAttributes) { 

        // Retrieve the verification token
        VerificationToken verificationToken = verificationTokenService.getVerificationToken(token);
            
        // Check if the token is valid
        if (verificationToken == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "無効なトークンです。再試行してください。");
            return "redirect:/pwreset";
        }

        // Retrieve the associated user
        User user = verificationToken.getUser();
        
        // Set email on the password reset form
        passwordResetForm.setEmail(user.getEmail());

        // Validate the password confirmation
        if (!userService.isSamePassword(passwordResetForm.getPassword(), passwordResetForm.getPasswordConfirmation())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
            bindingResult.addError(fieldError);
        }        
            
        // Check for binding errors
        if (bindingResult.hasErrors()) {
            return "auth/newpw";
        }
        
        // Update the user's password
        userService.updatePassword(user, passwordResetForm);

        redirectAttributes.addFlashAttribute("successMessage", "パスワードリセットが完了しました。新しいパスワードでログインしてください。"); 

        return "redirect:/login";
    }
    
    @GetMapping("/newpw-login")
    public String newpwLogin(@AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
    		Model model) {        
        // Create a new PasswordResetForm and set the email
        PasswordResetForm passwordResetForm = new PasswordResetForm();
        passwordResetForm.setEmail(userDetailsImpl.getUser().getEmail());
        
        model.addAttribute("passwordResetForm", passwordResetForm);
        return "user/newpw";  // Ensure this corresponds to the correct HTML file
    } 
    
    @PostMapping("/newpw-login")
    public String newpw(
            @ModelAttribute @Validated PasswordResetForm passwordResetForm, 
            @AuthenticationPrincipal UserDetailsImpl userDetailsImpl,
            BindingResult bindingResult, 
            RedirectAttributes redirectAttributes) { 
        
        // Set email on the password reset form
    	passwordResetForm.setEmail(userDetailsImpl.getUser().getEmail());

        // Validate the password confirmation
        if (!userService.isSamePassword(passwordResetForm.getPassword(), passwordResetForm.getPasswordConfirmation())) {
            FieldError fieldError = new FieldError(bindingResult.getObjectName(), "password", "パスワードが一致しません。");
            bindingResult.addError(fieldError);
        }        
            
        // Check for binding errors
        if (bindingResult.hasErrors()) {
            return "auth/newpw";
        }
        User user = userDetailsImpl.getUser();
        // Update the user's password
        userService.updatePassword(user, passwordResetForm);
        redirectAttributes.addFlashAttribute("successMessage", "パスワードリセットが完了しました。");

        return "redirect:/user";
    }
}