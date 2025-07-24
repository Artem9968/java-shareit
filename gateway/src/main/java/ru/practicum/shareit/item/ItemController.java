package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

@RestController
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Validated
@Slf4j
public class ItemController {

    private final ItemClient client;

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getItemById(@PathVariable("itemId") Long itemId,
                                              @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Get item {}, userId = {}", itemId, userId);
        return client.getItem(userId, itemId);
    }

    @GetMapping
    public ResponseEntity<Object> getItemsForUser(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                                  @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Get items with userId = {}, from = {}, size = {}", userId, from, size);
        return client.getItems(userId, from, size);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> updateItem(@PathVariable("itemId") Long itemId,
                                             @RequestBody ItemDto item,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        checkValidItemForUpdate(item);
        log.info("Update item {}, userId = {}", itemId, userId);
        return client.update(userId, item, itemId);
    }

    @PostMapping
    public ResponseEntity<Object> addItem(@Valid @RequestBody ItemDto item,
                                          @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Create item {}, userId = {}", item, userId);
        return client.add(userId, item);
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Object> deleteItem(@PathVariable Long itemId,
                                             @RequestHeader("X-Sharer-User-Id") Long userId) {
        log.info("Delete item {}, userId = {}", itemId, userId);
        return client.delete(userId, itemId);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestParam("text") String text,
                                         @RequestHeader("X-Sharer-User-Id") Long userId,
                                         @PositiveOrZero @RequestParam(defaultValue = "0") int from,
                                         @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Search items with text = {}, userId = {}, from = {}, size = {}", text, userId, from, size);
        return client.search(userId, from, size, text);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@Valid @RequestBody CommentDto comment,
                                             @RequestHeader("X-Sharer-User-Id") Long userId,
                                             @PathVariable("itemId") Long itemId) {
        log.info("Create comment {}, userId = {}, for item {}", comment, userId, itemId);
        return client.addComment(userId, itemId, comment);
    }

    private void checkValidItemForUpdate(ItemDto item) {
        if (item.getName() != null) {
            if (item.getName().isBlank()) {
                // throw new FieldIsNotValidException("Name");
            }
        }
    }
}
