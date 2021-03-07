package com.techakram.nearhospital;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JsonParser
{
    private HashMap<String,String> ParserJsonObject(JSONObject object)
    {
      //initialize Hashmap.
        HashMap<String,String> dataList=new HashMap<>();
        try {
            //get name from object
            String name=object.getString("name");
            //get latitude from object
            String latitude=object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lat");
            //get longitude from object.
            String longitude=object.getJSONObject("geometry")
                    .getJSONObject("location").getString("lng");
            //put all value in Hashmap
            dataList.put("name",name);
            dataList.put("lat",latitude);
            dataList.put("lng",longitude);

        } catch (JSONException e) {
            e.printStackTrace( );
        }
        //Return Hashmap
        return dataList;
    }
    private List<HashMap<String,String>> ParseJsonArray(JSONArray jsonArray)
    {
        //Initialize Hash map List
        List<HashMap<String,String>> dataList=new ArrayList<>();
        for(int i=0;i<jsonArray.length();i++)
        {
            try
            {
                HashMap<String,String> data=ParserJsonObject((JSONObject) jsonArray.get(i));
                 // add data in dataList
                  dataList.add(data);
            } catch (JSONException e)
            {
                e.printStackTrace( );
            }
        }
        //return hash map list.
        return dataList;
    }
    public  List<HashMap<String,String>> parseresult(JSONObject object)
    {
        // initialize json array
        JSONArray jsonArray=null;
        try {
            jsonArray=object.getJSONArray("result");
        } catch (JSONException e) {
            e.printStackTrace( );
        }
        //Return JsonArray
        return ParseJsonArray(jsonArray);
    }
}
