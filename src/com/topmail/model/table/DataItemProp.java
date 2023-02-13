package com.topmail.model.table;

import com.secretlib.model.ChunkData;
import com.secretlib.model.HiDataBag;
import com.secretlib.util.Parameters;

/**
 * @author Florent FRADET
 */
public class DataItemProp {

    private String key;
    private String value;


    public DataItemProp(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }
    public String getValue() {
        return value;
    }


    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
