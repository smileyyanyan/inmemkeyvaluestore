package com.sanutty.keyvaluestore.app.repositories;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.sanutty.keyvaluestore.app.entities.ActionEnum;
import com.sanutty.keyvaluestore.app.entities.KeyValueStoreRequest;
import com.sanutty.keyvaluestore.app.entities.KeyValueStoreResponse;
import com.sanutty.keyvaluestore.app.entities.StatusEnum;
import com.sanutty.keyvaluestore.app.entities.Transaction;

public class MemStoreKeyValueRepository implements KeyValueRepository
{
  private Map<String, String> dataMap = new ConcurrentHashMap<>();

  @Override
  public KeyValueStoreResponse search( String key, Connection conn, boolean closeConnection ) throws Exception
  {
    String value = dataMap.get( key );
    KeyValueStoreResponse resp = new KeyValueStoreResponse();
    if (value == null) {
      resp.setStatus( StatusEnum.Error );
      throw new RuntimeException(KeyValueStoreResponse.MSG_KEY_NOT_FOUND);
    }
    
    resp.setResult( value );
    resp.setStatus( StatusEnum.OK );
    resp.setAction( ActionEnum.Search );
    resp.setKey( key );
    resp.setResult( value );
    
    return resp;
  }

  @Override
  public KeyValueStoreResponse save( String key, String value, Connection conn, boolean closeConnection ) throws Exception
  {
    KeyValueStoreResponse resp = new KeyValueStoreResponse();
    dataMap.put( key, value ); 
    resp.setStatus( StatusEnum.OK );
    resp.setAction( ActionEnum.Save );
    resp.setKey( key );
    resp.setOriginalPayload( value );
    
    return resp;
  }

  @Override
  public KeyValueStoreResponse deleteById( String key, Connection conn, boolean closeConnection ) throws Exception
  {
    String value = dataMap.get( key );
    KeyValueStoreResponse resp = new KeyValueStoreResponse();
    if (value == null) {
      resp.setStatus( StatusEnum.Error );
      throw new RuntimeException(KeyValueStoreResponse.MSG_KEY_NOT_FOUND);
    }
    
    dataMap.remove( key );
    resp.setStatus( StatusEnum.OK );
    resp.setAction( ActionEnum.Delete );
    resp.setKey( key );
    
    return resp;
  }

  @Override
  public List<KeyValueStoreResponse> processComposite( List<KeyValueStoreRequest> requests ) throws Exception
  {
    List<KeyValueStoreResponse> respList = new LinkedList<>();
    int sequence = 1;
    KeyValueStoreResponse searchOldValue;
    List<Transaction> transactionList = new ArrayList<>();
    
    for (KeyValueStoreRequest request : requests) {
      
      ActionEnum action = request.getAction();
      
      Transaction transaction = new Transaction();
      transaction.setRequest( request );
      transaction.setTransactionOrder( sequence++ );
      transactionList.add( transaction );
      
      switch (action) {
        case Save:
            try {
              searchOldValue = search(request.getKey(), null, false);
              transaction.setOldValue( searchOldValue.getResult() );
              transactionList.add( transaction );
            } catch (Exception e) {
              if (KeyValueStoreResponse.MSG_KEY_NOT_FOUND.equalsIgnoreCase(e.getMessage())) {
                continue;
              }
            }
            break;
        case Delete:
            try {
              searchOldValue = search(request.getKey(), null, false);
              transaction.setOldValue( searchOldValue.getResult() );
            } catch (Exception e) {
              if (KeyValueStoreResponse.MSG_KEY_NOT_FOUND.equalsIgnoreCase(e.getMessage())) {
                continue;
              }
            }
            break;
      }
    }
    
    if (processTransactions( transactionList )) {
      for (Transaction transaction: transactionList) {
        respList.add(transaction.getResponse());
      }
      return respList;
      
    } else {
      throw new RuntimeException("Error processing composite requests");
    }
  }
  
  /*
   * This function processes the sequence of transactions in the order they are received. 
   */
  private boolean processTransactions(List<Transaction> transactionList) throws Exception {
    boolean hasExecutionError = false;
    Comparator<Transaction> minComparator = new Comparator<Transaction>() {
      @Override
      public int compare( Transaction t1, Transaction t2 )
      {
        return Integer.compare( t1.getTransactionOrder() , t2.getTransactionOrder());
      }
    };
    
    //commit transactions in order using min heap
    PriorityQueue<Transaction> orderedTransactionList = new PriorityQueue<>(minComparator);
    orderedTransactionList.addAll( transactionList );
    
    for (Transaction eachTransaction  : orderedTransactionList) {
      
      ActionEnum action = eachTransaction.getRequest().getAction();
      switch (action) {
        case Search:
          try {
            KeyValueStoreResponse resp = this.search( eachTransaction.getRequest().getKey(), null, false );
            eachTransaction.setResponse( resp );
          } catch (Exception e) {
            hasExecutionError = true;
          }
          break;
        case Save:
            try {
              KeyValueStoreResponse resp = save( eachTransaction.getRequest().getKey(), eachTransaction.getRequest().getPayload().toString(),null, false );
              eachTransaction.setResponse( resp );
            } catch (Exception e) {
              hasExecutionError = true;
            }
            break;
        case Delete:
            try {
              KeyValueStoreResponse resp = deleteById( eachTransaction.getRequest().getKey(), null, false );
              eachTransaction.setResponse( resp );
            } catch (Exception e) {
              hasExecutionError = true;
            }
            break;
      }
    } // done processing all
    
    
    if (hasExecutionError) {
      rollback(transactionList);
      return false;
    } else {
      return true;
    }
  }
  
  public StatusEnum commit(List<Transaction> list) {
    
    Comparator<Transaction> minComparator = new Comparator<Transaction>() {
      @Override
      public int compare( Transaction t1, Transaction t2 )
      {
        return Integer.compare( t1.getTransactionOrder() , t2.getTransactionOrder());
      }
    };
    
    //commit transactions in order using min heap
    PriorityQueue<Transaction> orderedTransactionList = new PriorityQueue<>(minComparator);
    orderedTransactionList.addAll( list );
    
    orderedTransactionList.forEach( transaction -> {
      KeyValueStoreRequest request = transaction.getRequest();
      
      request.getAction();
      
    } );
    
    return StatusEnum.OK;
  }
  
  public StatusEnum rollback(List<Transaction> transactionList) throws Exception {
    
    Comparator<Transaction> maxComparator = new Comparator<Transaction>() {
      @Override
      public int compare( Transaction t1, Transaction t2 )
      {
        //the value 0 if x == y;a value less than 0 if x < y; anda value greater than 0 if x > y
        if (t1.getTransactionOrder() == t2.getTransactionOrder()) {
          return 0;
        } else if (t1.getTransactionOrder() < t2.getTransactionOrder())  { //this is the max heap part
          return 1;
        } else {
          return -1;
        }
          
      }
    };
    
    //rollback transactions in reverse order using max heap
    PriorityQueue<Transaction> orderedTransactionList = new PriorityQueue<>(maxComparator);
    orderedTransactionList.addAll( transactionList );
    
    for (Transaction transaction : orderedTransactionList) {
      ActionEnum action = transaction.getRequest().getAction();
      switch (action) {
        case Save:
            save( transaction.getRequest().getKey(), transaction.getOldValue(),null, false );
            break;
        case Delete:
            save( transaction.getRequest().getKey(), transaction.getOldValue() ,null, false );
            break;
        }
    }
    return StatusEnum.OK;
  }

}
