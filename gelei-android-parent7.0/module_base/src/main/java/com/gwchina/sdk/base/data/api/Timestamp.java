package com.gwchina.sdk.base.data.api;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2019-01-08 11:57
 */
class Timestamp {

    static long getTimestamp() {
        return System.currentTimeMillis() + 259200000;
    }

}
