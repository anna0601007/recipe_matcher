package com.example.recipematcher.dto;

import java.util.List;

public record RecommendationRequest(
        List<String> ingredients
) {
}