package com.pricehunt;

public class Product {
    private String title;
    private String price;
    private String rating;
    private String source;
    private String link;
    private String image;

    public Product(String title, String price, String rating, String source, String link, String image) {
        this.title = title;
        this.price = price;
        this.rating = rating;
        this.source = source;
        this.link = link;
        this.image = image;
    }

    public String getTitle() { return title; }
    public String getPrice() { return price; }
    public String getRating() { return rating; }
    public String getSource() { return source; }
    public String getLink() { return link; }
    public String getImage() { return image; }
}
