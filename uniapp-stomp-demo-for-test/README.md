## 简介
Stomp.js搭配原生WebSocket使用，兼容h5、App、小程序。由于三端均支持WebSocket协议，故没有使用SockJS。

## 使用说明
- 使用时请根据需要修改 **ws.js** 文件，如连接地址引入方式、WebSocket认证方式、token刷新方式、调试信息打印等问题。
- **config.js** 文件提供了测试地址和模拟token刷新方法。
- **stomp.js** 文件的最后部分添加了一个 “兼容uniapp” 的判断。
