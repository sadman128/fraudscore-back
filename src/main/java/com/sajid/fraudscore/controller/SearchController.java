package com.sajid.fraudscore.controller;

import com.sajid.fraudscore.dto.PostDto;
import com.sajid.fraudscore.dto.PostSearchResponse;
import com.sajid.fraudscore.service.PostService;
import com.sajid.fraudscore.service.SearchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public PostSearchResponse search(@RequestParam("query") String query) {
        return searchService.searchPosts(query);
    }
}



