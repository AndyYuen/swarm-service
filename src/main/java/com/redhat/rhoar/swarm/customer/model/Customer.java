package com.redhat.rhoar.swarm.customer.model;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CUSTOMER")
public class Customer {
	@Id
	private String customerId;
	private String vipStatus;
    private Integer balance;
    
    public String getCustomerId() {
		return customerId;
	}
    
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	
	public String getVipStatus() {
		return vipStatus;
	}
	
	public void setVipStatus(String vipStatus) {
		this.vipStatus = vipStatus;
	}
	
	public Integer getBalance() {
		return balance;
	}
	
	public void setBalance(Integer balance) {
		this.balance = balance;
	}



}
