package com.redhat.rhoar.swarm.customer.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import org.wildfly.swarm.health.Health;
import org.wildfly.swarm.health.HealthStatus;


@Path("/")
public class HealthCheckEndpoint {

    @GET
    @Health
    @Path("/status")
    public HealthStatus check() {
    	return HealthStatus.named("server-state").up();
    }

}