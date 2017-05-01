package com.zyden.stockhelper.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

/**
 * Created by Kenny on 4/18/2017.
 */
@Document(collection = "userthings")
public class UserThing {

    @Id
    private String id;

    private String ownerId;

    @DBRef
    private Thing thing;

    @Indexed
    private List<String> selectedLabels;

    @Indexed
    private String title;

    private String description;

    private Date reminder;

    private Date lastModified;

    private String thumbnailDataURI;

    public String getThumbnailDataURI() {
        return thumbnailDataURI;
    }

    public void setThumbnailDataURI(String thumbnailDataURI) {
        this.thumbnailDataURI = thumbnailDataURI;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public List<String> getSelectedLabels() {
        return selectedLabels;
    }

    public void setSelectedLabels(List<String> selectedLabels) {
        this.selectedLabels = selectedLabels;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getReminder() {
        return reminder;
    }

    public void setReminder(Date reminder) {
        this.reminder = reminder;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Thing getThing() {
        return thing;
    }

    public void setThing(Thing thing) {
        this.thing = thing;
    }
}
