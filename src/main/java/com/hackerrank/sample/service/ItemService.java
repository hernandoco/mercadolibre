package com.hackerrank.sample.service;

import com.hackerrank.sample.dto.CompareItemsResponse;
import com.hackerrank.sample.dto.PagedItemsResponse;
import java.util.List;

public interface ItemService {
    PagedItemsResponse getPagedItems(int page, int size);
    CompareItemsResponse compareItems(List<String> ids);
}
