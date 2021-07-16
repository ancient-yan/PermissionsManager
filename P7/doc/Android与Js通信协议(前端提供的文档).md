# gelei-guard

## 主要功能

 1. 支持字体图标,css分离打包
 2. 各入口文件分离打包,第三方库模块打包,公共组件分离打包
 3. 支持vue-router路由按需加载
 4. 可自定义页面入口模块名
 5. 基于`webpack2`，全面支持`ES6 Modules`
 6. 热更新
 7. 支持`Less`css预处理,`Sass`css预处理
 8. 全局注册vue全局过滤器的方法 

## Build Setup

``` bash
# 安装依赖
npm install

# 调试环境 serve with hot reload at 172.168.40.27:1234
npm run dev

# 开发环境调app端签名(目前调android端)
npm run dev:app

# 生产环境 build for production with minification
npm run build
```

本地默认访问端口为1234，需要更改的童鞋请到项目目录文件`config/index.js`修改。

## 目录结构

```log
webpack
 |---build
 |---static
     |---vendor #第三方的js文件，直接拷贝到dist目录（希望后期使用cdn）
 |---src
     |---assets    #资源
     |---css/common.css  #css
     |---fonts/    #字体图标
 |---components 组件
     |---baseheader  #头部组件-未投入使用
     |---toast     #toast组件
 |---config 插件
     |---js/common.js    #全局样式依赖与公共组建
     |---js/vueFilter.js    #注册vue的全局过滤器
 |---server 接口
     |---sign.js    #接口签名方式
     |---http.js    #axios配置，及异常处理
     |---index.js    #暴露接口名给组件调用
     |---android.js   #导入android端提供的方法
 |---page    #各个页面模块，模块名可以自定义哦！
     |---address    #一级目录
        |---address.html
        |---address.js
        |---addresschange.vue
        |---app.vue
     |---list
        |---list.html#二级目录
        |---list.js
        |---app.vue
......
```

例如 http:// 172.168.40.27:1234/list.html，`page`就是我们线上的模块名，如需修改请到项目目录文件config/index.js修改`moduleName`参数，修改这里的配置的同时，也要同时重命名`/src/page`的这个文件夹名称，是否会报错的。

从目录结构上，各种组件、页面模块、资源等都按类新建了文件夹，方便我们储存文件。其实我们所有的文件，最主要都是放在`page`文件夹里，以文件夹名为html的名称。

在`page`里二级文件夹，一个文件夹就是一个入口，`js``vue``html` 都统一放在当前文件夹里

## 待办事项
- 关于我们：微信客服的UI页面（二维码、头像带提供）；端提供方法端->保存至相册
- 关于我们：端提供方法->版本号
- 帮助与反馈：数据待测试；端方法->跳转到吐槽一下；问题列表注意事项：(换行使用 br)( 成长攻略：配置a标签)
- 成长记录：更多数据待测；端提供方法->跳转成长树浇水，和成长日记
- 守护报告：端方法->查询日期
- import按需加载：chunk如何命名
- app提供->需要孩子ID(成长记录)

## 端提供的方法

描述|方法 | 参数 |返回值
---|---|---|---
保存图片|savePhoto | (string)photoUrl|
获取版本号|getVersion | |string
跳到成长树浇水页面|skipTreePage|
跳到成长日记页面|skipDirtyPage
吐槽一下页面|skipDissPage
签名加密|sign(json, isNeedDeviceId, isNeedToken,isNeedChildId)
获得设备id|getDeviceId
获得孩子id|getChildId
获得登录token|getToken

## web提供的方法

描述|方法 | 参数 |返回值
---|---|---|---
守护报告切换时间|switchTime | (string)value|


## git忽略已经push在仓库中的文件

```bash
git update-index --assume-unchanged config/dev.env.js
git update-index --assume-unchanged package.json

#若以后不想忽略该文件的修改，则输入命令：
git update-index --no-assume-unchanged FILENAME 
```

## 命令行参数

- NODE_BUILD：打包生产环境时，选择测试或其他
- NODE_HOST：开发环境启动的地址
- NODE_SIGN：开发，或生产，选择的签名方式，走app端，或自己生成

