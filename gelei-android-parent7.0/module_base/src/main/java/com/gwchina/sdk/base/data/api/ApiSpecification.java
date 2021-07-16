package com.gwchina.sdk.base.data.api;

import android.support.annotation.NonNull;

import com.android.base.utils.common.StringChecker;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.gwchina.sdk.base.data.DataContext;

import java.io.IOException;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.Interceptor;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-13 11:21
 */
public class ApiSpecification {

    public static void configHttp(OkHttpClient.Builder builder) {
        builder.addInterceptor(new GwApiInterceptor());
    }

    private static class GwApiInterceptor implements Interceptor {

        private final JsonParser mJsonParser = new JsonParser();

        @NonNull
        @Override
        public Response intercept(@NonNull Chain chain) throws IOException {
            Request request = chain.request();
            RequestBody body = request.body();
            if (body instanceof MultipartBody) {
                Timber.d("拦截器不处理 multi-part");
                return chain.proceed(request);
            } else {
                Request.Builder newBuilder = request.newBuilder();
                //Header
                newBuilder.addHeader("Content-Type", "application/json; charset=utf-8");
                //Body
                processBody(request, newBuilder, body);
                //Send request
                return chain.proceed(newBuilder.build());
            }
        }

        private void processBody(Request request, Request.Builder builder, RequestBody body) {
            JsonObject jsonObject = ApiParameter.generateCommonParamsJson();

            if (body instanceof FormBody) {
                Timber.d("请求体是表单--->");
                processFormBody(jsonObject, (FormBody) body);
            }

            /*添加app token*/
            if (ApiParameter.isHeaderValueTrue(request.header(ApiParameter.APP_TOKEN_HEADER))) {
                String appToken = DataContext.getInstance().getAppToken();
                if (!StringChecker.isEmpty(appToken)) {
                    jsonObject.addProperty(ApiParameter.APP_TOKEN_KEY, appToken);
                }
            }
            /*添加deviceId*/
            if (ApiParameter.isHeaderValueTrue(request.header(ApiParameter.DEVICE_ID_HEADER))) {
                String deviceId = DataContext.getInstance().getDeviceId();
                if (!StringChecker.isEmpty(deviceId)) {
                    jsonObject.addProperty(ApiParameter.DEVICE_ID_KEY, deviceId);
                }
            }

            long time = Timestamp.getTimestamp();
            String sign = SignTools.genSign(jsonObject, String.valueOf(time), ApiParameter.token());
            jsonObject.addProperty(ApiParameter.SIGN_KEY, sign);

            String json = jsonObject.toString();
            RequestBody requestBody = ApiParameter.createJsonBody(json);
            builder.post(requestBody);

            Timber.d("请求参数---------------------------------start");
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                Timber.d("key = %-20s    value = %s", entry.getKey(), entry.getValue());
            }
            Timber.d("json = %-20s", json);
            Timber.d("请求参数---------------------------------end");
        }

        private void processFormBody(JsonObject jsonObject, FormBody body) {
            for (int i = 0; i < body.size(); i++) {
                String value = body.value(i);
                //是对象或数组
                if ((value.startsWith("{") && value.endsWith("}")) || (value.startsWith("[")) && value.endsWith("]")) {
                    jsonObject.add(body.name(i), mJsonParser.parse(value));
                }
                //是字符串
                else {
                    jsonObject.addProperty(body.name(i), value);
                }
            }
        }

    }

}