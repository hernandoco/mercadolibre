package com.hackerrank.sample.dto;

import com.hackerrank.sample.model.Item;
import java.util.List;

public class CompareItemsResponse {
    private List<Item> items;
    private List<String> missingIds;

    public CompareItemsResponse() {
    }

    public CompareItemsResponse(List<Item> items, List<String> missingIds) {
        this.items = items;
        this.missingIds = missingIds;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public List<String> getMissingIds() {
        return missingIds;
    }

    public void setMissingIds(List<String> missingIds) {
        this.missingIds = missingIds;
    }
}
