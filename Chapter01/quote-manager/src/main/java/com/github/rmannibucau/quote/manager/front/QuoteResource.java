package com.github.rmannibucau.quote.manager.front;

import com.github.rmannibucau.quote.manager.front.json.JsonQuote;
import com.github.rmannibucau.quote.manager.front.json.JsonQuotePage;
import com.github.rmannibucau.quote.manager.service.QuoteService;

import javax.annotation.Resource;
import javax.enterprise.concurrent.ManagedExecutorService;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Response;
import java.util.Collection;
import java.util.List;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

@Path("quote")
@RequestScoped
public class QuoteResource {
    @Inject
    private QuoteService quoteService;

    @Resource
    private ManagedExecutorService managedExecutorService;

    @GET
    @Path("{id}")
    public JsonQuote findById(@PathParam("id") final long id) {
        return quoteService.findById(id)
                .map(quote -> {
                    final JsonQuote json = new JsonQuote();
                    json.setId(quote.getId());
                    json.setName(quote.getName());
                    json.setValue(quote.getValue());
                    json.setCustomerCount(ofNullable(quote.getCustomers()).map(Collection::size).orElse(0));
                    return json;
                })
                .orElseThrow(() -> new WebApplicationException(Response.Status.NO_CONTENT));
    }

    @GET
    public void findAll(@Suspended final AsyncResponse response,
                        @QueryParam("from") @DefaultValue("0") final int from,
                        @QueryParam("to") @DefaultValue("10") final int to) {
        managedExecutorService.execute(() -> {
            try {
                final long total = quoteService.countAll();
                final List<JsonQuote> items = quoteService.findAll(from, to)
                        .map(quote -> {
                            final JsonQuote json = new JsonQuote();
                            json.setId(quote.getId());
                            json.setName(quote.getName());
                            json.setValue(quote.getValue());
                            json.setCustomerCount(ofNullable(quote.getCustomers()).map(Collection::size).orElse(0));
                            return json;
                        })
                        .collect(toList());

                final JsonQuotePage page = new JsonQuotePage();
                page.setItems(items);
                page.setTotal(total);
                response.resume(page);
            } catch (final RuntimeException re) {
                response.resume(re);
            }
        });
    }

    // todo: update + delete
}
