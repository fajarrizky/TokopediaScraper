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

    public String toCsvString() {
        String[] result = new String[6];
        result[0] = name;
        result[1] = description;
        result[2] = imgUrl;
        result[3] = price;
        result[4] = rating;
        result[5] = merchant;
        return String.join("|", result);
    }
}
