package com.sanutty.keyvaluestore.app.entities;

import com.google.gson.annotations.Expose;

/**
 * The KeyValueResponse is used to store the response for each requested action
 */
public class KeyValueStoreResponse
{
  
  public static final String MSG_KEY_NOT_FOUND = "Key not found".intern();
  public static final String MSG_INVALID_JSON = "Invalid JSON".intern();
  public static final String MSG_EXECUTION_ERROR = "Error during execution ".intern();
  
  @Expose private StatusEnum status;
  @Expose private String result;
  @Expose private String mesg;
  
  private String key;
  private ActionEnum action;
  private String originalPayload; 
  
  public String getOriginalPayload()
  {
    return originalPayload;
  }
  public void setOriginalPayload( String originalPayload )
  {
    this.originalPayload = originalPayload;
  }
  public String getKey()
  {
    return key;
  }
  public ActionEnum getAction()
  {
    return action;
  }
  public void setAction( ActionEnum action )
  {
    this.action = action;
  }
  public void setKey( String key )
  {
    this.key = key;
  }
  public StatusEnum getStatus()
  {
    return status;
  }
  public void setStatus( StatusEnum status )
  {
    this.status = status;
  }
  public String getResult()
  {
    return result;
  }
  public void setResult( String result )
  {
    this.result = result;
  }
  public String getMessage()
  {
    return mesg;
  }
  public void setMessage( String message )
  {
    this.mesg = message;
  }
  
  
  
}
