package com.seven749.httpquesthelper;

import java.net.URL;
import java.util.Map;

public class Request {

    private String url;
    private String method;
    private Map<String, Object> hashMap;

    private Request(String url, String method, Map<String, Object> hashMap) {
        this.url = url;
        this.method = method;
        this.hashMap = hashMap;
    }

    public Map<String, Object> getHashMap() {
        return hashMap;
    }

    public String getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public static class Builder {
        private String url = null;
        private String method = "GET";
        private Map<String, Object> hashMap = null;

        private Builder() {}

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder hashMap(Map<String, Object> hashMap) {
            this.hashMap = hashMap;
            return this;
        }

        public Request build() {
            return new Request(url, method, hashMap);
        }
    }


}
