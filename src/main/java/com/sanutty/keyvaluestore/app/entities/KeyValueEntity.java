package com.sanutty.keyvaluestore.app.entities;

import jakarta.persistence.*;

@Entity
@Table(name = "keyvaluepairs")
public class KeyValueEntity 
{ 
    @Id 
    @Column(length = 125, nullable = false)
    private String searchKey;
    
    @Column(length = 2048, nullable = false)
    private String searchValue;
    
    public String getKey()
    {
      return searchKey;
    }
    public void setKey( String key )
    {
      this.searchKey = key;
    }
    public String getValue()
    {
      return searchValue;
    }
    public void setValue( String value )
    {
      this.searchValue = value;
    }
    
}
