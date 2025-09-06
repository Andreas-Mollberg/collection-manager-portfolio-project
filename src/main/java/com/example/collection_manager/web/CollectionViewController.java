package com.example.collection_manager.web;

import com.example.collection_manager.dtos.CollectionDetailDTO;
import com.example.collection_manager.dtos.CollectionSummaryDTO;
import com.example.collection_manager.dtos.CreateCollectionDTO;
import com.example.collection_manager.enums.Visibility;
import com.example.collection_manager.models.User;
import com.example.collection_manager.services.AuthorizationService;
import com.example.collection_manager.services.CollectionService;
import com.example.collection_manager.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Controller
public class CollectionViewController {

    private static final String VIEW_COLLECTIONS_TEMPLATE = "view-collections";
    private static final String ERROR_VIEW = "error";
    private static final String LOGIN_REDIRECT = "redirect:/login";
    private static final String HOME_REDIRECT = "redirect:/";
    private final CollectionService collectionService;
    private final UserService userService;
    private final AuthorizationService authorizationService;

    public CollectionViewController(CollectionService collectionService, 
                                  UserService userService, 
                                  AuthorizationService authorizationService) {
        this.collectionService = collectionService;
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/create-collection")
    public String showCreateCollectionForm(Model model) {
        model.addAttribute("collectionForm", new CreateCollectionDTO());
        return "create-collection";
    }

    @PostMapping("/create-collection")
    public String createCollection(@ModelAttribute("collectionForm") CreateCollectionDTO dto,
                                   Principal principal) {
        if (principal == null) return "redirect:/login";

        return userService.findByUsername(principal.getName())
                .flatMap(user -> collectionService.createCollectionForUser(user.getId(), dto))
                .map(c -> "redirect:/collections/" + c.getId())
                .orElse("redirect:/?error=collection-create");
    }

    @GetMapping("/collections/{collectionId}")
    public String showCollection(@PathVariable Long collectionId, Model model, Principal principal) {
        Optional<CollectionDetailDTO> collectionOpt = collectionService.getCollectionById(collectionId);
        if (collectionOpt.isEmpty()) {
            return ERROR_VIEW;
        }

        CollectionDetailDTO collection = collectionOpt.get();
        if (!isCollectionAccessible(collection, principal)) {
            return ERROR_VIEW;
        }

        boolean isOwner = isOwner(collection, principal);
        model.addAttribute("collection", collection);
        model.addAttribute("isOwner", isOwner);
        return "collection";
    }

    @GetMapping("/collections/{collectionId}/edit")
    public String showEditForm(@PathVariable Long collectionId, Model model, Principal principal) {
        if (!authorizationService.isOwnerOfCollection(collectionId, principal)) {
            return ERROR_VIEW;
        }

        return collectionService.getCollectionById(collectionId)
                .map(dto -> {
                    model.addAttribute("collection", dto);
                    return "edit-collection";
                })
                .orElse(ERROR_VIEW);
    }

    @PostMapping("/collections/{collectionId}/edit")
    public String updateCollection(@PathVariable Long collectionId,
                                   @RequestParam String collectionTitle,
                                   @RequestParam Visibility visibility,
                                   @RequestParam(required = false) String description,
                                   Principal principal) {
        if (!authorizationService.isOwnerOfCollection(collectionId, principal)) {
            return HOME_REDIRECT;
        }

        collectionService.updateCollection(collectionId, collectionTitle, visibility, description, principal);
        return "redirect:/collections/" + collectionId;
    }

    @PostMapping("/collections/{collectionId}/delete")
    public String deleteCollection(@PathVariable Long collectionId, Principal principal) {
        if (!authorizationService.isOwnerOfCollection(collectionId, principal)) {
            return HOME_REDIRECT;
        }

        collectionService.deleteCollection(collectionId, principal);
        return HOME_REDIRECT;
    }

    @GetMapping("/users/{userId}/public-collections")
    public String showPublicCollections(@PathVariable Long userId, Model model) {
        model.addAttribute("collections", collectionService.getPublicCollectionsByUserId(userId));
        return VIEW_COLLECTIONS_TEMPLATE;
    }

    @GetMapping("/public-collections")
    public String showAllPublicCollections(Model model) {
        model.addAttribute("collections", collectionService.getAllPublicCollections());
        return VIEW_COLLECTIONS_TEMPLATE;
    }

    @GetMapping("/discover")
    public String discoverCollections(
            @RequestParam(name = "scope", defaultValue = "public") String scope,
            Model model,
            Principal principal) {

        String normalizedScope = scope.toLowerCase();
        
        switch (normalizedScope) {
            case "friends" -> { return handleFriendsScope(model, principal); }
            case "all" -> { return handleAllScope(model, principal); }
            case "recent" -> { return "redirect:/collections/recent"; }
            case "public" -> { return handlePublicScope(model, normalizedScope); }
            default -> { return handlePublicScope(model, "public"); }
        }
    }

    @GetMapping("/friend-collections")
    public String redirectFriendsCollections() {
        return "redirect:/discover?scope=friends";
    }

    @GetMapping("/collections/recent")
    public String recentVisibleCollections(Model model, Principal principal) {
        if (!authorizationService.isAuthenticated(principal)) {
            return LOGIN_REDIRECT;
        }

        Optional<Long> userIdOpt = getUserId(principal);
        if (userIdOpt.isEmpty()) {
            return LOGIN_REDIRECT;
        }

        List<CollectionSummaryDTO> collections = collectionService.findRecentCollectionsVisibleToUser(userIdOpt.get(), 20);
        addCollectionsToModel(model, collections, "Recent Collections", "recent");
        return VIEW_COLLECTIONS_TEMPLATE;
    }

    private boolean isCollectionAccessible(CollectionDetailDTO collection, Principal principal) {
        boolean isOwner = isOwner(collection, principal);
        boolean isPublic = collection.visibility() == Visibility.PUBLIC;
        boolean isFriendVisible = collection.visibility() == Visibility.FRIENDS
                && authorizationService.isFriendsWith(collection.ownerId(), principal);

        return isOwner || isPublic || isFriendVisible;
    }

    private boolean isOwner(CollectionDetailDTO collection, Principal principal) {
        String username = principal != null ? principal.getName() : null;
        return username != null && username.equals(collection.ownerName());
    }

    private Optional<Long> getUserId(Principal principal) {
        if (principal == null) {
            return Optional.empty();
        }
        return userService.findByUsername(principal.getName()).map(User::getId);
    }

    private String handleFriendsScope(Model model, Principal principal) {
        if (!authorizationService.isAuthenticated(principal)) {
            return LOGIN_REDIRECT;
        }

        Optional<Long> userIdOpt = getUserId(principal);
        List<CollectionSummaryDTO> collections = userIdOpt
                .map(collectionService::getFriendsCollectionsForUser)
                .orElse(List.of());

        addCollectionsToModel(model, collections, "Friends' Collections", "friends");
        return VIEW_COLLECTIONS_TEMPLATE;
    }

    private String handleAllScope(Model model, Principal principal) {
        List<CollectionSummaryDTO> publicCollections = collectionService.getAllPublicCollections();
        
        if (!authorizationService.isAuthenticated(principal)) {
            addCollectionsToModel(model, publicCollections, "All Collections", "all");
            return VIEW_COLLECTIONS_TEMPLATE;
        }

        Optional<Long> userIdOpt = getUserId(principal);
        List<CollectionSummaryDTO> friendsCollections = userIdOpt
                .map(collectionService::getFriendsCollectionsForUser)
                .orElse(List.of());

        List<CollectionSummaryDTO> allCollections = mergeCollections(publicCollections, friendsCollections);
        addCollectionsToModel(model, allCollections, "All Collections", "all");
        return VIEW_COLLECTIONS_TEMPLATE;
    }

    private String handlePublicScope(Model model, String scope) {
        List<CollectionSummaryDTO> collections = collectionService.getAllPublicCollections();
        addCollectionsToModel(model, collections, "Public Collections", scope);
        return VIEW_COLLECTIONS_TEMPLATE;
    }

    private List<CollectionSummaryDTO> mergeCollections(List<CollectionSummaryDTO> publicCollections,
                                                       List<CollectionSummaryDTO> friendsCollections) {
        LinkedHashMap<Long, CollectionSummaryDTO> collectionsById = new LinkedHashMap<>();
        
        Stream.concat(publicCollections.stream(), friendsCollections.stream())
                .forEach(collection -> collectionsById.put(collection.id(), collection));
        
        return new ArrayList<>(collectionsById.values());
    }

    private void addCollectionsToModel(Model model, List<CollectionSummaryDTO> collections, 
                                     String pageTitle, String scope) {
        model.addAttribute("collections", collections);
        model.addAttribute("pageTitle", pageTitle);
        model.addAttribute("scope", scope);
    }
}
