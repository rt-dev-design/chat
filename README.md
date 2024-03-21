# chat功能模块的架构设计

## 服务集群中的各个源码项目

- chat-commons：数据原型和相关工具，即entity, dto, vo, enum, util
- chat-inner-api：内部RPC接口类，供消费者引入和注入，提供者引入和实现
- chat-websocket-gateway：Stomp网关，对外Stomp端点都在这里
- chat-http-gateway：HTTP网关，对外HTTP端点都在这里
- chat-wssession-service：连接服务，维护用户-Stomp网关的关系和在线状态的数据库（Redis），提供RPC接口
- chat-routing-service：路由服务，提供RPC接口供网关调用，同时协调调用其他服务和Stomp网关的RPC接口
- chat-chat-service：聊天服务，管理聊天资源，提供相关接口
- chat-message-service：消息服务，管理消息资源，提供相关接口

## 前端应用中的源码模块

- 应用层面的全局数据管理模块，维护聊天功能的状态
  - 用户当前在哪个页面
  - 消息页面的相关数据
  - 聊天页面的相关数据

    ```javascript
    {
        userIsCurrentlyOnPage: "chat" 
        (or "message", or "others"),

        messagePageChatId: "chatId",
        enableStompMessageAppending: true,
        messages: [],
        messagesRequest: {
          current: 1,
          pageSize: 10,
          beforeTime: "",
        },

        chats: [],
        chatsRequest: {
          current: 1,
          pageSize: 10,
          sortField: "lastMessageTime",
          sortOrder: "descend"
        }
    }
    ```

- 调用上述模块更新数据，且对于这些数据进行UI响应的UI组件
  - chat聊天页面
  - message消息（具体的某个聊天）页面
  - tabbar

## 端到端业务流程

---

- 应用启动时且在用户登录后，前端发起请求，与某个Stomp网关的/ws端点建立Stomp连接
  - 连接建立时
    - 前端发送以下消息到/app/user-connect端点
  
      ```json
      {
          "userId": "userId"
      }
      ```

    - 网关接收到消息，取出自身的ip和port，调用连接服务的setOnline，连接服务为用户创建或更新连接，并修改状态为上线

      ```json
      {
          "userId": "userId",
          "ip": "ip",
          "port": "port"
      }
      ```

  - 连接建立时
    - GET自用户上一次离线之后的是否有未读消息

      ```json
      {
          "userId": "userId"
      }
      ```

    - http网关接收到消息，转发给聊天服务
    - 聊天服务查询并返回该用户是否有未读消息
  
      ```json
      {
          true / false
      }
      ```

    - 前端依据页面情况更新页面和tabbar，如果在聊天页则无需更新，否则需要更新
  - 连接建立时，前端订阅网关的/user/${username}/queue/message端点，准备实时接受消息

---

- 当前端从/user/${username}/queue/message端点收到消息
  - 消息形式为
  
    ```json
    {
        message: {...} (其中有chatId)
    }
    ```

  - 前端根据用户在哪个页面进行界面更新
    - 若用户在others，则更新tabbar，提示有新消息
    - 若用户在chat，则直接刷新chat的视图（这里做了简化）
    - 若用户在message
      - 若该message的chatId和新消息chatId不匹配，更新tabbar
      - 否则，立刻更新到message的界面上，即添加到messages数组的最后面，如果enableStompMessageAppending为false，则设置延时3秒后再进行append

---

- 初次加载和之后每次显示chat页面，以及下拉刷新该页面时
  - 前端发http请求向后端分页请求这个用户的聊天视图，代替全局数据的chats数组
  - 同时更新userIsCurrentlyOnPage

    ```json
    [
        {
            ...chat,
            thereAreNewMessages: true / false
        },
        ...
    ]
    ```

- 每次退出或者隐藏页面时，更新userIsCurrentlyOnPage为others

---

- 初次加载和之后每次显示聊天页面，先设置enableStompMessageAppending为false，请求聊天数据，渲染页面，然后再设置enableStompMessageAppending为true
- 每次退出或者隐藏页面时，更新userIsCurrentlyOnPage为others，清除chatId

发送消息时，向/app/chat发送Stomp消息
消息转发服务查询连接会话
若不在线则不转发，否则立刻转发
同时，
调用消息服务、对话服务、中转服务进行存储

---

- 用户注销或者应用关闭时
  - 前端发消息到/app/user-disconnect端点，网关接收消息后，调用连接服务的setOffline，传入userId，修改用户在线状态为下线
  - 同时，前端断开Stomp连接
