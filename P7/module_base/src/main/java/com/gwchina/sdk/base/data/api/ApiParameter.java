package com.gwchina.sdk.base.data.api;

import android.text.TextUtils;

import com.android.base.utils.common.StringChecker;
import com.android.base.utils.security.DESCipherStrategy;
import com.blankj.utilcode.util.FileIOUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.gwchina.sdk.base.AppContext;
import com.gwchina.sdk.base.app.AppSecurity;
import com.gwchina.sdk.base.data.DataContext;
import com.gwchina.sdk.base.data.models.Child;
import com.gwchina.sdk.base.data.models.Device;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import timber.log.Timber;

/**
 * @author Ztiany
 * Email: ztiany3@gmail.com
 * Date : 2018-11-13 11:16
 */
public class ApiParameter {

    public static void init() {
        String desKey = "a1f81629ce8edc743e1bc23e24465735";
        DESCipherStrategy desCipherStrategy = new DESCipherStrategy(desKey);
        TOKEN_VALUE = desCipherStrategy.decrypt(AppSecurity.getAppToken());
        GNW_APP_ID_VALUE = desCipherStrategy.decrypt(AppSecurity.getAppId());
    }

    private static String TOKEN_VALUE = "";

    final static String SIGN_KEY = "sign";
    final static String APP_TOKEN_KEY = "app_token";
    final static String DEVICE_ID_KEY = "device_id";

    private final static String GNW_APP_ID_KEY = "gnw_appid";
    private static String GNW_APP_ID_VALUE = "";

    private final static String API_VERSION_KEY = "version";
    private final static String API_VERSION_VALUE = "3.0.2";//接口版本

    private final static String SOURCE_KEY = "source";
    private final static String SOURCE_VALUE = "01";//01-格雷守护家长端

    static final String DEVICE_ID_HEADER = "need_parent_device_id";
    public static final String WITH_DEVICE_ID = DEVICE_ID_HEADER + ":true";

    static final String APP_TOKEN_HEADER = "need_parent_app_token";
    public static final String WITH_APP_TOKEN = APP_TOKEN_HEADER + ":true";

    static boolean isHeaderValueTrue(String value) {
        Timber.d("isHeaderValueTrue() called with: value = [" + value + "]");
        return "true".equals(value);
    }

    static String token() {
        return TOKEN_VALUE;
    }

    /**
     * 生成公共的参数，以Map<String, String> 的形式返回
     *
     * @return 公共的参数
     */
    static JsonObject generateCommonParamsJson() {
        checkInitialize();
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(GNW_APP_ID_KEY, GNW_APP_ID_VALUE);
        jsonObject.addProperty(API_VERSION_KEY, API_VERSION_VALUE);
        jsonObject.addProperty(SOURCE_KEY, SOURCE_VALUE);
        return jsonObject;
    }

    private static void checkInitialize() {
        if (StringChecker.isEmpty(TOKEN_VALUE) || StringChecker.isEmpty(GNW_APP_ID_VALUE)) {
            throw new IllegalStateException("ApiParameter has not initialize");
        }
    }

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    static RequestBody createJsonBody(String json) {
        return RequestBody.create(JSON, json);
    }

    /**
     * for h5
     */
    public static String genSignAndGenerateRequestParams(String json, Boolean isNeedChildDeviceId, Boolean isNeedToken, Boolean isNeedChildId) {
        JsonObject jsonObject = generateCommonParamsJson();
        /*添加app token*/
        if (isNeedToken) {
            String appToken = DataContext.getInstance().getAppToken();
            jsonObject.addProperty(ApiParameter.APP_TOKEN_KEY, appToken == null ? "" : appToken);
        }
        /*添加deviceId*/
        if (isNeedChildDeviceId) {
            Device device = AppContext.appDataSource().user().getCurrentDevice();
            String childDeviceId = device != null ? device.getDevice_id() : "";
            jsonObject.addProperty("child_device_id", childDeviceId);
        }
        /*添加 childUserId*/
        if (isNeedChildId) {
            Child currentChild = AppContext.appDataSource().user().getCurrentChild();
            String childUserId = currentChild != null ? currentChild.getChild_user_id() : "";
            jsonObject.addProperty("child_user_id", childUserId);
        }
        /*添加参数*/
        if (!TextUtils.isEmpty(json) && (json.startsWith("{") && json.endsWith("}"))) {
            try {
                JsonElement parse = new JsonParser().parse(json);
                if (parse.isJsonObject()) {
                    JsonObject obj = parse.getAsJsonObject();
                    for (String key : obj.keySet()) {
                        JsonElement element = obj.get(key);
                        if (element.isJsonObject() || element.isJsonArray()) {
                            jsonObject.add(key, element);
                        } else {
                            jsonObject.addProperty(key, element.getAsString());
                        }
                    }
                }
            } catch (JsonSyntaxException e) {
                Timber.e(e, "parse json error");
            }
        }
        String sign = SignTools.genSign(jsonObject, String.valueOf(Timestamp.getTimestamp()), ApiParameter.token());
        jsonObject.addProperty(ApiParameter.SIGN_KEY, sign);
        return jsonObject.toString();
    }

    /**
     * 生成表达请求体，已经添加了默认参数并且已经签名。
     */
    public static Map<String, RequestBody> buildMultiPartRequestBody(Map<String, String> fieldParts, Map<String, File> fileParts) {
        return buildMultiPartRequestBody(fieldParts, fileParts, false, false);
    }

    /**
     * 生成表达请求体，已经添加了默认参数并且已经签名。
     */
    @SuppressWarnings("WeakerAccess")
    public static Map<String, RequestBody> buildMultiPartRequestBody(Map<String, String> fieldParts, Map<String, File> fileParts, boolean needToken, boolean needDeviceId) {
        checkInitialize();

        Map<String, String> forSign = new LinkedHashMap<>();

        forSign.put(GNW_APP_ID_KEY, GNW_APP_ID_VALUE);
        forSign.put(API_VERSION_KEY, API_VERSION_VALUE);

        for (Map.Entry<String, String> entry : fieldParts.entrySet()) {
            forSign.put(entry.getKey(), entry.getValue());
        }

        /*添加app token*/
        if (needToken) {
            String appToken = DataContext.getInstance().getAppToken();
            if (!StringChecker.isEmpty(appToken)) {
                forSign.put(APP_TOKEN_KEY, appToken);
            }
        }

        /*添加deviceId*/
        if (needDeviceId) {
            String deviceId = DataContext.getInstance().getDeviceId();
            if (!StringChecker.isEmpty(deviceId)) {
                forSign.put(DEVICE_ID_KEY, deviceId);
            }
        }

        JsonObject jsonObject = new JsonObject();
        for (String key : forSign.keySet()) {
            jsonObject.addProperty(key, forSign.get(key));
        }

        String content = SignTools.genSign(jsonObject, String.valueOf(Timestamp.getTimestamp()), ApiParameter.token());

        Map<String, RequestBody> params = new LinkedHashMap<>();
        params.put(SIGN_KEY, RequestBody.create(null, content));

        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            JsonElement value = entry.getValue();
            if (value.isJsonObject()) {
                params.put(entry.getKey(), RequestBody.create(null, value.getAsJsonObject().toString()));
            } else if (value.isJsonArray()) {
                params.put(entry.getKey(), RequestBody.create(null, value.getAsJsonArray().toString()));
            } else {
                params.put(entry.getKey(), RequestBody.create(null, value.getAsString()));
            }
        }

        for (Map.Entry<String, File> entry : fileParts.entrySet()) {
            params.put(entry.getKey() + "\"; filename=\"" + entry.getValue().getName() + "", RequestBody.create(MediaType.parse("application/octet-stream"), entry.getValue()));
        }

        return params;
    }

    public static String fileHash(File file) {
        return SignTools.sha256(FileIOUtils.readFile2BytesByStream(file));
    }

}