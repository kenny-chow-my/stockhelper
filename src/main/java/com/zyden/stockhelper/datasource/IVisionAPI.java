package com.zyden.stockhelper.datasource;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.GeneralSecurityException;
import java.util.Map;

/**
 * Created by Kenny on 4/19/2017.
 */
public interface IVisionAPI {
    Map<String, BigDecimal> getLabels(String path);
    Map<String, BigDecimal> getLabels(byte[] data);
}
