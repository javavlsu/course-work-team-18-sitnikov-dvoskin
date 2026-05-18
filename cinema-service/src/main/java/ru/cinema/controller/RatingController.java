package ru.cinema.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.cinema.dto.rating.RateRequest;
import ru.cinema.dto.rating.RatingResponse;
import ru.cinema.security.CurrentUser;
import ru.cinema.service.rating.RatingService;

@RestController
@RequestMapping("/api/v1/content/{contentId}/rating")
@Tag(name = "Ratings")
public class RatingController {

    private final RatingService ratingService;

    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @GetMapping("/me")
    public ResponseEntity<RatingResponse> my(@PathVariable Long contentId) {
        return ratingService.myRating(CurrentUser.requireUserId(), contentId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PutMapping
    public RatingResponse rate(@PathVariable Long contentId,
                               @Valid @RequestBody RateRequest req) {
        return ratingService.rate(CurrentUser.requireUserId(), contentId, req.value());
    }

    /** Алиас на PUT — заявлен в API map Этап 8 как POST. */
    @PostMapping
    public RatingResponse ratePost(@PathVariable Long contentId,
                                   @Valid @RequestBody RateRequest req) {
        return ratingService.rate(CurrentUser.requireUserId(), contentId, req.value());
    }

    @DeleteMapping
    public ResponseEntity<Void> remove(@PathVariable Long contentId) {
        ratingService.delete(CurrentUser.requireUserId(), contentId);
        return ResponseEntity.noContent().build();
    }
}
