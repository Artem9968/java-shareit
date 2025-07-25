package ru.practicum.shareit.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@FieldDefaults(level = AccessLevel.PRIVATE)
@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    RequestService requestService;

    static final String USER_ID_HEADER = "X-Sharer-User-Id";
    Long userId = 1L;
    Long requestId = 1L;

    ItemRequestDto requestDto;
    ItemRequest itemRequest;

    @BeforeEach
    void setup() {
        requestDto = ItemRequestDto.builder()
                .id(requestId)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();

        itemRequest = ItemRequest.builder()
                .id(requestId)
                .description("Need a drill")
                .created(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllItemRequestsByUser_shouldReturnList() throws Exception {
        when(requestService.getAllByUserId(userId)).thenReturn(List.of(itemRequest));

        String result = mockMvc.perform(get("/requests")
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ItemRequest[] requests = objectMapper.readValue(result, ItemRequest[].class);
        assertEquals(1, requests.length);
        assertEquals(itemRequest.getDescription(), requests[0].getDescription());

        verify(requestService).getAllByUserId(userId);
    }

    @Test
    void getById_whenExists_thenReturnsItemRequestDto() throws Exception {
        when(requestService.getById(requestId, userId)).thenReturn(requestDto);

        String result = mockMvc.perform(get("/requests/{requestId}", requestId)
                        .header(USER_ID_HEADER, userId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ItemRequestDto actual = objectMapper.readValue(result, ItemRequestDto.class);
        assertEquals(requestDto.getId(), actual.getId());
        assertEquals(requestDto.getDescription(), actual.getDescription());

        verify(requestService).getById(requestId, userId);
    }
}

