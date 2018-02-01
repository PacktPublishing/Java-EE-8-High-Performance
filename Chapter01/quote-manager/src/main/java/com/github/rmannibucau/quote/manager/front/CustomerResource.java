package com.github.rmannibucau.quote.manager.front;

import com.github.rmannibucau.quote.manager.front.json.JsonCustomer;
import com.github.rmannibucau.quote.manager.model.Customer;
import com.github.rmannibucau.quote.manager.service.CustomerService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

@Path("customer")
@ApplicationScoped
public class CustomerResource {
    @Inject
    private CustomerService customerService;

    @GET
    @Path("{id}")
    public JsonCustomer findById(@PathParam("id") final long id) {
        return customerService.findById(id)
                .map(quote -> {
                    final JsonCustomer json = new JsonCustomer();
                    json.setId(quote.getId());
                    json.setName(quote.getName());
                    return json;
                })
                .orElseThrow(() -> new WebApplicationException(Response.Status.NO_CONTENT));
    }

    @POST
    @Path("{id}")
    public JsonCustomer update(@PathParam("id") final long id, final JsonCustomer json) {
        customerService.mutate(id, optionalCustomer -> optionalCustomer.map(customer -> {
            customer.setName(customer.getName());
            return json;
        }).orElseThrow(() -> new WebApplicationException(Response.Status.NO_CONTENT)));
        return findById(id);
    }

    @POST
    public JsonCustomer create(final JsonCustomer customer) {
        final Customer newCustomer = new Customer();
        newCustomer.setName(customer.getName());
        customerService.create(newCustomer);
        return findById(newCustomer.getId());
    }

    @DELETE
    @Path("{id}")
    public void delete(@PathParam("id") final long id) {
        customerService.delete(id);
    }

    // todo: findall + pagination
}
