package com.zyden.stockhelper.datasource;

import com.mongodb.util.JSON;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by Kenny on 4/18/2017.
 */
@RunWith(SpringRunner.class)
@ActiveProfiles(profiles = "unittest")
@SpringBootTest
public class GoogleVisionAPITest {

    @Autowired
    private GoogleVisionAPI googleVisionAPI;

    /*
    Run only when needed. This test costs money.
    You can mock your own results:
    	food (score: 0.939)
	    egg (score: 0.899)
	    dish (score: 0.846)
     */
    @Ignore("Costly test it hits Google API, Need to mock the Google API calls") @Test
    public void run() throws Exception {
        Map<String, BigDecimal> labels = this.googleVisionAPI.getLabels("C:\\tmp\\test.jpg");
        System.out.println("====================== ----------- ================");
        System.out.println(JSON.serialize(labels));
    }


}