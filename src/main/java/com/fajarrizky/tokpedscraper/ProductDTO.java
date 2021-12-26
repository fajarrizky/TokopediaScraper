package com.fajarrizky.tokpedscraper;

import lombok.Data;

@Data
public class ProductDTO {
    String name;
    String description;
    String imgUrl;
    String price;
    String rating;
    String merchant;
}
