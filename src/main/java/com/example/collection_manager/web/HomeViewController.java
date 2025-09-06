package com.example.collection_manager.web;

import com.example.collection_manager.models.User;
import com.example.collection_manager.services.AuthorizationService;
import com.example.collection_manager.services.CollectionService;
import com.example.collection_manager.services.FriendService;
import com.example.collection_manager.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;
import java.util.Optional;

@Controller
public class HomeViewController {

    private static final String LOGIN_REDIRECT = "redirect:/login";
    private static final String HOME_TEMPLATE = "home";
    private final AuthorizationService authorizationService;
    private final UserService userService;
    private final CollectionService collectionService;
    private final FriendService friendService;

    public HomeViewController(AuthorizationService authorizationService,
                             UserService userService,
                             CollectionService collectionService,
                             FriendService friendService) {
        this.authorizationService = authorizationService;
        this.userService = userService;
        this.collectionService = collectionService;
        this.friendService = friendService;
    }

    @GetMapping("/")
    public String showHome(Model model, Principal principal) {
        if (!authorizationService.isAuthenticated(principal)) {
            return LOGIN_REDIRECT;
        }

        Optional<User> userOpt = userService.findByUsername(principal.getName());
        if (userOpt.isEmpty()) {
            return LOGIN_REDIRECT;
        }

        User user = userOpt.get();
        populateHomeModel(model, user);
        
        return HOME_TEMPLATE;
    }

    private void populateHomeModel(Model model, User user) {
        Long userId = user.getId();
        String username = user.getUserName();

        HomeStats stats = calculateHomeStats(userId);

        model.addAttribute("userId", userId);
        model.addAttribute("userName", username);
        model.addAttribute("collectionCount", stats.collectionCount());
        model.addAttribute("recentCount", stats.recentCount());
        model.addAttribute("friendCount", stats.friendCount());
    }

    private HomeStats calculateHomeStats(Long userId) {
        int collectionCount = collectionService.getCollectionsByUserId(userId).size();
        int recentCount = collectionService.countRecentCollectionsVisibleToUser(userId);
        int friendCount = friendService.friendsOf(userId).size();

        return new HomeStats(collectionCount, recentCount, friendCount);
    }

    private record HomeStats(int collectionCount, int recentCount, int friendCount) {}
}
