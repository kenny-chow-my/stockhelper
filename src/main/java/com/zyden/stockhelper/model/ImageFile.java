package com.zyden.stockhelper.model;

import org.springframework.data.annotation.Id;

/**
 * Created by Kenny on 4/19/2017.
 */
public class ImageFile {
    @Id
    private String id;
    private byte[] image;
    private String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }
}
