/*
 * Copyright (c) 2017. All rights reserved
 * @Author Kenny Chow
 */

package com.zyden.stockhelper.controller;

import com.zyden.stockhelper.model.Stock;
import com.zyden.stockhelper.datasource.YahooStockScraper;
import com.zyden.stockhelper.repo.StockRepo;
import org.apache.commons.logging.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/v1/stocks")
public class StockController {

    @Autowired
    private YahooStockScraper yahooStockScraper;

    @Autowired
    private StockRepo stocks;

    private Log log =  org.apache.commons.logging.LogFactory.getLog(this.getClass());

    @RequestMapping(method = RequestMethod.GET, value = "scrape/{symbol}")
    public @ResponseBody Stock getFromScraper(@PathVariable String symbol){
        Stock got = yahooStockScraper.getStock(symbol);
        return got;
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody List<Stock> getStocks(){
        List<Stock> allStock = stocks.findAll();
        allStock.forEach(item-> {
            if (item.getLastRefreshed().getTime() + TimeUnit.DAYS.toMillis(1)  > new Date().getTime()) {
                log.info("Getting update for stock symbol: " + item.getSymbol());
                Stock s = this.getFromScraper(item.getSymbol());
                item.setName(s.getName());
                item.setPrice(s.getPrice());
                item.setLastRefreshed(new Date());
            }
        });
        return allStock;
    }


    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody Stock addStock(@RequestBody Stock newStock){
        Stock saved = stocks.save(newStock);
        return saved;
    }
}

