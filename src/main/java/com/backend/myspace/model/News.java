package com.backend.myspace.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class News {
    private String title;
    private String description;
    private String url;
    private String imageUrl;
    private String publishedAt;
    private String source;
}
