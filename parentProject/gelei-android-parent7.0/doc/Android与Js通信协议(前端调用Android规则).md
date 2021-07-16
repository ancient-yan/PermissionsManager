# Android 与 JavaScript 通信协议

## 1 调用方式

1. Android 调用 JavaScript：常规方式，js提供方法即可
2. JavaScript 调用 Android：
   1. 无返回值调用：`window.confirm(xxx)`
   2. 有返回值调用：`var result = window.prompt(xxx)`

## 2 传参方式

上面 xxx 表示传参内容，格式为：`callAndroid#a#a#a#a#方法名#a#a#a#a#参数1#a#a#a#a#参数2`。

说明：

- callAndroid 为固定前缀，表示用来调用安卓提供的方法，安卓端会拦截进行处理，否则安卓端不做任何拦截。
- `#a#a#a#a#` 为固定分隔符。

示例：

| 参数                                        | 意义                               |
| ------------------------------------------- | ---------------------------------- |
| `callAndroid#a#a#a#a#getVersion`            | 调用 Android 的 getVersion 方法    |
| `callAndroid#a#a#a#a#saveImage#a#a#a#a#url` | 调用 Android 的 saveImage(url) 方法 |

