package com.github.rmannibucau.quote.manager.front.json;

import javax.json.bind.annotation.JsonbPropertyOrder;
import java.util.List;

@JsonbPropertyOrder({"total", "items"})
public class JsonQuotePage {
    private long total;
    private List<JsonQuote> items;

    public long getTotal() {
        return total;
    }

    public void setTotal(final long total) {
        this.total = total;
    }

    public List<JsonQuote> getItems() {
        return items;
    }

    public void setItems(final List<JsonQuote> items) {
        this.items = items;
    }
}
