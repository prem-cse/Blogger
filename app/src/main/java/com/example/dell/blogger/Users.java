package com.example.dell.blogger;

public class Users {

    private String Title;
    private String Desc;
    private String Image;
    private String username;

    public Users(){}

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getDesc() {
        return Desc;
    }

    public void setDesc(String desc) {
        Desc = desc;
    }

    public String getImage() {
        return Image;
    }

    public void setImage(String image) {
        Image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Users(String title, String desc, String image, String username) {
        Title = title;
        Desc = desc;
        Image = image;
        this.username = username;
    }
}
