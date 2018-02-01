package com.github.rmannibucau.quote.manager.service;

import com.github.rmannibucau.quote.manager.model.Quote;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_OCTET_STREAM_TYPE;

@ApplicationScoped
public class ProvisioningService {
    private static final Logger LOGGER = Logger.getLogger(ProvisioningService.class.getName());

    @Inject
    private QuoteService quoteService;

    private final String symbolIndex = System.getProperty("app.data.symbols.url", "http://www.cboe.com/publish/ScheduledTask/MktData/cboesymboldir2.csv");
    private final String financialData = System.getProperty("app.data.financial.url", "https://query1.finance.yahoo.com/v10/finance/quoteSummary/{symbol}?modules=financialData");
    private final int maxInitQuotes = Integer.getInteger("app.data.intiial.size", 10/*Integer.MAX_VALUE*/);

    // can use JBatch but to keep things simple to get some data at startup just do it with plain java
    public void refresh() {
        final Client client = ClientBuilder.newClient();
        try {
            final String[] symbols = getSymbols(client);
            LOGGER.info("Updated " + Stream.of(symbols).limit(maxInitQuotes).peek(symbol -> {
                try {
                    final Data data = client.target(financialData)
                            .resolveTemplate("symbol", symbol)
                            .request(APPLICATION_JSON_TYPE)
                            .get(Data.class);

                    if (data.getQuoteSummary() == null ||
                            data.getQuoteSummary().getError() != null ||
                            data.getQuoteSummary().getResult() == null ||
                            data.getQuoteSummary().getResult().isEmpty() ||
                            data.getQuoteSummary().getResult().get(0).getFinancialData() == null ||
                            data.getQuoteSummary().getResult().get(0).getFinancialData().getCurrentPrice() == null) {

                        LOGGER.info("Can't retrieve '" + symbol + "' value." +
                                ((data.getQuoteSummary() != null && data.getQuoteSummary().getError() != null) ?
                                        " Error: " + data.getQuoteSummary().getError().getDescription() : ""));
                        return;
                    }

                    final double value = data.getQuoteSummary().getResult().get(0).getFinancialData().getCurrentPrice().getRaw();

                    final Quote quote = quoteService.mutate(symbol, quoteOrEmpty ->
                            quoteOrEmpty.map(q -> {
                                q.setValue(value);
                                return q;
                            }).orElseGet(() -> {
                                final Quote newQuote = new Quote();
                                newQuote.setName(symbol);
                                newQuote.setValue(value);
                                quoteService.create(newQuote);
                                return newQuote;
                            }));

                    LOGGER.info("Updated quote '" + quote.getName() + "' to value " + quote.getValue() + " (id=" + quote.getId() + ")");
                } catch (final WebApplicationException error) {
                    LOGGER.info("Error getting '" + symbol + "': " + error.getMessage() +
                            " (HTTP " + (error.getResponse() == null ? "-" : error.getResponse().getStatus()) + ")");
                }
            }).count() + " quotes");
        } finally {
            client.close();
        }
    }

    private String[] getSymbols(final Client client) {
        try (final BufferedReader stream = new BufferedReader(
                new InputStreamReader(
                        client.target(symbolIndex)
                                .request(APPLICATION_OCTET_STREAM_TYPE)
                                .get(InputStream.class),
                        StandardCharsets.UTF_8))) {

            return stream.lines().skip(2/*comment+header*/)
                    .map(line -> line.split(","))
                    .filter(columns -> columns.length > 2 && !columns[1].isEmpty())
                    .map(columns -> columns[1])
                    .toArray(String[]::new);
        } catch (final IOException e) {
            throw new IllegalArgumentException("Can't connect to find symbols", e);
        }
    }

    public static class Data {
        private QuoteSummary quoteSummary;

        public QuoteSummary getQuoteSummary() {
            return quoteSummary;
        }

        public void setQuoteSummary(final QuoteSummary quoteSummary) {
            this.quoteSummary = quoteSummary;
        }
    }

    public static class Error {
        private String code;
        private String description;

        public String getCode() {
            return code;
        }

        public void setCode(final String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(final String description) {
            this.description = description;
        }
    }

    public static class QuoteSummary {
        private Error error;
        private List<Result> result;

        public Error getError() {
            return error;
        }

        public void setError(final Error error) {
            this.error = error;
        }

        public List<Result> getResult() {
            return result;
        }

        public void setResult(final List<Result> result) {
            this.result = result;
        }
    }

    public static class Result {
        private FinancialData financialData;

        public FinancialData getFinancialData() {
            return financialData;
        }

        public void setFinancialData(final FinancialData financialData) {
            this.financialData = financialData;
        }
    }

    public static class FinancialData {
        private Price currentPrice;

        public Price getCurrentPrice() {
            return currentPrice;
        }

        public void setCurrentPrice(final Price currentPrice) {
            this.currentPrice = currentPrice;
        }
    }

    public static class Price {
        private double raw;

        public double getRaw() {
            return raw;
        }

        public void setRaw(final double raw) {
            this.raw = raw;
        }
    }
}
