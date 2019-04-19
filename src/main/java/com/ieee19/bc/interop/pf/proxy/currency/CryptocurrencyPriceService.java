package com.ieee19.bc.interop.pf.proxy.currency;

import com.ieee19.bc.interop.pf.proxy.currency.interfaces.ICryptocurrencyPriceService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;


/**
 * This class interacts with an external service for retrieving currency exchange rates.
 */
public class CryptocurrencyPriceService implements ICryptocurrencyPriceService {

    private static final String BASE_URL = "https://min-api.cryptocompare.com/data/price";

    static final Logger LOG = LoggerFactory.getLogger(CryptocurrencyPriceService.class);

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public BigDecimal getPrice(Currency from, Currency to) throws CurrencyServiceException {
        LOG.debug("Get price in " + to + " for " + from);
        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUri(new URI(BASE_URL))
                    .queryParam("fsym", from.getLabel())
                    .queryParam("tsyms", to.getLabel());
            String response = restTemplate.getForObject(uriBuilder.toUriString(), String.class);
            LOG.debug("Response: " + response);
            ObjectMapper mapper = new ObjectMapper();
            mapper.enable(DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

            Map<String, BigDecimal> price = mapper.readValue(response, new TypeReference<Map<String, BigDecimal>>(){});
            return price.get(to.getLabel());
        } catch (RestClientException | URISyntaxException | IOException e) {
            LOG.error(e.getMessage(), e);
            throw new CurrencyServiceException(e.getMessage(), e);
        }
    }

}
