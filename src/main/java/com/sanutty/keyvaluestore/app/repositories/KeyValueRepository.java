package com.sanutty.keyvaluestore.app.repositories;

import java.sql.Connection;
import java.util.List;

import com.sanutty.keyvaluestore.app.entities.KeyValueStoreRequest;
import com.sanutty.keyvaluestore.app.entities.KeyValueStoreResponse;

public interface KeyValueRepository
{
  public KeyValueStoreResponse search(String key, Connection conn, boolean closeConnection) throws Exception ;
  public KeyValueStoreResponse save(String key, String value, Connection conn, boolean closeConnection) throws Exception;
  public KeyValueStoreResponse deleteById(String key, Connection conn, boolean closeConnection) throws Exception;
  public List<KeyValueStoreResponse> processComposite(List<KeyValueStoreRequest> requests) throws Exception;
}
