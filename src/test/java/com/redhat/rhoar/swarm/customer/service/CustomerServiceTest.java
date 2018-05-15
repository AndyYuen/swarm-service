package com.redhat.rhoar.swarm.customer.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Properties;

import javax.inject.Inject;


import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;

import com.redhat.rhoar.swarm.customer.model.Customer;
import com.redhat.rhoar.swarm.customer.service.CustomerService;

@RunWith(Arquillian.class)
public class CustomerServiceTest {
    
    private static String port="18081";

    
    // keep track of # of rows in the database as addCustomer will change this value
    // META-INF/test-load.sql loads 2 rows
    private static int rows = 2; 
    
    @CreateSwarm
    public static Swarm newContainer() throws Exception {
    	System.out.println("@@CreateSwarm...");
        Properties properties = new Properties();
        properties.put("swarm.http.port", port);
        return new Swarm(properties).withProfile("local");
    }

    @Deployment
    public static Archive<?> createDeployment() {

        System.out.println("@Deployment...");
    	
        return ShrinkWrap.create(WebArchive.class)
                .addPackages(true, CustomerService.class.getPackage())
                .addPackages(true, Customer.class.getPackage())
                .addAsResource("project-local.yml", "project-local.yml")
                .addAsResource("META-INF/test-persistence.xml",  "META-INF/persistence.xml")
                .addAsResource("META-INF/test-load.sql",  "META-INF/test-load.sql");
    }


    @Inject
    private CustomerService customerService;

    @Test
    public void getCustomer() throws Exception {
        assertThat(customerService, notNullValue());
        Customer customer = customerService.getCustomer("A01");
        assertThat(customer, notNullValue());
        assertThat(customer.getBalance(), is(1000));
    }
    
    @Test
    public void getCustomers() throws Exception {
        assertThat(customerService, notNullValue());
        List list = customerService.getCustomers();
        // rows may have changed
        assertThat(list.size(), is(rows));
        assertThat(list, notNullValue());
        Customer customer = (Customer) list.get(0);
        assertThat(customer.getBalance(), is(1000));
    }
    
    @Test
    public void getNonExistingCustomer() throws Exception {
        assertThat(customerService, notNullValue());
        Customer customer = customerService.getCustomer("A999");
        assertThat(customer, nullValue());
    }
    
    @Test
    public void addCustomer() throws Exception {
        assertThat(customerService, notNullValue());
        Customer cust1 = new Customer();
        cust1.setCustomerId("A999");
        cust1.setVipStatus("Platinum");
        cust1.setBalance(2000);
        customerService.addCustomer(cust1);
        List list = customerService.getCustomers();
        assertThat(list.size(), is(3));
        rows = 3;
        Customer cust2 = customerService.getCustomer(cust1.getCustomerId());
        assertThat(cust2, notNullValue());
        assertThat(cust2.getBalance(), is(2000));
        assertThat(cust2.getVipStatus(), is("Platinum"));
    }
}
