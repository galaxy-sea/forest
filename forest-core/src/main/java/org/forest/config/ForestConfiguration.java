/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Jun Gong
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.forest.config;


import org.forest.converter.ForestConverter;
import org.forest.converter.JSONConverterSelector;
import org.forest.converter.json.ForestJsonConverter;
import org.forest.converter.xml.ForestJaxbConverter;
import org.forest.converter.xml.ForestXmlConverter;
import org.forest.exceptions.ForestRuntimeException;
import org.forest.backend.HttpBackend;
import org.forest.backend.httpclient.HttpclientBackend;
import org.forest.filter.Filter;
import org.forest.filter.JSONFilter;
import org.forest.filter.XmlFilter;
import org.forest.ssl.SSLKeyStore;
import org.forest.utils.ForestDataType;
import org.forest.utils.RequestNameValue;
import org.forest.proxy.ProxyFactory;

import java.io.Serializable;
import java.util.*;

/**
 * global configuration
 * @author gongjun[dt_flys@hotmail.com]
 * @since 2016-03-24
 */
public class ForestConfiguration implements Serializable {

    private String id;

    /**
     * maximum number of conntections allowed
     */
    private Integer maxConnections;

    /**
     * maximum number of connections allowed per route
     */
    private Integer maxRouteConnections;

    /**
     * timeout in milliseconds
     */
    private Integer timeout;

    /**
     * connect timeout in milliseconds
     */
    private Integer connectTimeout;

    /**
     * count of retry times
     */
    private Integer retryCount;

    private HttpBackend backend;

    private List<RequestNameValue> defaultParameters;

    private List<RequestNameValue> defaultHeaders;

    private Map<ForestDataType, ForestConverter> converterMap;

    private JSONConverterSelector jsonConverterSelector;

    private Map<String, Class> filterRegisterMap = new HashMap<>();

    private Map<String, Object> variables = new HashMap<String, Object>();

    private Map<String, SSLKeyStore> sslKeyStores = new HashMap<>();

    public ForestConfiguration() {
    }

    public static ForestConfiguration configuration() {
        ForestConfiguration configuration = new ForestConfiguration();
        configuration.setId("forestConfiguration" + configuration.hashCode());
        configuration.setJsonConverterSelector(new JSONConverterSelector());
        configuration.setXmlConverter(new ForestJaxbConverter());
        setupJSONConverter(configuration);
        setupBackend(configuration);
        configuration.setTimeout(3000);
        configuration.setConnectTimeout(2000);
        configuration.setMaxConnections(500);
        configuration.setMaxRouteConnections(500);
        configuration.registerFilter("json", JSONFilter.class);
        configuration.registerFilter("xml", XmlFilter.class);
        return configuration;
    }

    private static void setupBackend(ForestConfiguration configuration) {
        configuration.backend = new HttpclientBackend(configuration);
    }

    private static void setupJSONConverter(ForestConfiguration configuration) {
        JSONConverterSelector jsonConverterSelector = new JSONConverterSelector();
        configuration.setJsonConverter(jsonConverterSelector.select());
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(Integer maxConnections) {
        this.maxConnections = maxConnections;
    }

    public Integer getMaxRouteConnections() {
        return maxRouteConnections;
    }

    public void setMaxRouteConnections(Integer maxRouteConnections) {
        this.maxRouteConnections = maxRouteConnections;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public List<RequestNameValue> getDefaultParameters() {
        return defaultParameters;
    }

    public void setDefaultParameters(List<RequestNameValue> defaultParameters) {
        this.defaultParameters = defaultParameters;
    }

    public List<RequestNameValue> getDefaultHeaders() {
        return defaultHeaders;
    }

    public void setDefaultHeaders(List<RequestNameValue> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
    }

    public void setJsonConverter(ForestJsonConverter converter) {
        getConverterMap().put(ForestDataType.JSON, converter);
    }

    public ForestJsonConverter getJsonConverter() {
        return (ForestJsonConverter) getConverterMap().get(ForestDataType.JSON);
    }

    public void setXmlConverter(ForestXmlConverter converter) {
        getConverterMap().put(ForestDataType.XML, converter);
    }

    public ForestXmlConverter getXmlConverter() {
        return (ForestXmlConverter) getConverterMap().get(ForestDataType.XML);
    }

    public <T> ProxyFactory<T> getProxyFactory(Class<T> clazz) {
        return new ProxyFactory<T>(this, clazz);
    }

    public HttpBackend getBackend() {
        return backend;
    }

    public void setVariableValue(String name, Object value) {
        getVariables().put(name, value);
    }

    public Object getVariableValue(String name) {
        return getVariables().get(name);
    }

    public Map<String, SSLKeyStore> getSslKeyStores() {
        return sslKeyStores;
    }

    public void setSslKeyStores(Map<String, SSLKeyStore> sslKeyStores) {
        this.sslKeyStores = sslKeyStores;
    }

    /**
     * register a SSL KeyStore object
     * @param id
     * @param keyStore
     */
    public void registerKeyStore(String id, SSLKeyStore keyStore) {
        sslKeyStores.put(id, keyStore);
    }

    public SSLKeyStore getKeyStore(String id) {
        return sslKeyStores.get(id);
    }

    public ForestConverter getConverter(ForestDataType dataType) {
        ForestConverter converter = getConverterMap().get(dataType);
        if (converter == null) {
            throw new ForestRuntimeException("Can not found converter for type " + dataType.name());
        }
        return converter;
    }

    public Map<ForestDataType, ForestConverter> getConverterMap() {
        if (converterMap == null) {
            converterMap = new HashMap<ForestDataType, ForestConverter>();
        }
        return converterMap;
    }

    public void setConverterMap(Map<ForestDataType, ForestConverter> converterMap) {
        this.converterMap = converterMap;
    }


    public Map<String, Object> getVariables() {
        return variables;
    }

    public void setVariables(Map<String, Object> variables) {
        this.variables = variables;
    }


    public <T> T createInstance(Class<T> clazz) {
        ProxyFactory<T> proxyFactory = getProxyFactory(clazz);
        return proxyFactory.createInstance();
    }

    public JSONConverterSelector getJsonConverterSelector() {
        return jsonConverterSelector;
    }

    public void setJsonConverterSelector(JSONConverterSelector jsonConverterSelector) {
        this.jsonConverterSelector = jsonConverterSelector;
    }



    public void registerFilter(String name, Class filterClass) {
        if (!(Filter.class.isAssignableFrom(filterClass))) {
            throw new ForestRuntimeException("Cannot register class \"" + filterClass.getName()
                    + "\" as a filter, filter class must implement Filter interface!");
        }
        if (filterRegisterMap.containsKey(name)) {
            throw new ForestRuntimeException("filter \"" + name + "\" already exists!");
        }
        filterRegisterMap.put(name, filterClass);
    }


    public List<String> getRegisteredFilterNames() {
        Set<String> nameSet = filterRegisterMap.keySet();
        List<String> names = new ArrayList<>();
        Iterator<String> iterator = nameSet.iterator();
        for (; iterator.hasNext(); ) {
            names.add(iterator.next());
        }
        return names;
    }

    public boolean hasFilter(String name) {
        return filterRegisterMap.containsKey(name);
    }

    public Filter newFilterInstance(String name) {
        Class filterClass = filterRegisterMap.get(name);
        if (filterClass == null) {
            throw new ForestRuntimeException("filter \"" + name + "\" does not exists!");
        }
        try {
            return (Filter) filterClass.newInstance();
        } catch (InstantiationException e) {
            throw new ForestRuntimeException("An error occurred the initialization of filter \"" + name + "\" ! cause: " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new ForestRuntimeException("An error occurred the initialization of filter \"" + name + "\" ! cause: " + e.getMessage(), e);
        }
    }

}
