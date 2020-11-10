package com.payline.payment.oney.utils.http;

import com.payline.payment.oney.exception.HttpCallException;
import com.payline.pmapi.bean.configuration.PartnerConfiguration;
import com.payline.pmapi.logger.LogManager;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

import static com.payline.payment.oney.utils.OneyConstants.*;

/**
 * This utility class provides a basic HTTP client to send requests, using OkHttp library.
 * It must be extended to match each payment method needs.
 */
public abstract class AbstractHttpClient {

    private CloseableHttpClient client;
    private static final Logger LOGGER = LogManager.getLogger(AbstractHttpClient.class);
    public static final String KEY_CONNECT_TIMEOUT = "connect.time.out";
    public static final String CONNECTION_REQUEST_TIMEOUT = "connect.request.time.out";
    public static final String READ_SOCKET_TIMEOUT = "read.time.out";
    public static final String KEEP_ALIVE_DURATION = "keep.alive.duration";
    public static final String POOL_VALIDATE_CONN_AFTER_INACTIVITY = "pool.validate.connection.after.inactivity";
    public static final String POOL_MAX_SIZE_PER_ROUTE = "pool.max.size.per.route";
    public static final String EVICT_IDLE_CONNECTION_TIMEOUT = "evict.idle.connection.timeout";
    public static final String CONNECTION_TIME_TO_LIVE = "connection.time.to.live";

    /**
     * Instantiate a HTTP client.
     */

    protected AbstractHttpClient(final PartnerConfiguration partnerConfiguration) {

        final RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(Integer.parseInt(partnerConfiguration.getProperty(KEY_CONNECT_TIMEOUT)))
                .setConnectionRequestTimeout(Integer.parseInt(partnerConfiguration.getProperty(CONNECTION_REQUEST_TIMEOUT)))
                .setSocketTimeout(Integer.parseInt(partnerConfiguration.getProperty(READ_SOCKET_TIMEOUT))).build();


        final HttpClientBuilder builder = getHttpClientBuilder(partnerConfiguration, requestConfig);
        this.client = builder.build();
    }


    protected HttpClientBuilder getHttpClientBuilder(final PartnerConfiguration partnerConfiguration, final RequestConfig requestConfig) {
        final HttpClientBuilder builder = HttpClientBuilder.create();
        builder.useSystemProperties()
                .setDefaultRequestConfig(requestConfig)
                .setDefaultCredentialsProvider(new BasicCredentialsProvider())
                .setSSLSocketFactory(new SSLConnectionSocketFactory(HttpsURLConnection.getDefaultSSLSocketFactory(), SSLConnectionSocketFactory.getDefaultHostnameVerifier()));

        final String inactivityConnection = partnerConfiguration.getProperty(POOL_VALIDATE_CONN_AFTER_INACTIVITY);
        final String maxSizePerRoute = partnerConfiguration.getProperty(POOL_MAX_SIZE_PER_ROUTE);

        boolean hasInactivityConnexion = inactivityConnection != null && inactivityConnection.length() > 0;
        boolean hasMaxPoolSizePerRoute = maxSizePerRoute != null && maxSizePerRoute.length() > 0;

        // Si des paramètres concernant le pool ont été changé on définit
        // un nouveau pool de connection.
        if (hasInactivityConnexion || hasMaxPoolSizePerRoute) {
            final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
            if (hasInactivityConnexion) {
                connManager.setValidateAfterInactivity(Integer.parseInt(inactivityConnection));
            }
            if (hasMaxPoolSizePerRoute) {
                connManager.setDefaultMaxPerRoute(Integer.parseInt(maxSizePerRoute));
            }
            builder.setConnectionManager(connManager);
        }

        final String keepAliveStrategy = partnerConfiguration.getProperty(KEEP_ALIVE_DURATION);
        if (keepAliveStrategy != null && keepAliveStrategy.length() > 0) {
            builder.setKeepAliveStrategy((response, context) -> Long.parseLong(keepAliveStrategy));
        }

        final String evictIdleConnection = partnerConfiguration.getProperty(EVICT_IDLE_CONNECTION_TIMEOUT);
        if (evictIdleConnection != null && evictIdleConnection.length() > 0) {
            builder.evictIdleConnections(Long.parseLong(evictIdleConnection), TimeUnit.MILLISECONDS);
        }

        final String connectionTimeToLive = partnerConfiguration.getProperty(CONNECTION_TIME_TO_LIVE);
        if (connectionTimeToLive != null && connectionTimeToLive.length() > 0){
            builder.setConnectionTimeToLive(Long.parseLong(connectionTimeToLive), TimeUnit.MILLISECONDS);
        }
        return builder;
    }


    /**
     * Send a POST request.
     *
     * @param url  URL scheme + host
     * @param path URL path
     * @param body Request body
     * @return The response returned from the HTTP call
     * @throws HttpCallException COMMUNICATION_ERROR
     */
    protected StringResponse doPost(String url, String path, Header[] headers, HttpEntity body) throws HttpCallException {
        final String methodName = "doPost";

        try {
            URI uri = new URI(url + path);

            final HttpPost httpPostRequest = new HttpPost(uri);
            httpPostRequest.setHeaders(headers);
            httpPostRequest.setEntity(body);

            return getStringResponse(url, methodName, httpPostRequest);

        } catch (URISyntaxException e) {
            LOGGER.error(e.getMessage(), e);
            throw new HttpCallException(e, "AbstractHttpClient.doPost.URISyntaxException");
        }


    }

    private StringResponse getStringResponse(String url, String methodName, HttpRequestBase httpPostRequest) throws HttpCallException {
        final long start = System.currentTimeMillis();
        int count = 0;
        StringResponse strResponse = null;
        String errMsg = null;
        while (count < 3 && strResponse == null) {
            try (CloseableHttpResponse httpResponse = this.client.execute(httpPostRequest)) {

                LOGGER.info("Start partner call... [URL: {}]", url);

                strResponse = new StringResponse();
                strResponse.setCode(httpResponse.getStatusLine().getStatusCode());
                strResponse.setMessage(httpResponse.getStatusLine().getReasonPhrase());

                if (httpResponse.getEntity() != null) {
                    final String responseAsString = EntityUtils.toString(httpResponse.getEntity());
                    strResponse.setContent(responseAsString);
                }
                final long end = System.currentTimeMillis();

                LOGGER.info("End partner call [T: {}ms] [CODE: {}]", end - start, strResponse.getCode());

            } catch (final IOException e) {
                LOGGER.error("Error while partner call [T: {}ms]", System.currentTimeMillis() - start, e);
                strResponse = null;
                errMsg = e.getMessage();
            } finally {
                count++;
            }
        }

        if (strResponse == null) {
            if (errMsg == null) {
                throw new HttpCallException("Http response is empty", "AbstractHttpClient." + methodName + " : empty partner response");
            }
            throw new HttpCallException(errMsg, "AbstractHttpClient." + methodName + ".IOException");
        }
        return strResponse;
    }


    /**
     * Send a GET request
     *
     * @param url  URL RL scheme + host
     * @param path URL path
     * @return The response returned from the HTTP call
     * @throws HttpCallException COMMUNICATION_ERROR
     */

    protected StringResponse doGet(String url, String path, Header[] headers) throws HttpCallException {
        final String methodName = "doGet";
        try {
            URI uri = new URI(url + path);

            final HttpGet httpGetRequest = new HttpGet(uri);
            httpGetRequest.setHeaders(headers);

            return getStringResponse(url, methodName, httpGetRequest);
        } catch (URISyntaxException e) {
            throw new HttpCallException(e, "AbstractHttpClient.doGet.URISyntaxException");
        }


    }


}
