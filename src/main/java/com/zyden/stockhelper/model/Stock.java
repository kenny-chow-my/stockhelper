package com.zyden.stockhelper.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * Created by Kenny on 4/12/2017.
 */
public class Stock {

    @Id
    public String id;

    @Indexed
    public String name;

    private String symbol;

    @Indexed
    private Double price;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private Date lastRefreshed = new Date();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Date getLastRefreshed() {
        return lastRefreshed;
    }

    public void setLastRefreshed(Date lastRefreshed) {
        this.lastRefreshed = lastRefreshed;
    }

}
