package com.sanutty.keyvaluestore.app.entities;

import com.fasterxml.jackson.databind.JsonNode;

public class KeyValueStoreRequest
{
  String key;
  ActionEnum action;
  JsonNode payload;
  
  public String getKey()
  {
    return key;
  }
  public void setKey( String key )
  {
    this.key = key;
  }
  public ActionEnum getAction()
  {
    return action;
  }
  public void setAction( ActionEnum action )
  {
    this.action = action;
  }
  public JsonNode getPayload()
  {
    return payload;
  }
  public void setPayload( JsonNode payload )
  {
    this.payload = payload;
  }
  
  
}
