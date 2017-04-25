package com.zyden.stockhelper.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Created by Kenny on 4/18/2017.
 */
@Document(collection = "things")
public class Thing {
    @Id
    private String id;
    private Map<String, BigDecimal> labelScore;
    @Indexed
    private String sha256;
    private String thumbnailPngBase64;
    private Date dateAdded;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Thing thing = (Thing) o;

        return getSha256().equals(thing.getSha256());
    }

    @Override
    public int hashCode() {
        return getSha256().hashCode();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, BigDecimal> getLabelScore() {
        return labelScore;
    }

    public void setLabelScore(Map<String, BigDecimal> labelScore) {
        this.labelScore = labelScore;
    }

    public String getThumbnailPngBase64() {
        return thumbnailPngBase64;
    }

    public void setThumbnailPngBase64(String thumbnailPngBase64) {
        this.thumbnailPngBase64 = thumbnailPngBase64;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }
}
