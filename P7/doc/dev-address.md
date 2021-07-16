## 代码管理

- 方式：Gitlab
- 地址：http://172.168.50.230/
- 账号：请找刘军

## 原型

- v1.0.0：https://org.modao.cc/app/7l1qN4Qz2Hoz3jUGUbdpemexkUjnCp2
- v1.0.1：https://org.modao.cc/app/7deb43dd0901817ace0d88d2f5cbc58fd76c823e#screen=s7257d3585ae88ce704a722
- v1.1.0：https://org.modao.cc/app/08d312def0d1e169d907ec1a275e998b#screen=s0BA309B1F81553572213831
- v7.0：https://org.modao.cc/app/5f6142cc3b45ce4ed839b06a9336cdf6#screen=s58c6e238f9c4152c93fb6f

## 接口文档

地址：https://wechat.dev.zhixike.net/dokuwiki/doku.php?id=start&do=index&d=1562120464411

账号

- account: greenguard
- password: greenguard

深圳访问接口地址需要跳板机，步骤：

1. EMessage 找许友升获取相关信息，包括：`IP、账号、密码`。
2. Windows 搜索远程桌面，使用获取的`IP、账号、密码`进行远程连接。
3. 在远程桌面中访问接口文档地址。

## 需求文档

- v1.0.0：https://docs.qq.com/desktop/?_from=1
- v1.1.0：https://docs.qq.com/doc/DR2thRG5iUk5nbU51
- v7.0
    - https://docs.qq.com/doc/DR1F2UW9qUU1Db0xD
    - 孩子端设备为IOS时影响范围说明：https://docs.qq.com/sheet/DR3dlVlRwQ2FRbHdw?opendocxfrom=admin&preview_token=&tab=BB08J2&coord=C20A0F0

权限：请找产品经理开通权限

## 需求管理

- TAPD：`https://www.tapd.cn/`

## 数据库

- 数据库地址(测试/开发环境)：172.168.50.56
- 用户：greenguard
- 密码：gg666666

相关查询语句

```sql
SELECT * FROM `t_user`
UPDATE t_user set USER_NAME= '18817013387_c' WHERE USER_NAME='18817013387'
SELECT * FROM `t_user` WHERE PHONE='16675331583'
SELECT * FROM t_record_user_bind WHERE P_USER_ID='1668C947ACA83239DE039EDD'
DELETE FROM t_record_user_bind WHERE P_USER_ID = '1668C947ACA83239DE039EDD'
DELETE FROM t_record_user_device WHERE DEVICE_ID = 'fac33f21af9037193795bd4e4e06b846'
```

## svn(需求相关资料)

- 地址：svn://172.168.50.241/gelei_protect/
- 账号：邮件下发

## 第三方账号

蒲公英：

- account：chenxf@txtws.com
- password：lwtx2018

## 跳板机

- 跳板机远程桌面地址：`172.168.50.225`
- 账号密码：`zhantianyou/123321`

## 关于第三方账号

1. APP 开发需要集成一些第三方 SDK，包括分享、登录、支付等功能。
2. 下列账号申请需要的邮箱/手机号等信息，统一由公司提供，不能使用私人账号注册。

第三方分享/登录账号准备

- 微信
- 微博
- QQ

移动支付账号准备

- 微信
- 支付宝

第三方SDK账号、AppKey准备

- 极光推送
- ShareSDK
- 高德地图
- 友盟统计
- 环形IM
- Bugly 升级与热修复
   
## 会员状态操作

```log
http://172.168.50.89:32572/debug/test/member
-------------------------------------------------------
{
"app_token": "eyJhbGciOiJIUzI1NiJ9.eyJBUFBfVE9LRU46IjoiMTU5NTkyNzY2ODZfTjllc0RzIiwiZXhwIjoxNTU3OTA5NTg4fQ.EkVKs3O929UAMAPn8bN8ZS931TrF1JbSOPgacRe6LS4",
"sign": "15681703760005426ed0dfaac3ceff930b85c77aeacd3a6aecf7cb97b66fcc65536fd3c7e9056",
"gnw_appid": "437EC0AC7F0000015E2BBF4849643C96",
"source": "01",
"version": "1.0.0",
"type": "1"
}
---------------------------------------------------
// type = 1 : 会员到期前7天发送短信（调用此接口后，会员日期就会被改变成7天后到期，且发推送给家长端）
// type = 2 : 会员到期1天后替用户保留（调用此接口后，会员就会变成昨天23:59到期，且帮用户保留默认设备）
```