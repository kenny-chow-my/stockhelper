package com.zyden.stockhelper.repo;

import com.zyden.stockhelper.model.Thing;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

/**
 * Created by Kenny on 4/18/2017.
 */
public interface ThingRepo extends MongoRepository<Thing, String> {
    @Query(value = "{ 'sha256' : ?0}")
    Thing findBySha256(String sha256);
}
