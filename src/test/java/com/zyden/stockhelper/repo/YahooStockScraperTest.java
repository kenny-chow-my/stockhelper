/*
 * Copyright (c) 2017. All rights reserved
 * @Author Kenny Chow
 */

package com.zyden.stockhelper.repo;

import com.zyden.stockhelper.model.Stock;
import com.zyden.stockhelper.datasource.YahooStockScraper;
import org.apache.commons.logging.Log;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = "unittest")
@SpringBootTest
public class YahooStockScraperTest {
    @Autowired
    private YahooStockScraper yahooStockScraper;


    private Log log =  org.apache.commons.logging.LogFactory.getLog(this.getClass());
    @Test
    public void testGetStock(){
        Stock stock = yahooStockScraper.getStock("FB");
        assertEquals(stock.getSymbol(), "FB");
        log.warn("Inside get stock");
    }

}