# 绿网-家长端

- 每个模块都有 README 文件，修改代码前请务必先阅读模块相关 README 文档。
- 其他文档统一放在 [doc](doc) 中。

## 项目配置

```shell
# 获取公共依赖库
git clone git@172.168.50.230:lwtx/gelei-android-base.git

# 修改仓库名称
mv gelei-android-libs Gw-Libs
# 拉取定制机家长端公版代码
git clone git@172.168.50.230:lwtx/gelei-android-parent7.0.git
```

AndroidStudio 配置：

1. 关闭 Instant Run（必须）
2. Language & Frameworks 中将 kotlin 插件升级到最新版（必须）
3. 取消 RxJava2 订阅后的黄色 `Settings -> Editor -> Inspections -> Android -> Lint -> Ignoring result`

## 1 模块与基础架构说明

### 基础架构

展示层架构：MVVM + AAC(jetpack)

- V：Fragment、View。
- VM：VM 为视图提供数据，主要方式为提供 LiveData 或者 Rx 的数据源、或者某些数据可以直接返回的，则提供对应的方法或属性供 V 直接访问。
        - LiveData 不一定就是作出成员，也可以作为方法的返回值直接返回。
        - LiveData 的使用不要局限于 VM 中，没有限制，只要场景适合，就可以使用。
- M：负责数据的存储和获取，统一命名为 xxxRepository。
- MVVM 三者通过 Dagger2 进行绑定
- 大部分情况下 Activity 仅作为 Fragment 的容器。

View 层基础封装

- BaseActivity / BaseFragment 类等封装
- 本地浏览器：BrowserActivity
- 通用资源：字符串、style、theme
- 异常处理器：ErrorHandler
- 路由导航：AppRouter

Model 层技术栈

- 网络调度封装：RxJava2 + Retrofit + OkHttp
- 磁盘缓存：StorageManager
- 网络结果处理器：ResultHandlers

## 应用缓存管理

- 与用户相关的缓存，在退出登录时必须删除。
- 与用户无关的缓存，不需要清除。
- 与设备相关的数据在变更守护模块或解绑后需要清除。
- 缓存统一使用 StorageManager 类进行管理。
- AppSettings 提供了一些基础的标志位存储功能（比如某个操作仅在第一次时触发的标记）。

### 模块

业务无关基础类库：

- lib_base：通用基础功能库，包括 BaseActivity、BaseFragment、Adapter、MVVM 、常用控件、等通用组件。
- lib_media_selector：图片、视频选择器，调用系统相机获取照片。
- lib_qrcode：二维码扫描与生成。
- lib_social：第三方平台登、分享、支付等。
- lib_network：RxJava + Retrofit + OkHttp 网络层封装。
- lib_push：第三方推送 sdk 集成。
- lib_cache：DiskLruCache、MMKV 缓存封装。

业务模块：

- module_base：基于业务封装的基础模块，所有业务组件都需要依赖此模块。
- module_home：首页组件，包括首页，应用推荐。
- module_guard：守护模块，包括时间守护、应用守护、守护等级。
- module_account：登录注册相关。
- module_binding：设备绑定。
- module_user：其他用户相关业务。
- app：不包含具体的业务实现，用于集成所有的业务组件。

## 2 代码规范

### 编程语言选择

- kotlin 和 java，都可以使用。
- kotlin 优于 java，但确保你对 kotlin 是熟悉的。
- 如果你对 kotlin 一知半解，还是使用 java 吧。

### 关于 Kotlin 代码规范

- 属性不使用 m 前缀（旧代码慢慢迁移，新代码执行此规则）
- 尽量不要使用 `!!` 操作符
- data class 的定义：除非你有其他考虑或者认为没有必要，请给 data class 的所有参数提供默认值，以便让其生成默认的无参构造函数。

## 编码

- 分包规范，参考已开发模块。
- 原则上一个模块一个 Activity，模块内界面通过 Fragment 实现。
- 尽量保证每一个类不要超过 500 行
- 不要写过于复杂的方法
- 添加必要的注释

### 资源规范

- 属于该模块特有的 `layout\color\drawable` 等资源，命名时加上模块前缀，比如 `apps_fragment_list` 表示 apps 模块 fragment 使用的一个 列表布局。
- 控件 id 使用驼峰命名，除公共模块，id 命名都要包含自己的模块名。比如 `tvHomeOpenCart` 表示 home 模块的 TextView，用来打开购物车。
- 所有资源图片放入项目之前都要在 <https://tinypng.com> 先进行压缩，先看 module_base 中是否已经存在相同的资源图片，避免重复，如果是模块独有的资源应该加上模块前缀，再放入该模块内；如果是公共资源则放入 module_base 中。
- UI 提供的资源图片命名可能不是很规范，甚至包含拼音，应该替换为英文命名。

### 工程规范

- 命名时，英文单词区分动词、名词。
- 编码格式统一为 UTF-8
- 编辑完文件后一定要格式化
- 删除多余的 import 和无用代码，减少警告出现
- 字符串资源严禁硬编码，必须定义在 xml 中。
- dimens 资源尽量定义在 xml 中。
- **请尽量消除代码中的黄色警告**
- 代码缩进空行务必规范，方法与方法之间空一行，方法与字段之间空一行，类的大括号`A{ }`与类的内容空一行，参考下面实例。
- **File Header**，在 `AndroidStudio -> Settings->Editor->File and code Templates -> File Header` 中统一定义，参考下面实例。

```java
/**
 *@author your name
 *      Email: your email
 *      Date : ${YEAR}-${MONTH}-${DAY} ${HOUR}:${MINUTE}
 */
class Example{
    
    private int mAge;
    private int mGrade;
    
    public int getAge(){
        return mAge;
    }

}
```

具体参考 

- [Android开发规范](https://github.com/Blankj/AndroidStandardDevelop#1-%E5%89%8D%E8%A8%80)
- [android kotlin style-guide](https://developer.android.com/kotlin/style-guide)
- [kotlin coding-conventions](https://www.kotlincn.net/docs/reference/coding-conventions.html)

## 3 避坑指南

- 在 TextInputLayout 中不要使用TextInputEditText，使用 FixedTextInputEditText 代替。

## 4 发版前测试覆盖

- 关注：https://github.com/Tencent/VasDolly 多渠道打包工具更新
- 一定是用打好的渠道包进行测试
- 全机型覆盖升级测试
