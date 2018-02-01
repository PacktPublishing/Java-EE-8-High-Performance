package com.github.rmannibucau.quote.manager.socket;

import com.github.rmannibucau.quote.manager.model.Quote;
import com.github.rmannibucau.quote.manager.service.QuoteService;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.json.bind.Jsonb;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.Optional;

@Dependent
@ServerEndpoint(value = "/quote", decoders = DirectQuoteSocket.RequestDecoder.class, encoders = DirectQuoteSocket.JsonEncoder.class)
public class DirectQuoteSocket {
    @Inject
    private QuoteService quoteService;

    @OnMessage
    public void onMessage(final Session session, final ValueRequest request) {
        final Optional<Quote> quote = quoteService.findByName(request.getName());
        final ValueResponse response = new ValueResponse();
        if (quote.isPresent()) {
            response.setFound(true);
            response.setValue(quote.get().getValue()); // false
        }

        if (session.isOpen()) {
            try {
                session.getBasicRemote().sendObject(response);
            } catch (final EncodeException | IOException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    public static class ValueRequest {
        private String name;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

    public static class ValueResponse {
        private double value;
        private boolean found;

        public boolean isFound() {
            return found;
        }

        public void setFound(final boolean found) {
            this.found = found;
        }

        public double getValue() {
            return value;
        }

        public void setValue(final double value) {
            this.value = value;
        }
    }

    @Dependent
    public static class JsonEncoder implements Encoder.TextStream<Object> {
        @Inject
        private Jsonb jsonb;

        @Override
        public void init(final EndpointConfig endpointConfig) {
            // no-op
        }

        @Override
        public void encode(final Object o, final Writer writer) throws EncodeException, IOException {
            jsonb.toJson(o, writer);
        }

        @Override
        public void destroy() {
            // no-op
        }
    }

    @Dependent
    public static class RequestDecoder implements Decoder.TextStream<ValueRequest> {
        @Inject
        private Jsonb jsonb;

        @Override
        public void init(final EndpointConfig endpointConfig) {
            // no-op
        }

        @Override
        public ValueRequest decode(final Reader reader) throws DecodeException, IOException {
            return jsonb.fromJson(reader, ValueRequest.class);
        }

        @Override
        public void destroy() {
            // no-op
        }
    }
}
