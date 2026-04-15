package com.hackerrank.sample.controller;

import com.hackerrank.sample.dto.CompareItemsResponse;
import com.hackerrank.sample.dto.PagedItemsResponse;
import com.hackerrank.sample.exception.BadResourceRequestException;
import com.hackerrank.sample.service.ItemService;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/items")
public class ItemController {

    private static final int SIZE_MAX = 50;

    @Autowired
    private ItemService itemService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PagedItemsResponse getItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > SIZE_MAX) {
            throw new BadResourceRequestException(
                    "Invalid pagination parameters: page must be >= 0 and size must be between 1 and " + SIZE_MAX);
        }
        return itemService.getPagedItems(page, size);
    }

    @GetMapping("/compare")
    @ResponseStatus(HttpStatus.OK)
    public CompareItemsResponse compareItems(@RequestParam String ids) {
        List<String> idList = Arrays.stream(ids.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        if (idList.size() < 2) {
            throw new BadResourceRequestException(
                    "At least 2 distinct item ids are required for comparison");
        }

        return itemService.compareItems(idList);
    }
}
