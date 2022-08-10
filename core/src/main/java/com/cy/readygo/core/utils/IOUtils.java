package com.cy.readygo.core.utils;

import java.io.IOException;
import java.net.Socket;

public class IOUtils {
    private IOUtils() {
    }

    public static void closeQuietly(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException var2) {
            }
        }

    }
}