package com.sanutty.keyvaluestore.app.util;

import org.apache.commons.text.StringEscapeUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.google.gson.JsonElement;
import com.sanutty.keyvaluestore.app.entities.KeyValueStoreResponse;

public class JSONUtilility
{
    public static String convertToJSON(KeyValueStoreResponse input) {
      Gson gson = new GsonBuilder()
          .excludeFieldsWithoutExposeAnnotation()
          .create();
      if (input.getResult() != null ) {
        String escapedJson =  gson.toJson( input );
        
        return StringEscapeUtils.unescapeJson(escapedJson);
      } else {
        return gson.toJson( input );
      }
      
    }
    
    public static boolean isJson(String Json) {
      try {
        Gson gson = new Gson();
          gson.fromJson(Json, Object.class);
          return true;
      } catch (com.google.gson.JsonSyntaxException ex) {
          return false;
      }
    }
    

}
