package com.example.clichepiggybankdb.controller;

import com.example.clichepiggybankdb.controller.exceptions.*;
import com.example.clichepiggybankdb.model.Sanction;
import com.example.clichepiggybankdb.repository.*;
import com.example.clichepiggybankdb.service.SanctionService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/sanctions")
@CrossOrigin(origins = "${app.cors.allowed-origins}")
public class SanctionController {
    private final SanctionService sanctionService;

    public SanctionController(
            SanctionService sanctionService
    ) {
        this.sanctionService = sanctionService;
    }

    @PostMapping
    public ResponseEntity<Sanction> createSanction(@RequestBody Sanction newSanction, @RequestParam("inquirerid") UUID inquirerId) {
        return ResponseEntity.ok(sanctionService.create(newSanction, inquirerId));
    }

    @GetMapping
    public ResponseEntity<Page<Sanction>> paginateSanctions(
            @RequestParam(name = "sort", defaultValue = "datetime") String sort,
            @RequestParam(name = "order", defaultValue = "desc") String order,
            @RequestParam(name = "size", defaultValue = "10") Integer size,
            @RequestParam(name = "page", defaultValue = "0") Integer page,
            @RequestParam("inquirerid") UUID inquirerId) {
        return ResponseEntity.ok(sanctionService.getPage(sort, order, size, page, inquirerId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sanction> getSanction(
            @PathVariable UUID id,  @RequestParam("inquirerid") UUID inquirerId) {
        return ResponseEntity.ok(sanctionService.get(id, inquirerId));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Sanction> likeSanction(@PathVariable UUID id, @RequestParam("inquirerid") UUID inquirerId) {
        return ResponseEntity.ok(sanctionService.like(id, inquirerId));
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Sanction> unlikeSanction(@PathVariable UUID id, @RequestParam("inquirerid") UUID inquirerId) {
        return ResponseEntity.ok(sanctionService.unlike(id, inquirerId));
    }
}
