package com.sanutty.keyvaluestore.app.entities;

public class Transaction
{
  private int transactionOrder;
  private KeyValueStoreRequest request;
  private KeyValueStoreResponse response;
  private String oldValue;
  private boolean committed;
  
  public int getTransactionOrder()
  {
    return transactionOrder;
  }
  
  public void setTransactionOrder( int transactionOrder )
  {
    this.transactionOrder = transactionOrder;
  }
  public KeyValueStoreRequest getRequest()
  {
    return request;
  }
  public void setRequest( KeyValueStoreRequest request )
  {
    this.request = request;
  }
  public String getOldValue()
  {
    return oldValue;
  }
  public void setOldValue( String oldValue )
  {
    this.oldValue = oldValue;
  }
  public boolean isCommitted()
  {
    return committed;
  }
  public void setCommitted( boolean committed )
  {
    this.committed = committed;
  }
  
  public KeyValueStoreResponse getResponse()
  {
    return response;
  }

  public void setResponse( KeyValueStoreResponse response )
  {
    this.response = response;
  }
  
}
