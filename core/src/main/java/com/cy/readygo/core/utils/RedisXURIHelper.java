package com.cy.readygo.core.utils;

import java.net.URI;

public final class RedisXURIHelper {

        private static final int DEFAULT_DB = 0;
        private static final String REDIS = "redis";
        private static final String REDISS = "rediss";

        private RedisXURIHelper() {
            throw new InstantiationError("Must not instantiate this class");
        }

        public static String getUser(URI uri) {
            String userInfo = uri.getUserInfo();
            if (userInfo != null) {
                String user = userInfo.split(":", 2)[0];
                if (user.isEmpty()) {
                    user = null;
                }

                return user;
            } else {
                return null;
            }
        }

        public static String getPassword(URI uri) {
            String userInfo = uri.getUserInfo();
            return userInfo != null ? userInfo.split(":", 2)[1] : null;
        }

        public static int getDBIndex(URI uri) {
            String[] pathSplit = uri.getPath().split("/", 2);
            if (pathSplit.length > 1) {
                String dbIndexStr = pathSplit[1];
                return dbIndexStr.isEmpty() ? 0 : Integer.parseInt(dbIndexStr);
            } else {
                return 0;
            }
        }

        public static boolean isValid(URI uri) {
            return !isEmpty(uri.getScheme()) && !isEmpty(uri.getHost()) && uri.getPort() != -1;
        }

        private static boolean isEmpty(String value) {
            return value == null || value.trim().length() == 0;
        }

        public static boolean isRedisScheme(URI uri) {
            return "redis".equals(uri.getScheme());
        }

        public static boolean isRedisSSLScheme(URI uri) {
            return "rediss".equals(uri.getScheme());
        }


}
