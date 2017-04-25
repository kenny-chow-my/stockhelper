package com.zyden.stockhelper.repo;


import com.zyden.stockhelper.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by Kenny on 4/18/2017.
 */
@Repository
public interface UserRepo extends MongoRepository<User, String> {

}
