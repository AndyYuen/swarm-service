package com.redhat.rhoar.swarm.customer.rest;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.redhat.rhoar.swarm.customer.model.Customer;
import com.redhat.rhoar.swarm.customer.service.CustomerService;

@Path("/")
@RequestScoped
public class CustomerEndpoint {

    @Inject
    private CustomerService customerService;

    @GET
    @Path("/customer/{customerId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Customer getCustomer(@PathParam("customerId") String itemId) {
        Customer customer = customerService.getCustomer(itemId);
        if (customer == null) {
            throw new NotFoundException();
        } else {
            return customer;
        }
    }
    
    @GET
    @Path("/customers")
    @Produces(MediaType.APPLICATION_JSON)
    public List getCustomers() {
        List list = customerService.getCustomers();
        if (list == null) {
            throw new NotFoundException();
        } else {
            return list;
        }
    }
    
    @POST
    @Path("/customer")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addCustomer(Customer customer) throws Exception{
        customerService.addCustomer(customer);
        return Response.status(200).build();
    }
}
