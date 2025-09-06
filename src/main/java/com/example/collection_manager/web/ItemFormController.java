package com.example.collection_manager.web;

import com.example.collection_manager.dtos.UpdateItemDTO;
import com.example.collection_manager.forms.CreateItemForm;
import com.example.collection_manager.models.Image;
import com.example.collection_manager.models.Item;
import com.example.collection_manager.models.Tag;
import com.example.collection_manager.models.User;
import com.example.collection_manager.services.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping({"/collections/{collectionId}/items", "/users/{userId}/collections/{collectionId}/items"})
public class ItemFormController {

    private static final String CREATE_ITEM_TEMPLATE = "create-item";
    private static final String EDIT_ITEM_TEMPLATE = "edit-item";
    private static final String ERROR_VIEW = "error";
    private static final String LOGIN_REDIRECT = "redirect:/login";
    private final ItemService itemService;
    private final TagService tagService;
    private final ImageService imageService;
    private final AuthorizationService authorizationService;
    private final UserService userService;

    public ItemFormController(ItemService itemService,
                              TagService tagService,
                              ImageService imageService,
                              AuthorizationService authorizationService,
                              UserService userService) {
        this.itemService = itemService;
        this.tagService = tagService;
        this.imageService = imageService;
        this.authorizationService = authorizationService;
        this.userService = userService;
    }

    @GetMapping("/create")
    public String showCreateForm(@PathVariable Long collectionId,
                                 Model model,
                                 Principal principal) {

        if (!isOwnerOfCollection(collectionId, principal)) {
            return getUnauthorizedRedirect(principal);
        }

        model.addAttribute("collectionId", collectionId);
        model.addAttribute("form", new CreateItemForm());
        return CREATE_ITEM_TEMPLATE;
    }

    @PostMapping("/create")
    public String createItem(@PathVariable Long collectionId,
                             @ModelAttribute CreateItemForm form,
                             Principal principal,
                             Model model) {

        if (!isOwnerOfCollection(collectionId, principal)) {
            return getUnauthorizedRedirect(principal);
        }

        try {
            Item item = createItemFromForm(form);
            Optional<Item> savedItem = itemService.addItemToCollection(
                    collectionId, item, item.getTags(), item.getImages(), principal);

            if (savedItem.isEmpty()) {
                return ERROR_VIEW;
            }

            return "redirect:/collections/" + collectionId;

        } catch (IOException e) {
            handleImageUploadError(e, model);
            model.addAttribute("collectionId", collectionId);
            model.addAttribute("form", form);
            return CREATE_ITEM_TEMPLATE;
        }
    }

    @GetMapping("/{itemId}/edit")
    public String showEditForm(@PathVariable Long collectionId,
                               @PathVariable Long itemId,
                               Model model,
                               Principal principal) {

        if (!isOwnerOfItem(itemId, principal)) {
            return getUnauthorizedRedirect(principal);
        }

        Optional<Item> itemOpt = itemService.findItemByIdInCollection(itemId, collectionId);
        if (itemOpt.isEmpty()) {
            return ERROR_VIEW;
        }

        Item item = itemOpt.get();
        populateEditModel(model, item, collectionId, itemId, principal);
        return EDIT_ITEM_TEMPLATE;
    }

    @PostMapping("/{itemId}/edit")
    public String updateItem(@PathVariable Long collectionId,
                             @PathVariable Long itemId,
                             @ModelAttribute("createItemForm") CreateItemForm form,
                             Principal principal) {

        if (!isOwnerOfItem(itemId, principal)) {
            return getUnauthorizedRedirect(principal);
        }

        Optional<Item> itemOpt = itemService.findItemByIdInCollection(itemId, collectionId);
        if (itemOpt.isEmpty()) {
            return ERROR_VIEW;
        }

        try {
            UpdateItemDTO updateDTO = createUpdateDTO(form, itemOpt.get());
            itemService.updateItem(itemId, updateDTO);
            return "redirect:/collections/" + collectionId;

        } catch (IOException e) {
            handleImageUploadError(e, null);
            return ERROR_VIEW;
        }
    }

    @PostMapping("/{itemId}/images/{imageId}/delete")
    public String deleteImage(@PathVariable Long collectionId,
                              @PathVariable Long itemId,
                              @PathVariable Long imageId,
                              Principal principal) {

        if (!isOwnerOfItem(itemId, principal)) {
            return getUnauthorizedRedirect(principal);
        }

        itemService.removeImageFromItem(itemId, imageId);
        return "redirect:/collections/" + collectionId + "/items/" + itemId + "/edit";
    }

    @PostMapping("/{itemId}/delete")
    public String deleteItem(@PathVariable Long collectionId,
                             @PathVariable Long itemId,
                             Principal principal) {

        if (!isOwnerOfItem(itemId, principal)) {
            return getUnauthorizedRedirect(principal);
        }

        boolean deleted = itemService.deleteItemInCollection(itemId, collectionId);
        return deleted ? "redirect:/collections/" + collectionId : ERROR_VIEW;
    }

    private boolean isOwnerOfCollection(Long collectionId, Principal principal) {
        return authorizationService.isOwnerOfCollection(collectionId, principal);
    }

    private boolean isOwnerOfItem(Long itemId, Principal principal) {
        return authorizationService.isOwnerOfItem(itemId, principal);
    }

    private String getUnauthorizedRedirect(Principal principal) {
        return principal == null ? LOGIN_REDIRECT : ERROR_VIEW;
    }

    private User getCurrentUser(Principal principal) {
        return userService.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private Set<String> extractTagNames(List<String> rawTagNames) {
        Set<String> result = new HashSet<>();
        if (rawTagNames != null) {
            rawTagNames.forEach(tagString ->
                    Arrays.stream(tagString.split(","))
                            .map(String::trim)
                            .filter(tag -> !tag.isEmpty())
                            .forEach(result::add)
            );
        }
        return result;
    }

    private List<Image> processImageUploads(List<MultipartFile> imageFiles) throws IOException {
        List<Image> uploadedImages = new ArrayList<>();

        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    uploadedImages.add(imageService.uploadAndSave(file));
                }
            }
        }

        return uploadedImages;
    }

    private Item createItemFromForm(CreateItemForm form) throws IOException {
        Set<String> tagNames = extractTagNames(form.getTagNames());
        List<Tag> tags = tagService.getOrCreateTags(tagNames);
        List<Image> images = processImageUploads(form.getImages());

        Item item = new Item();
        item.setItemName(form.getItemName());
        item.setDescription(form.getDescription());
        item.setTags(tags);
        item.setImages(images);

        return item;
    }

    private void populateEditModel(Model model, Item item, Long collectionId, Long itemId, Principal principal) {
        CreateItemForm form = new CreateItemForm();
        form.setItemName(item.getItemName());
        form.setDescription(item.getDescription());
        form.setTagNames(item.getTags().stream()
                .map(Tag::getTagName)
                .toList());

        model.addAttribute("createItemForm", form);
        model.addAttribute("userId", getCurrentUser(principal).getId());
        model.addAttribute("collectionId", collectionId);
        model.addAttribute("itemId", itemId);
        model.addAttribute("existingImages", item.getImages());
    }

    private UpdateItemDTO createUpdateDTO(CreateItemForm form, Item existingItem) throws IOException {
        Set<String> tagNames = extractTagNames(form.getTagNames());
        List<Long> tagIds = tagService.getOrCreateTags(tagNames)
                .stream()
                .map(Tag::getId)
                .toList();

        List<Long> imageIds = existingItem.getImages()
                .stream()
                .map(Image::getId)
                .collect(Collectors.toList());

        List<Image> newImages = processImageUploads(form.getImages());
        List<Long> newImageIds = newImages.stream()
                .map(Image::getId)
                .toList();
        imageIds.addAll(newImageIds);

        return new UpdateItemDTO(
                form.getItemName(),
                form.getDescription(),
                tagIds,
                imageIds
        );
    }

    private void handleImageUploadError(IOException e, Model model) {
        e.printStackTrace();
        if (model != null) {
            model.addAttribute("error", "Failed to upload image(s)");
        }
    }
}