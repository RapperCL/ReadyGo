package com.cy.readygo.core.socket;

import java.io.IOException;
import java.net.Socket;

public interface RedisXSocketFactory {

        Socket createSocket() throws IOException;

        String getDescription();

        String getHost();

        void setHost(String var1);

        int getPort();

        void setPort(int var1);

        int getConnectionTimeout();

        void setConnectionTimeout(int var1);

        int getSoTimeout();

        void setSoTimeout(int var1);

}
