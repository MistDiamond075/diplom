package com.diplom.diplom.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConfUrls {
    @Value("${url.http.janus}")
    public String httpJanus;

    @Value("${url.https.janus}")
    public String httpsJanus;

    @Value("${url.ws.janus}")
    public String wsJanus;

    @Value("${url.ws.port}")
    public int wsPort;

    public String getHttpJanus() {
        return httpJanus;
    }

    public void setHttpJanus(String httpJanus) {
        this.httpJanus = httpJanus;
    }

    public String getHttpsJanus() {
        return httpsJanus;
    }

    public void setHttpsJanus(String httpsJanus) {
        this.httpsJanus = httpsJanus;
    }

    public String getWsJanus() {
        return wsJanus;
    }

    public void setWsJanus(String wsJanus) {
        this.wsJanus = wsJanus;
    }

    public int getWsPort() {
        return wsPort;
    }

    public void setWsPort(int wsPort) {
        this.wsPort = wsPort;
    }
}
