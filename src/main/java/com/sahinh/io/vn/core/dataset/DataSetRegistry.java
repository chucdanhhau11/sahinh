package com.sahinh.io.vn.core.dataset;

import javax.sql.DataSource;
import org.springframework.stereotype.Component;

/** Registry giữ 1 DataSet duy nhất cho app ("sahinh"). */
@Component
public class DataSetRegistry {
    private final DataSet dataSet;

    public DataSetRegistry(DataSource dataSource) {
        this.dataSet = new DataSet(dataSource);
    }

    public DataSet get(String name) {
        return dataSet;
    }

    public DataSet getDefault() {
        return dataSet;
    }
}
