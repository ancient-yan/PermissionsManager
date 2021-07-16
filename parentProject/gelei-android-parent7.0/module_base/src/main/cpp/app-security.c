#include <jni.h>
#include <string.h>
#include <android/log.h>
#include "app-security.h"

#define LOG_TAG "C-Log"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

/*这是APP的正式签名*/
static char *SIGN = "3082021f30820188a00302010202044e2f8d6c300d06092a864886f70d010"
                    "10505003054310b300906035504061302636e310b300906035504081302666a310b"
                    "300906035504071302786d310d300b060355040a130474787477310d300b060355"
                    "040b130474787477310d300b0603550403130474787477301e170d3131303732373"
                    "034303034345a170d3334303732313034303034345a3054310b300906035504061"
                    "302636e310b300906035504081302666a310b300906035504071302786d310d300"
                    "b060355040a130474787477310d300b060355040b130474787477310d300b06035"
                    "5040313047478747730819f300d06092a864886f70d010101050003818d00308189"
                    "02818100894cb2979582ec23f0abe3406b5f08239c3d58e13135f437783d0fad74daa"
                    "c07ea6617675e383a1986e82b18b4177c6854ecc32b1256289e7d492bd139f6fbaaac7"
                    "f312f07aaebec7bd6c6371c9b5c9e8675ebd0ef65abb335ce56841cbf73772a7f005cb7"
                    "2b94f31993acd9be4875bc6c1d4da332a4ad4d39a454f60326d72d0203010001300d0"
                    "6092a864886f70d01010505000381810016ed322f5e8351432f1b4d211771fb3c6cab85"
                    "a48433e8d6e188c1438c1474f5171f3d2a639c358cd875bcf262892be8625515346425"
                    "6a212d3d4da58bb5fd6af2bf9ecca8dbd53fc07a38e87231e2c8b5fdbd1c2dac6f36f1ac5"
                    "2cb13053197780dbeb2a6bd56ef6e4c9129ff054f2e0a4b91b0a370696ec1f36fb7ee8f355c";

static jint GET_SIGNATURES = 64;

/**
 * 获取java层的Application对象
 */
static jobject getApplication(JNIEnv *env) {

    jobject application = NULL;
    jclass activity_thread_clz = (*env)->FindClass(env, "android/app/ActivityThread");

    if (activity_thread_clz != NULL) {

        jmethodID currentApplication = (*env)->GetStaticMethodID(
                env,
                activity_thread_clz,
                "currentApplication",
                "()Landroid/app/Application;");

        if (currentApplication != NULL) {
            application = (*env)->CallStaticObjectMethod(
                    env,
                    activity_thread_clz,
                    currentApplication);
        } else {
            LOGE("Cannot find method: currentApplication() in ActivityThread.");
        }
        (*env)->DeleteLocalRef(env, activity_thread_clz);
    } else {
        LOGE("Cannot find class: android.app.ActivityThread");
    }
    return application;
}


static int verifySign(JNIEnv *env) {
    // Application object
    jobject application = getApplication(env);
    if (application == NULL) {
        return JNI_ERR;
    }
    // Context(ContextWrapper) class
    jclass context_clz = (*env)->GetObjectClass(env, application);
    // getPackageManager()
    jmethodID getPackageManager = (*env)->GetMethodID(
            env,
            context_clz,
            "getPackageManager",
            "()Landroid/content/pm/PackageManager;");

    // android.content.pm.PackageManager object
    jobject package_manager = (*env)->CallObjectMethod(env, application, getPackageManager);
    // PackageManager class
    jclass package_manager_clz = (*env)->GetObjectClass(env, package_manager);
    // getPackageInfo()
    jmethodID getPackageInfo = (*env)->GetMethodID(env,
                                                   package_manager_clz,
                                                   "getPackageInfo",
                                                   "(Ljava/lang/String;I)Landroid/content/pm/PackageInfo;");
    // context.getPackageName()
    jmethodID getPackageName = (*env)->GetMethodID(env,
                                                   context_clz,
                                                   "getPackageName",
                                                   "()Ljava/lang/String;");

    // call getPackageName() and cast from jobject to jstring
    jstring package_name = (jstring) ((*env)->CallObjectMethod(env, application, getPackageName));

    // PackageInfo object
    jobject package_info = (*env)->CallObjectMethod(
            env,
            package_manager,
            getPackageInfo,
            package_name,
            GET_SIGNATURES);

    // class PackageInfo
    jclass package_info_clz = (*env)->GetObjectClass(env, package_info);

    // field signatures
    jfieldID signatures_field = (*env)->GetFieldID(
            env,
            package_info_clz,
            "signatures",
            "[Landroid/content/pm/Signature;");

    jobject signatures = (*env)->GetObjectField(env, package_info, signatures_field);
    jobjectArray signatures_array = (jobjectArray) signatures;
    jobject signature0 = (*env)->GetObjectArrayElement(env, signatures_array, 0);
    jclass signature_clz = (*env)->GetObjectClass(env, signature0);

    jmethodID toCharsString = (*env)->GetMethodID(
            env,
            signature_clz,
            "toCharsString",
            "()Ljava/lang/String;");

    // call toCharsString()
    jstring signature_str = (jstring) (*env)->CallObjectMethod(env, signature0, toCharsString);

    // release
    (*env)->DeleteLocalRef(env, application);
    (*env)->DeleteLocalRef(env, context_clz);
    (*env)->DeleteLocalRef(env, package_manager);
    (*env)->DeleteLocalRef(env, package_manager_clz);
    (*env)->DeleteLocalRef(env, package_name);
    (*env)->DeleteLocalRef(env, package_info);
    (*env)->DeleteLocalRef(env, package_info_clz);
    (*env)->DeleteLocalRef(env, signatures);
    (*env)->DeleteLocalRef(env, signature0);
    (*env)->DeleteLocalRef(env, signature_clz);

    const char *sign = (*env)->GetStringUTFChars(env, signature_str, NULL);
    if (sign == NULL) {
        LOGE("分配内存失败");
        return JNI_ERR;
    }

    LOGI("应用中读取到的签名首字母为：%c", *sign);
    //LOGI("应用中读取到的签名为：%s", sign);
    LOGI("native中预置的签名首字母为：%c", *SIGN);
    //LOGI("native中预置的签名为：%s", SIGN);

    int result = strcmp(sign, SIGN);
    // 使用之后要释放这段内存
    (*env)->ReleaseStringUTFChars(env, signature_str, sign);
    (*env)->DeleteLocalRef(env, signature_str);
    // 签名一致
    if (result == 0) {
        return JNI_OK;
    }
    return JNI_ERR;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOGI("验证APP签名----------------->");
    JNIEnv *env = NULL;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_4) != JNI_OK) {
        return JNI_ERR;
    }
    if (verifySign(env) == JNI_OK) {
        LOGI("APP签名验证通过<-----------------");
        return JNI_VERSION_1_4;
    }
    LOGE("APP签名不一致<-----------------");
    return JNI_ERR;
}

static char *APP_TOKEN = "27688ab70a56db714b59a6ebc79b8509a1f81629ce8edc743e1bc23e24465735";
static char *APP_ID = "437EC0AC7F0000015E2BBF4849643C96";

JNIEXPORT jstring JNICALL Java_com_gwchina_sdk_base_app_AppSecurity_getAppToken
        (JNIEnv *env, jclass clazz) {
    char appToken[] = {'8','r','W','S','1','E','G','f','6','L','i','1','R','D','U','N','B','q','l','u','t','o','k','F','A','1','J','2','T','G','e','W','9','H','Y','w','C','i','/','c','E','+','T','O','x','7','a','Q','F','C','s','I','z','D','a','M','f','d','b','t','1','q','+','V','7','B','2','k','p','o','G','U','1','9','5','z','S','2','P','g','D','2','Y','A','E','L','k','v','2','5','W','f','s','x','U','S','\0'};
    return (*env)->NewStringUTF(env, appToken);
}

JNIEXPORT jstring JNICALL Java_com_gwchina_sdk_base_app_AppSecurity_getAppId
        (JNIEnv *env, jclass clazz) {
    char appId[] = {'T','0','O','u','2','g','j','j','U','D','l','Q','y','t','V','0','M','X','t','8','x','0','Z','i','0','+','n','k','F','m','V','q','r','c','4','E','n','A','x','O','C','n','y','5','L','9','u','V','n','7','M','V','E','g','=','=','\0'};
    return (*env)->NewStringUTF(env, appId);
}