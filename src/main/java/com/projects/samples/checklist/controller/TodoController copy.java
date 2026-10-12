package com.projects.samples.checklist.controller;

import com.projects.samples.checklist.model.ChecklistItem;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/checklist")
public class TodoController {

    private final List<ChecklistItem> items = new ArrayList<>();

    @GetMapping
    public List<ChecklistItem> getAll() {
        return items;
    }

    @PostMapping
    public ChecklistItem addItem(@RequestBody ChecklistItem item) {
        item.setId(items.size() + 1);
        items.add(item);
        return item;
    }

    @PutMapping("/{id}")
    public ChecklistItem updateItem(@PathVariable Long id, @RequestBody ChecklistItem item) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(id)) {
                item.setId(id);
                items.set(i, item);
                return item;
            }
        }
        return null;
    }

    @DeleteMapping("/{id}")
    public void deleteItem(@PathVariable Long id) {
        items.removeIf(item -> item.getId().equals(id));
    }
}