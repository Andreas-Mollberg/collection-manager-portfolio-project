
package com.example.collection_manager.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;

@Controller
public class AuthController {

    private static final String LOGIN_TEMPLATE = "login";
    private static final String REGISTER_TEMPLATE = "register";
    private static final String HOME_REDIRECT = "redirect:/";

    @GetMapping("/login")
    public String loginPage(Principal principal) {
        if (principal != null) {
            return HOME_REDIRECT;
        }
        return LOGIN_TEMPLATE;
    }

    @GetMapping("/register")
    public String showRegisterForm(Principal principal) {
        if (principal != null) {
            return HOME_REDIRECT;
        }
        return REGISTER_TEMPLATE;
    }
}