package com.zyden.stockhelper.repo;

import com.zyden.stockhelper.datasource.YahooStockScraper;
import com.zyden.stockhelper.model.Stock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Kenny on 4/16/2017.
 */

@Repository
public interface StockRepo extends MongoRepository<Stock, String>{

}
