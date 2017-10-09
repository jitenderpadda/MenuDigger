package com.jitender.menudigger.entity;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.datanucleus.api.jpa.annotations.Extension;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class Restaurant {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	@Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
	String id;	 
	@Persistent
	String restroName;
	@Persistent 
	String restroAddress;
	@Persistent 
	String latLong; 

	public String getLatLong() {
		return latLong;
	}

	public void setLatLong(String latLong) {
		this.latLong = latLong;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getRestroName() {
		return restroName;
	}

	public void setRestroName(String restroName) {
		this.restroName = restroName;
	}
	
	public String getRestAddress(){
		return restroAddress;
	}
	
	public void setRestAddress(String restroAddress){
		this.restroAddress = restroAddress;
	}
}
