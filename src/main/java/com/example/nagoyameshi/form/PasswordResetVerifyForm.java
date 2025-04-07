package com.example.nagoyameshi.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordResetVerifyForm {        
    @NotBlank(message = "登録のメールアドレスを入力してください。")
    @Email(message = "登録のメールアドレスは正しい形式で入力してください。")
    private String email;
}