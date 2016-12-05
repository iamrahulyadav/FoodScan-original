package com.ph7.foodscan.models.scio;

import java.io.Serializable;

/**
 * Created by sony on 24-08-2016.
 */
public class SCIOResultDataModel implements Serializable {
    public String test_id ;
    public String test_name;
    public String test_note;
    public String test_location;
    public String model_ids;
    public String collection_id;
    public String test_scan_result;
    public int scan_count;
    public String test_status;
    public String create_datetime ;
    public String expire;
    public String imgs_path;
}
