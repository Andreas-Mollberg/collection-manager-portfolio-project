package com.example.collection_manager.web;

import com.example.collection_manager.dtos.CollectionSummaryDTO;
import com.example.collection_manager.models.User;
import com.example.collection_manager.services.CollectionService;
import com.example.collection_manager.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class UserViewController {

    private static final String MY_COLLECTIONS_TEMPLATE = "my-collections";
    private static final String LOGIN_REDIRECT = "redirect:/login";
    private final UserService userService;
    private final CollectionService collectionService;

    public UserViewController(UserService userService,
                              CollectionService collectionService) {
        this.userService = userService;
        this.collectionService = collectionService;
    }

    @GetMapping("/users/me")
    public String viewMyCollections(Model model, Principal principal) {
        if (principal == null) {
            return LOGIN_REDIRECT;
        }

        Optional<User> userOpt = userService.findByUsername(principal.getName());
        if (userOpt.isEmpty()) {
            return LOGIN_REDIRECT;
        }

        User user = userOpt.get();
        List<CollectionSummaryDTO> collections = collectionService.getCollectionsByUserId(user.getId());

        model.addAttribute("collections", collections);
        model.addAttribute("userName", user.getUserName());
        model.addAttribute("userId", user.getId());

        return MY_COLLECTIONS_TEMPLATE;
    }
}