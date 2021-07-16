# todo List

## Android Q 适配

- AndPermission 需要适配 Android Q 适配
- 具体参考：https://juejin.im/post/5cad5b7ce51d456e5a0728b0#heading-3

## 迁移到 AndroidX

支持迁移到 AndroidX 的库

- 所有 Android 官方库
- LiveDataKt
- RxBinding 
- AndPermission
- LeakCanary 
- MultiType
- PhotoView
- CircleImageView
- smartTab
- fotoapparat
- KeyboardVisibilityEvent
- AutoDispose
- AndroidUtilCode
- chenBingX/SuperTextView
- WrapperAdapter
- SpinnerDatePicker

暂不支持迁移到 AndroidX 的库

- stetho
- UETool
- qmui
- https://github.com/gzu-liyujiang/AndroidPicker
- https://github.com/bilibili/boxing
- https://github.com/Yalantis/uCrop

## 优化

- AOP：
    - 方案确定
    - 模块代码插入
    - 无痕埋点：当前实现为手动买单，后期改为采用 AOP 实现无痕埋点
- 下拉刷新控件替换，开源库选型：
    - https://github.com/dkzwm/SmoothRefreshLayout
    - https://github.com/scwang90/SmartRefreshLayout/blob/master/art/md_custom.md
    - https://github.com/cachecats/LikeMeiTuan/blob/master/app/src/main/java/com/cachecats/meituan/widget/refresh/CustomRefreshHeader.java
- [sonarqube](https://www.sonarqube.org/) 集成代码检查
- AppUpgradeChecker 做成单例模式，使用 LiveData 暴露状态

## 新框架与技术研究

- MvRx
- Epoxy
- Kotlin Coroutine



#### 图片选择器bug

https://github.com/bilibili/boxing/issues/163