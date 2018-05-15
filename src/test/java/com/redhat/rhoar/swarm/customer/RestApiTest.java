package com.redhat.rhoar.swarm.customer;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.time.DurationFormatUtils;
import org.apache.commons.lang.time.StopWatch;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.wildfly.swarm.Swarm;
import org.wildfly.swarm.arquillian.CreateSwarm;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.redhat.rhoar.swarm.customer.CustomerRestApplication;
import com.redhat.rhoar.swarm.customer.model.Customer;

@RunWith(Arquillian.class)
public class RestApiTest {

    private static String port = System.getProperty("arquillian.swarm.http.port", "18080");
    
    // set up stop watches to time execution and overhead
    private static StopWatch totalSW = new StopWatch();
    private static StopWatch testSW = new StopWatch();
    
    private Client client;

    @CreateSwarm
    public static Swarm newContainer() throws Exception {
        Properties properties = new Properties();
        properties.put("swarm.http.port", port);
        return new Swarm(properties).withProfile("local");
    }

    @Deployment
    public static Archive<?> createDeployment() {
    	totalSW.start();

        return ShrinkWrap.create(WebArchive.class)
                .addPackages(true, CustomerRestApplication.class.getPackage())
                .addPackages(true, StopWatch.class.getPackage())
                .addAsResource("project-local.yml", "project-local.yml")
                .addAsResource("META-INF/test-persistence.xml",  "META-INF/persistence.xml")
                .addAsResource("META-INF/test-load.sql",  "META-INF/test-load.sql");
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        testSW.start();
        testSW.suspend();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        totalSW.stop();
        testSW.stop();
        
        // display overall elapsed time and execution/overhead percentage
        System.out.println("********************************************************");
        System.out.println(String.format("Total Runtime: %10s", totalSW.toString()));
        System.out.println(String.format("Test Runtime : %10s or %.2f percent", testSW.toString()
        		, (double) testSW.getTime() / (double) totalSW.getTime()));
        System.out.println(String.format("Overhead     : %10s or %.2f percent"
        		, DurationFormatUtils.formatDurationHMS((totalSW.getTime() - testSW.getTime()))
        		,(double) (totalSW.getTime() - testSW.getTime()) / (double) totalSW.getTime()));
        System.out.println("********************************************************");
    }
    
    @Before
    public void before() throws Exception {
    	testSW.resume();
        client = ClientBuilder.newClient();
    }

    @After
    public void after() throws Exception {
        client.close();
        testSW.suspend();
    }

    @Test
    @RunAsClient
    public void testGetCustomer() throws Exception {
        WebTarget target = client.target("http://localhost:" + port).path("/customer").path("/A01");
        Response response = target.request(MediaType.APPLICATION_JSON).get();
        assertThat(response.getStatus(), equalTo(new Integer(200)));
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        assertThat(value.getString("customerId", null), equalTo("A01"));
        assertThat(value.getString("vipStatus", null), equalTo("Diamond"));
        assertThat(value.getInt("balance", 0), equalTo(new Integer(1000)));
    }
    
    @Test
    @RunAsClient
    public void testGetCustomers() throws Exception {
        WebTarget target = client.target("http://localhost:" + port).path("/customers");
        Response response = target.request(MediaType.APPLICATION_JSON).get();
        assertThat(response.getStatus(), equalTo(new Integer(200)));
        JsonArray array = Json.parse(response.readEntity(String.class)).asArray();

        assertThat(array.size(), is(2));
        JsonObject value = array.get(0).asObject();
        assertThat(value.getString("customerId", null), equalTo("A01"));
        assertThat(value.getString("vipStatus", null), equalTo("Diamond"));
        assertThat(value.getInt("balance", 0), equalTo(new Integer(1000)));
    }
    
    @Test
    @RunAsClient
    public void testAddCustomer() throws Exception {
    	Customer newCustomer = new Customer();
    	newCustomer.setCustomerId("A999");
    	newCustomer.setVipStatus("Silver");
    	newCustomer.setBalance(500);
        WebTarget target = client.target("http://localhost:" + port).path("/customer");
        Response response = target.request(MediaType.APPLICATION_JSON)
        		.accept(MediaType.APPLICATION_JSON).post(Entity.json(newCustomer));
        assertThat(response.getStatus(), equalTo(new Integer(200)));

        client = ClientBuilder.newClient();
        target = client.target("http://localhost:" + port)
        		.path("/customer/").path(newCustomer.getCustomerId());
        response = target.request(MediaType.APPLICATION_JSON).get();
        assertThat(response.getStatus(), equalTo(new Integer(200)));
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        assertThat(value.getString("customerId", null), equalTo(newCustomer.getCustomerId()));
        assertThat(value.getString("vipStatus", null), equalTo(newCustomer.getVipStatus()));
        assertThat(value.getInt("balance", 0), equalTo(newCustomer.getBalance()));
    }
    
    @Test
    @RunAsClient
    public void testGetCustomerWhenItemIdDoesNotExist() throws Exception {
        WebTarget target = client.target("http://localhost:" + port).path("/customer").path("/doesnotexist");
        Response response = target.request(MediaType.APPLICATION_JSON).get();
        assertThat(response.getStatus(), equalTo(new Integer(404)));
    }
    
    @Test
    @RunAsClient
    public void testHealthCheckCombined() throws Exception {
        WebTarget target = client.target("http://localhost:" + port).path("/health");
        Response response = target.request(MediaType.APPLICATION_JSON).get();
        assertThat(response.getStatus(), equalTo(new Integer(200)));
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        assertThat(value.getString("outcome", ""), equalTo("UP"));
        JsonArray checks = value.get("checks").asArray();
        assertThat(checks.size(), equalTo(new Integer(1)));
        JsonObject state = checks.get(0).asObject();
        assertThat(state.getString("id", ""), equalTo("server-state"));
        assertThat(state.getString("result", ""), equalTo("UP"));
    }
    
    @Test
    @RunAsClient
    public void testHealthCheckStatus() throws Exception {
        WebTarget target = client.target("http://localhost:"+ port).path("/status");
        Response response = target.request(MediaType.APPLICATION_JSON).get();
        assertThat(response.getStatus(), equalTo(new Integer(200)));
        JsonObject value = Json.parse(response.readEntity(String.class)).asObject();
        assertThat(value.getString("id", ""), equalTo("server-state"));
        assertThat(value.getString("result", ""), equalTo("UP"));
    }
}