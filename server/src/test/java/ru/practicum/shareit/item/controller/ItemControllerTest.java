package ru.practicum.shareit.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingInfoDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FieldDefaults(level = AccessLevel.PRIVATE)
@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    MockMvc mockMvc;

    @MockBean
    ItemService itemService;

    Long userId = 1L;
    Long itemId = 1L;

    ItemDto itemDto;
    ItemWithBookingInfoDto itemWithBookingInfoDto;
    CommentDto commentDto;
    Comment comment;

    @BeforeEach
    void setup() {
        itemDto = ItemDto.builder()
                .id(itemId)
                .name("item name")
                .description("description")
                .available(true)
                .requestId(null)
                .lastBooking(null)
                .nextBooking(null)
                .comments(List.of())
                .build();

        itemWithBookingInfoDto = new ItemWithBookingInfoDto(
                itemId,
                "item name",
                "description",
                true,
                null,
                null,
                null,
                List.of()
        );

        commentDto = new CommentDto();
        commentDto.setText("Nice item!");
        commentDto.setId(10L);
        commentDto.setAuthorName("username");
        commentDto.setCreated(LocalDateTime.now());

        comment = new Comment();
        comment.setId(10L);
        comment.setText("Nice item!");
        comment.setCreated(LocalDateTime.now());
    }

    @Test
    void createItem_whenValid_thenReturnsItemDto() throws Exception {
        when(itemService.create(any(ItemDto.class), eq(userId))).thenReturn(itemDto);

        String result = mockMvc.perform(post("/items")
                        .header("X-Sharer-User-Id", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemDto)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ItemDto actual = objectMapper.readValue(result, ItemDto.class);
        assertEquals(itemDto.getId(), actual.getId());

        verify(itemService).create(any(ItemDto.class), eq(userId));
    }

    @Test
    void getById_whenExists_thenReturnsItemWithBookingInfoDto() throws Exception {
        when(itemService.getById(itemId)).thenReturn(itemWithBookingInfoDto);

        String result = mockMvc.perform(get("/items/{itemId}", itemId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ItemWithBookingInfoDto actual = objectMapper.readValue(result, ItemWithBookingInfoDto.class);
        assertEquals(itemWithBookingInfoDto.getId(), actual.getId());
        verify(itemService).getById(itemId);
    }

    @Test
    void getAllByUser_whenCalled_thenReturnsListOfItemDto() throws Exception {
        when(itemService.findAllByUser(userId)).thenReturn(List.of(itemDto));

        String result = mockMvc.perform(get("/items")
                        .header("X-Sharer-User-Id", userId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ItemDto> items = objectMapper.readValue(result,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ItemDto.class));

        assertEquals(1, items.size());
        assertEquals(itemDto.getId(), items.get(0).getId());
        verify(itemService).findAllByUser(userId);
    }

    @Test
    void searchItems_whenCalled_thenReturnsList() throws Exception {
        when(itemService.searchItems(eq("item"), eq(0), eq(10))).thenReturn(List.of(itemDto));

        String result = mockMvc.perform(get("/items/search")
                        .param("text", "item")
                        .param("from", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ItemDto> items = objectMapper.readValue(result,
                objectMapper.getTypeFactory().constructCollectionType(List.class, ItemDto.class));

        assertEquals(1, items.size());
        verify(itemService).searchItems("item", 0, 10);
    }
}

