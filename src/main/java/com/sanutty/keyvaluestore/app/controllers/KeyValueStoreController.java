package com.sanutty.keyvaluestore.app.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sanutty.keyvaluestore.app.entities.ActionEnum;
import com.sanutty.keyvaluestore.app.entities.KeyValueStoreRequest;
import com.sanutty.keyvaluestore.app.entities.KeyValueStoreResponse;
import com.sanutty.keyvaluestore.app.entities.StatusEnum;
import com.sanutty.keyvaluestore.app.services.KeyValueStoreService;
import com.sanutty.keyvaluestore.app.util.JSONUtilility;

/**
 * The MemoryStoreController defines CRUD APIs for the in memory datastore. 
 */

@RestController
@RequestMapping("api/keyvaluestore")
public class KeyValueStoreController
{
  
  @Autowired
  private KeyValueStoreService keyValueStoreService;

  @GetMapping(path = "/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> search(@PathVariable String key) {
    KeyValueStoreResponse resp = null;
    try {
      resp = keyValueStoreService.search( key );
    } catch (Exception e) {
      resp = new KeyValueStoreResponse();
      resp.setStatus( StatusEnum.Error );
      resp.setMessage( e.getMessage());
      String response = JSONUtilility.convertToJSON( resp );
      return ResponseEntity.of( Optional.of(response) );
    }
    return ResponseEntity.ok( JSONUtilility.convertToJSON( resp ) );  
  }
  
  @PutMapping("/{key}")
  public ResponseEntity<String> save(@PathVariable String key, @RequestBody String value) {
    KeyValueStoreResponse resp;
    try {
      resp = keyValueStoreService.save(key, value);
    } catch (Exception e) {
      resp = new KeyValueStoreResponse();
      resp.setStatus( StatusEnum.Error );
      resp.setMessage( e.getMessage());
      String response = JSONUtilility.convertToJSON( resp );
      return ResponseEntity.of( Optional.of(response) );
    }
    return ResponseEntity.ok( JSONUtilility.convertToJSON( resp ) );  
  }

  @DeleteMapping(path = "/{key}")
  public ResponseEntity<String> delete(@PathVariable String key){
    KeyValueStoreResponse resp;
    try {
      resp = keyValueStoreService.delete(key);
    } catch (Exception e) {
      resp = new KeyValueStoreResponse();
      resp.setStatus( StatusEnum.Error );
      resp.setMessage( e.getMessage());
      String response = JSONUtilility.convertToJSON( resp );
      return ResponseEntity.of( Optional.of(response) );
    }
    return ResponseEntity.ok( JSONUtilility.convertToJSON( resp ) );  
  }
  
  @PostMapping(path = "/composite", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> compositeRequest(@RequestBody List<KeyValueStoreRequest> requests) throws Exception {
    StringBuilder buf = new StringBuilder();
    try {
      
      //-------------------------------------------
      // call the composite service
      //-------------------------------------------
      List<KeyValueStoreResponse> responses = keyValueStoreService.compositeRequest( requests );

      //-------------------------------------------
      // render the service response
      //-------------------------------------------
      for (KeyValueStoreResponse response : responses) {
        ActionEnum action = response.getAction();
        
        switch (action) {
          case Search:
              buf.append( "GET " + response.getKey() + "\n");
              buf.append( JSONUtilility.convertToJSON( response ) + "\n");
              break;
          case Save:
              buf.append( "PUT " + response.getKey() + " " + response.getOriginalPayload() + "\n");
              buf.append( JSONUtilility.convertToJSON( response ) + "\n");
              break;
          case Delete:
              buf.append( "DELETE " + response.getKey() + "\n");
              buf.append( JSONUtilility.convertToJSON( response ) + "\n");
              break;
        }
        
      } //for
    } catch (Exception e) {
      KeyValueStoreResponse resp = new KeyValueStoreResponse();
      resp.setStatus( StatusEnum.Error );
      resp.setMessage( e.getMessage());
      String response = JSONUtilility.convertToJSON( resp );
      return ResponseEntity.of( Optional.of(response) );
    }
    
    return ResponseEntity.ok(buf.toString());
  }
}
