package com.gwchina.sdk.base.data.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import timber.log.Timber;

final public class SignTools {

    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static char[] encodeHex(final byte[] data) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = DIGITS_LOWER[(0xF0 & data[i]) >>> 4];
            out[j++] = DIGITS_LOWER[0x0F & data[i]];
        }
        return out;
    }

    private static String encodeHexString(final byte[] data) {
        return new String(encodeHex(data));
    }

    static String genSign(JsonObject params, String timeStamp, String token) {
        Timber.d("genSign() called with: params = [" + params + "], timeStamp = [" + timeStamp + "], token = [" + token + "]");
        //step1 接口所有参数值、当前13位时间戳、token按照字典序排序后进行字符串拼接获得字符串A
        List<String> values = new ArrayList<>();
        values.add(timeStamp);
        values.add(token);
        addParamsToList(params, values);

        String[] arr = values.toArray(new String[0]);
        Arrays.sort(arr);

        Timber.d("sorted params = %s", Arrays.toString(arr));
        StringBuilder content = new StringBuilder();
        for (String anArr : arr) {
            content.append(anArr);
        }

        String sign = sha256(content.toString());
        return timeStamp + sign;
    }

    private static String sha256(String string) {
        return sha256(string.getBytes(StandardCharsets.UTF_8));
    }

    static String sha256(byte[] bytes) {
        //step2 对步骤一生成的字符串A进行SHA256加密生成字符串B
        String result = "";
        try {
            final String algorithm = "SHA256";
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(bytes);
            result = encodeHexString(md.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        //step3 将步骤一生成的13位时间戳拼接字符串B生成签名
        return result;
    }

    private static void addParamsToList(JsonObject params, List<String> values) {
        if (params != null) {
            for (Map.Entry<String, JsonElement> entry : params.entrySet()) {
                JsonElement value = entry.getValue();
                if (value.isJsonObject()) {
                    values.add(value.getAsJsonObject().toString());
                } else if (value.isJsonArray()) {
                    values.add(value.getAsJsonArray().toString());
                } else {
                    values.add(value.getAsString());
                }
            }
        }
    }

}