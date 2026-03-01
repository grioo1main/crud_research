package com.lum1nar.tcp_project.service;

import com.lum1nar.tcp_project.dto.ItemRequest;
import com.lum1nar.tcp_project.model.Item;
import com.lum1nar.tcp_project.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Item findById(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found: " + id));
    }

    @Transactional
    public Item create(ItemRequest request) {
        Item item = Item.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .build();
        return itemRepository.save(item);
    }

    @Transactional
    public Item update(Long id, ItemRequest request) {
        Item item = findById(id);
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setPrice(request.getPrice());
        return itemRepository.save(item);
    }

    @Transactional
    public void delete(Long id) {
        itemRepository.deleteById(id);
    }

    public long count() {
        return itemRepository.count();
    }
}
