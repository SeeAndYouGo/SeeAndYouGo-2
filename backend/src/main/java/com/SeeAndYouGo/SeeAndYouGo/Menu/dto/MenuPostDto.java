package com.SeeAndYouGo.SeeAndYouGo.menu.dto;

import lombok.*;

@Getter
public class MenuPostDto {
    private String latitude;
    private String longitude;
    private String title;
    private String content;

    @Builder
    public MenuPostDto(String latitude, String longitude, String title, String content) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.title = title;
        this.content = content;
    }

    @Override
    public String toString() {
        return "MenuPostDto{" +
                "latitude='" + latitude + '\'' +
                ", longitude='" + longitude + '\'' +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
