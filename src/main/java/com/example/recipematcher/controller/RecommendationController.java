package com.example.recipematcher.controller;

import com.example.recipematcher.dto.RecommendationRequest;
import com.example.recipematcher.dto.RecommendationResponse;
import com.example.recipematcher.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {
    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @PostMapping
    public List<RecommendationResponse> recommendRecipes(@RequestBody RecommendationRequest request) {
        return recommendationService.recommendRecipes(request);
    }
}