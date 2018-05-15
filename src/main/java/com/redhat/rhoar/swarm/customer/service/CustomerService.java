package com.redhat.rhoar.swarm.customer.service;


import java.util.List;

import javax.annotation.Resource;
import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.transaction.UserTransaction;

import com.redhat.rhoar.swarm.customer.model.Customer;

@ApplicationScoped
public class CustomerService {
	@PersistenceContext(unitName = "customer", type = PersistenceContextType.EXTENDED)
	EntityManager em;
	
	@Resource
    private UserTransaction userTransaction;

	public Customer getCustomer(String customerId) {
        Customer customer = em.find(Customer.class, customerId);
        return customer;
	}
	
	public List getCustomers() {
        return em.createQuery("select c from Customer c").getResultList();
	}
	
	public void addCustomer(Customer customer) throws Exception {
		userTransaction.begin();
		em.persist(customer);
		userTransaction.commit();

	}

}
