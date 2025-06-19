package com.sanutty.keyvaluestore.app.services;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sanutty.keyvaluestore.app.entities.KeyValueStoreRequest;
import com.sanutty.keyvaluestore.app.entities.KeyValueStoreResponse;
import com.sanutty.keyvaluestore.app.repositories.KeyValueRepository;
import com.sanutty.keyvaluestore.app.util.JSONUtilility;

@Service
public class KeyValueStoreService
{
  @Autowired
  private KeyValueRepository repository;
  
  /**
   * @param key
   * @return KeyValueStoreResponse with result, if any
   * @throws Exception
   */
  public KeyValueStoreResponse search(String key) throws Exception {
    KeyValueStoreResponse resp = null;
    
    try {
      resp = repository.search( key, null, true );
    } catch (Exception e) {
      throw e;
    }
    return resp;
  }
  
  /**
   * @param key
   * @param value
   * @return KeyValueStoreResponse indicating save status
   * @throws Exception
   */
  public KeyValueStoreResponse save(String key, String value) throws Exception {
    KeyValueStoreResponse resp = null;
    
    try {
      if (!JSONUtilility.isJson(value)) {
        throw new RuntimeException(KeyValueStoreResponse.MSG_INVALID_JSON);
      }
      resp = repository.save(key, value, null, true);
    } catch (Exception e) {
      throw e;
    }
    return resp;
  }
  
  /**
   * @param key
   * @return KeyValueStoreResponse delete status
   * @throws Exception 
   */
  public KeyValueStoreResponse delete(String key) throws Exception {
    KeyValueStoreResponse resp = null;
    try {
      resp = repository.deleteById( key, null, true );
    } catch (Exception e) {
      throw e;
    } 
    return resp;
  }
  
  /**
   * @param requests a list of KeyValueStoreRequest capturing action for each request with necessary parameters
   * @return A list of KeyValueStoreResponse objects each indicating the status of the requested action
   * @throws Exception
   */
  public List<KeyValueStoreResponse> compositeRequest(List<KeyValueStoreRequest> requests) throws Exception {
    List<KeyValueStoreResponse> respList = new LinkedList<>();
    
    respList = repository.processComposite( requests );
    
    return respList;
  }
  
  
}
