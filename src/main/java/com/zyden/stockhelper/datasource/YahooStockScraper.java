package com.zyden.stockhelper.datasource;

import com.nhefner.main.StockFetcher;
import com.zyden.stockhelper.model.Stock;
import org.springframework.stereotype.Component;

/**
 * Created by Kenny on 4/12/2017.
 */

@Component
public class YahooStockScraper {


    public Stock getStock(String symbol) {
        StockFetcher sf = new StockFetcher();
        com.nhefner.main.Stock s = sf.getStock(symbol);
        Stock s1 = new Stock();
        s1.setName(s.getName());
        s1.setSymbol(s.getSymbol());
        s1.setPrice(s.getPrice());

        return s1;
    }

}
