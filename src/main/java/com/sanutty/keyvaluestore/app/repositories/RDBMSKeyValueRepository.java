package com.sanutty.keyvaluestore.app.repositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;

import com.sanutty.keyvaluestore.app.entities.ActionEnum;
import com.sanutty.keyvaluestore.app.entities.KeyValueStoreRequest;
import com.sanutty.keyvaluestore.app.entities.KeyValueStoreResponse;
import com.sanutty.keyvaluestore.app.entities.StatusEnum;

public class RDBMSKeyValueRepository implements KeyValueRepository {
  
  @Value("${spring.datasource.driver-class-name}")
  private String JDBC_DRIVER;
  
  @Value("${spring.datasource.url}")
  private String DB_URL;
  
  @Value("${spring.datasource.username}")
  private String USER;
  
  @Value("${spring.datasource.password}")
  private String PASS;
  
  private static final String GET_VALUE_FROM_KEY_SQL = "select search_value from keyvaluepairs where search_key = ?";
  private static final String UPDATE_VALUE_FROM_KEY_SQL = "update keyvaluepairs set search_value = ? where search_key = ?";
  private static final String INSERT_VALUE_SQL = "insert into keyvaluepairs (search_key, search_value) values(?,?)";
  private static final String DELETE_VALUE_SQL = "delete from keyvaluepairs where search_key = ?";
  
  
  /**
   * Given a search key, retrun the KeyValueEntity object of the search key exists.
   * @param key string key to be searched
   * @param conn Connection to database, passed in if from another method
   * @param closeConnection boolean to indicate whether to close the connection. False if the caller will handle connection closing. 
   * @return KeyValueStoreResponse
   * @throws Exception during processing
   */
  public KeyValueStoreResponse search(String key, Connection conn, boolean closeConnection) throws Exception {
    ResultSet rs = null;
    PreparedStatement stmt = null;
    KeyValueStoreResponse resp = new KeyValueStoreResponse();
    
    try {
      
      if (conn == null) {
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
      }
      stmt = conn.prepareStatement(GET_VALUE_FROM_KEY_SQL);
      
      //set the search key parameter
      stmt.setString(1, key);        
      //execute the query
      rs = stmt.executeQuery();
      
      if (rs.next()) {
        String value = rs.getString("search_value");
        rs.close();
        resp.setResult( value );
        resp.setStatus( StatusEnum.OK );
        resp.setAction( ActionEnum.Search );
        resp.setKey( key );
        return resp;
      } else {
        rs.close();
        throw new RuntimeException (KeyValueStoreResponse.MSG_KEY_NOT_FOUND);
      }
    } catch (Exception e) {
      throw e;
    } finally {
      try {
        if (rs != null ) {
          rs.close();
        }
        stmt.close();
        if (closeConnection) {
          conn.close();
        }                
      } catch (Exception e) {
        throw e;
      }
    }
  }
  
  /**
   * Save either updates or inserts a row to the table depending on whether the key exists already
   * @param key string key to be saved
   * @param value string value to be saved
   * @param conn Connection to database, passed in if from another method
   * @param closeConnection boolean to indicate whether to close the connection. False if the caller will handle connection closing. 
   * @return KeyValueStoreResponse
   * @throws Exception during processing
   */
  public KeyValueStoreResponse save(String key, String value, Connection conn, boolean closeConnection) throws Exception {
    PreparedStatement stmt = null;
    KeyValueStoreResponse resp = new KeyValueStoreResponse();
    boolean exists = false;
    
    try {
      
      if (conn == null) {
        conn = DriverManager.getConnection(DB_URL, USER, PASS);
      }
      //Does the key exist
      try {
        KeyValueStoreResponse searchResp = search(key, conn, false);
        if (searchResp.getStatus() == StatusEnum.OK) {
          exists = true;
        }
      } catch (Exception e) {
        if (KeyValueStoreResponse.MSG_KEY_NOT_FOUND.equalsIgnoreCase(e.getMessage())) {
          exists = false;
        } else {
          throw e;
        }
      }
      
      if (!exists) {        
        //insert
        stmt = conn.prepareStatement( INSERT_VALUE_SQL );
        stmt.setString(1, key);
        stmt.setString(2, value);        
      } else {
        //update
        stmt = conn.prepareStatement( UPDATE_VALUE_FROM_KEY_SQL );
        stmt.setString(1, value);
        stmt.setString(2, key); 
      }      
      stmt.execute();      
      resp.setStatus( StatusEnum.OK );
      resp.setKey( key );
      resp.setOriginalPayload( value );
      resp.setAction( ActionEnum.Save);
      return resp;
    } catch (Exception e) {
      if (closeConnection) {
        conn.rollback();
      }
      throw e;
    } finally {
      try {       
        stmt.close();
        if (closeConnection) {
          conn.close();
        }        
      } catch (Exception e) {
        throw e;
      }
    }
  }
  
  /**
   * Deletes the record of a given key
   * @param key string key to be deleted
   * @param conn Connection to database, passed in if from another method
   * @param closeConnection boolean to indicate whether to close the connection. False if the caller will handle connection closing. 
   * @return KeyValueStoreResponse
   * @throws Exception during processing or key not found
   */
  public KeyValueStoreResponse deleteById(String key, Connection conn, boolean closeConnection) throws Exception  {
      PreparedStatement stmt = null;
      KeyValueStoreResponse resp = new KeyValueStoreResponse();
      boolean exists = false;
      
      try {
        
        if (conn == null) {
          conn = DriverManager.getConnection(DB_URL, USER, PASS);
        }
        //Does the key exist
        try {
          KeyValueStoreResponse searchResp = search(key, conn, false);
          if (searchResp.getStatus() == StatusEnum.OK) {
            exists = true;
          }
        } catch (Exception e) {
          if (KeyValueStoreResponse.MSG_KEY_NOT_FOUND.equalsIgnoreCase(e.getMessage())) {
            exists = false;
          } else {
            throw e;
          }
        }
        
        if (exists) {
          stmt = conn.prepareStatement(DELETE_VALUE_SQL) ;
          stmt.setString(1, key);
          stmt.execute();
          resp.setStatus( StatusEnum.OK );
          resp.setAction( ActionEnum.Delete );
          resp.setKey( key );
          return resp;
        } else {
          throw new RuntimeException (KeyValueStoreResponse.MSG_KEY_NOT_FOUND);
        }
      } catch (Exception e) {
        if (closeConnection) {
          conn.rollback();
        }
        throw e;
      } finally {
        try {
          if (stmt != null ) {
            stmt.close();
          }          
          if (closeConnection) {
            conn.close();
          }          
        } catch (Exception e) {
          throw e;
        }
      }
  }
  
  /**
   * This function processes multiple requests at the same time. The requests are processed in a transaction in all or nothing fashion.
   * If one request fails, the entire process will fail. 
   * @param requests a list of KeyValueStoreRequest, one for each action to take. 
   * @return List<KeyValueStoreResponse> 
   * @throws Exception
   */
  
  public List<KeyValueStoreResponse> processComposite(List<KeyValueStoreRequest> requests) throws Exception {
    Connection conn = null;
    boolean hasExecutionError = false;
    List<KeyValueStoreResponse> respList = new LinkedList<>();
    
    try {
      conn = DriverManager.getConnection(DB_URL, USER, PASS);      
      //auto commit set to false to allow multiple executions to commit or rollback together
      conn.setAutoCommit( false );
      
        for (KeyValueStoreRequest request : requests) {
          ActionEnum action = request.getAction();
          
          switch (action) {
            case Search:
                KeyValueStoreResponse searchResp = null;
                try {
                  searchResp = search( request.getKey(), conn, false );
                  respList.add( searchResp );
                } catch (Exception e) {
                  hasExecutionError = true;
                }
                break;
            case Save:
                try {
                  KeyValueStoreResponse saveResp = save( request.getKey(), request.getPayload().toString(), conn, false );
                  respList.add( saveResp );
                } catch (Exception e) {
                  hasExecutionError = true;
                }
                break;
            case Delete:
                try {
                  KeyValueStoreResponse deleteResp = deleteById( request.getKey(), conn, false);
                  respList.add( deleteResp );
                } catch (Exception e) {
                  hasExecutionError = true;
                }
                break;
          }
        }        
        
        //roll back if any errors occurred 
        if (hasExecutionError) {
          conn.rollback();
          throw new RuntimeException (KeyValueStoreResponse.MSG_EXECUTION_ERROR);
        } else {
          //everything was successful, now commit all transactions
          conn.commit();
          return respList;
        }
    }//try
    catch (Exception e) {
      conn.rollback();
      throw e;
    } finally {
      try {
        conn.close();
      } catch (Exception e) {
        throw e;
      }
    }
  }
  
}

