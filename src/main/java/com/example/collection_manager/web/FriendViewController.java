package com.example.collection_manager.web;

import com.example.collection_manager.dtos.FriendContactDTO;
import com.example.collection_manager.dtos.FriendDTOMapper;
import com.example.collection_manager.models.User;
import com.example.collection_manager.services.AuthorizationService;
import com.example.collection_manager.services.FriendService;
import com.example.collection_manager.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/friends")
public class FriendViewController {

    private static final String TAB_FRIENDS = "friends";
    private static final String TAB_REQUESTS = "requests";
    private static final String LOGIN_REDIRECT = "redirect:/login";
    private static final String FRIENDS_TEMPLATE = "friends";
    private final UserService userService;
    private final FriendService friendService;
    private final FriendDTOMapper friendDTOMapper;
    private final AuthorizationService authorizationService;

    public FriendViewController(UserService userService,
                               FriendService friendService,
                               FriendDTOMapper friendDTOMapper,
                               AuthorizationService authorizationService) {
        this.userService = userService;
        this.friendService = friendService;
        this.friendDTOMapper = friendDTOMapper;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    public String friendsHome(Model model,
                             Principal principal,
                             @RequestParam(name = "tab", defaultValue = TAB_FRIENDS) String tab) {

        if (!authorizationService.isAuthenticated(principal)) {
            return LOGIN_REDIRECT;
        }

        Long userId = getCurrentUserId(principal);
        populateFriendsData(model, userId, tab);
        return FRIENDS_TEMPLATE;
    }

    @PostMapping("/send-by-username")
    public String sendRequestByUsername(@RequestParam String username, Principal principal) {
        if (!isValidRequest(principal)) {
            return LOGIN_REDIRECT;
        }

        Long currentUserId = getCurrentUserId(principal);
        Optional<String> cleanedUsername = getValidatedUsername(username, principal.getName());
        
        if (cleanedUsername.isEmpty()) {
            return redirectToTab(TAB_REQUESTS);
        }

        processUserAction(cleanedUsername.get(), targetId -> 
            friendService.sendRequest(currentUserId, targetId));
        
        return redirectToTab(TAB_REQUESTS);
    }

    @PostMapping("/accept-by-username")
    public String acceptByUsername(@RequestParam String username, Principal principal) {
        if (!isValidRequest(principal)) {
            return LOGIN_REDIRECT;
        }

        Long currentUserId = getCurrentUserId(principal);
        Optional<String> cleanedUsername = getValidatedUsername(username, principal.getName());
        
        if (cleanedUsername.isEmpty()) {
            return redirectToTab(TAB_REQUESTS);
        }

        processUserAction(cleanedUsername.get(), requesterId -> 
            friendService.acceptRequest(requesterId, currentUserId));
        
        return redirectToTab(TAB_REQUESTS);
    }

    @PostMapping("/decline-by-username")
    public String declineOrCancelByUsername(@RequestParam String username, Principal principal) {
        if (!isValidRequest(principal)) {
            return LOGIN_REDIRECT;
        }

        Long currentUserId = getCurrentUserId(principal);
        Optional<String> cleanedUsername = getValidatedUsername(username, principal.getName());
        
        if (cleanedUsername.isEmpty()) {
            return redirectToTab(TAB_REQUESTS);
        }

        processUserAction(cleanedUsername.get(), otherId -> 
            friendService.declineOrCancel(otherId, currentUserId));
        
        return redirectToTab(TAB_REQUESTS);
    }

    @PostMapping("/remove-by-username")
    public String removeByUsername(@RequestParam String username, Principal principal) {
        if (!isValidRequest(principal)) {
            return LOGIN_REDIRECT;
        }

        Long currentUserId = getCurrentUserId(principal);
        Optional<String> cleanedUsername = getValidatedUsername(username, principal.getName());
        
        if (cleanedUsername.isEmpty()) {
            return redirectToTab(TAB_FRIENDS);
        }

        processUserAction(cleanedUsername.get(), otherId -> 
            friendService.removeFriend(currentUserId, otherId));
        
        return redirectToTab(TAB_FRIENDS);
    }

    private Long getCurrentUserId(Principal principal) {
        return userService.findByUsername(principal.getName())
                .map(User::getId)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private String redirectToTab(String tab) {
        return "redirect:/friends?tab=" + tab;
    }

    private Optional<String> cleanUsername(String username) {
        if (username == null) {
            return Optional.empty();
        }
        
        String trimmed = username.trim();
        return trimmed.isEmpty() ? Optional.empty() : Optional.of(trimmed);
    }

    private boolean isValidRequest(Principal principal) {
        return authorizationService.isAuthenticated(principal);
    }

    private Optional<String> getValidatedUsername(String username, String currentUsername) {
        Optional<String> cleanedUsername = cleanUsername(username);
        
        if (cleanedUsername.isEmpty()) {
            return Optional.empty();
        }

        if (cleanedUsername.get().equalsIgnoreCase(currentUsername)) {
            return Optional.empty();
        }
        
        return cleanedUsername;
    }

    private void processUserAction(String username, UserAction action) {
        userService.findByUsername(username)
                .map(User::getId)
                .ifPresent(action::execute);
    }

    @FunctionalInterface
    private interface UserAction {
        void execute(Long userId);
    }

    private void populateFriendsData(Model model, Long userId, String activeTab) {
        List<FriendContactDTO> friends = buildFriendsList(userId);
        List<FriendContactDTO> incomingRequests = buildIncomingRequestsList(userId);
        List<FriendContactDTO> outgoingRequests = buildOutgoingRequestsList(userId);

        model.addAttribute("friends", friends);
        model.addAttribute("incoming", incomingRequests);
        model.addAttribute("outgoing", outgoingRequests);
        model.addAttribute("activeTab", activeTab);
    }

    private List<FriendContactDTO> buildFriendsList(Long userId) {
        return friendService.friendsOf(userId).stream()
                .map(friendDTOMapper::toDTO)
                .map(dto -> createFriendContact(dto, userId))
                .toList();
    }

    private List<FriendContactDTO> buildIncomingRequestsList(Long userId) {
        return friendService.incomingRequests(userId).stream()
                .map(friendDTOMapper::toDTO)
                .map(dto -> new FriendContactDTO(dto.id(), dto.requesterId(), 
                    dto.requesterName(), dto.status()))
                .toList();
    }

    private List<FriendContactDTO> buildOutgoingRequestsList(Long userId) {
        return friendService.outgoingRequests(userId).stream()
                .map(friendDTOMapper::toDTO)
                .map(dto -> new FriendContactDTO(dto.id(), dto.recipientId(), 
                    dto.recipientName(), dto.status()))
                .toList();
    }

    private FriendContactDTO createFriendContact(com.example.collection_manager.dtos.FriendDTO dto, Long userId) {
        boolean iAmRequester = userId.equals(dto.requesterId());
        Long otherId = iAmRequester ? dto.recipientId() : dto.requesterId();
        String otherName = iAmRequester ? dto.recipientName() : dto.requesterName();
        
        return new FriendContactDTO(dto.id(), otherId, otherName, dto.status());
    }
}
