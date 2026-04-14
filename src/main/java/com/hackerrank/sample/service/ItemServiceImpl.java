package com.hackerrank.sample.service;

import com.hackerrank.sample.dto.CompareItemsResponse;
import com.hackerrank.sample.dto.PagedItemsResponse;
import com.hackerrank.sample.model.Item;
import com.hackerrank.sample.repository.ItemRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service("itemService")
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemRepository itemRepository;

    @Override
    public PagedItemsResponse getPagedItems(int page, int size) {
        Page<Item> itemPage = itemRepository.findAll(PageRequest.of(page, size, Sort.by("id")));
        return new PagedItemsResponse(
                itemPage.getContent(),
                page,
                size,
                itemPage.getTotalElements(),
                itemPage.getTotalPages()
        );
    }

    @Override
    public CompareItemsResponse compareItems(List<String> ids) {
        Map<String, Item> itemMap = itemRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        List<Item> foundItems = new ArrayList<>();
        List<String> missingIds = new ArrayList<>();

        for (String id : ids) {
            Item item = itemMap.get(id);
            if (item != null) {
                foundItems.add(item);
            } else {
                missingIds.add(id);
            }
        }

        return new CompareItemsResponse(foundItems, missingIds);
    }
}
