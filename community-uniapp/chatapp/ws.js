/**
 * 引入Stomp协议的包裹代码
 * 引入封装了原生websocket的uniWebSocket
 * 引入WebSocket配置
 * 其中refreshToken不会被用到，在这里先删除
 */
import { Stomp } from './stomp.js';
import UniWebSocket from './uniWebSocket.js';
import { wsUrl } from './config.js';
import logMessages from '../utils/logmessages.js';

/**
 * 便于使用的Stomp连接
 * 由于uniWebSocket及其底层实现，以及config的唯一性，以及connect等方法中的逻辑，
 * 可以推测整个客户端全局只有1个连接
 * 类之间的使用关系：该类 -> Stomp -> uniWebSocket -> uni-app原生API
 */
class Ws {
    /**
     * 构造器，读取配置文件，并给全局Stomp连接一些默认值（但目前没有提供修改机制）
     * 
     * no param 
     * no return 
     * no exception
     */
	constructor() {
		// 重连间隔（？）
		this.reconnectInterval = 10000
		// 是否打印debug信息
		this.debug = true
        // stomp实例
        this.client = null
        // 重连事件编号（？）
        this.reconnectId = null
        // 订阅集合
        this.subscribes = {}
		// 根据配置构造出本实例种的ws地址
		if (wsUrl.indexOf('http://') === 0) {
			this.url = wsUrl.replace('http://', 'ws://')
		} else if (wsUrl.indexOf('https://') === 0) {
			this.url = wsUrl.replace('https://', 'wss://')
		} else {
			this.url = wsUrl
		}
	}

	/**
	 * 创建连接
	 */
	connect(headers, myConnectCallback, myErrorCallback) {
		// 如已存在连接，则不创建
		if (this.client && this.client.connected) {
			return
		}
		
        // 否则创建底层连接，并封装为Stomp连接
		let ws = new UniWebSocket(this.url, [])
		this.client = Stomp.over(ws)
        
		// 控制是否打印debug信息
        this.client.debug = (debugInfo) => {
            console.log(debugInfo)
        }
		if (!this.debug) {
			this.client.debug = () => {}
		}
        
        // 进行WebSocket之上的Stomp连接
        // 连接成功则进行该做的订阅
        // 连接失败则重连
		this.client.connect({...headers},
			frame => {
				// 正式订阅服务器的destinations
				Object.keys(this.subscribes).forEach(key => {
					this.subscribe(key, this.subscribes[key].callback)
				})
                myConnectCallback && myConnectCallback(frame);
			},
			error => {
				// 重连
				this.reconnectId = setTimeout(() => {
					this.reconnect()
				}, this.reconnectInterval)
                myErrorCallback && myErrorCallback(error);
			}
		)
        
        // 增加底层WebSocket对象的连接失败处理器
        // 如果底层断了，则顶层和底层都重连
        ws.onerror = () => {
        	// 重连
        	this.reconnectId = setTimeout(() => {
                this.reconnect();
        	}, this.reconnectInterval)
        }
	}

	/**
	 * Stomp层，也即包括WebSocket层，重新连接
	 */
	reconnect() {
		// 订阅状态置false
		Object.keys(this.subscribes).forEach(key => {
			this.subscribes[key]['subscribed'] = false
		})
		// 连接
		this.connect()
	}

	/**
	 * 断开连接
	 */
	disconnect() {
		// 断开连接
		if (this.client) {
			this.client.disconnect()
		}
		// 停止重连事件
		if (this.reconnectId) {
			clearTimeout(this.reconnectId)
			this.reconnectId = null
		}
		// 清空所有除订阅缓存
		this.subscribes = {}
	}
	
	/**
	 * 订阅
	 * @param {Object} destination 主题
	 * @param {Object} callback 回调
	 */
	subscribe(destination, callback) {
		if (this.subscribes[destination] && this.subscribes[destination]['subscribed']) { // 已订阅
			return
		} else if (this.client && this.client.connected) { // 已连接：调用订阅，缓存订阅信息
			let subscribe = this.client.subscribe(destination, res => callback(res))
			this.subscribes[destination] = { callback: callback, subscribed: true, subscribe: subscribe }
		} else { // 未连接：缓存订阅信息
			this.subscribes[destination] = { callback: callback, subscribed: false }
		}
	}
	
	/**
	 * 取消订阅
	 * @param {Object} destination 主题
	 */
	unsubscribe(destination) {
		if (this.subscribes[destination]) {
			// 取消订阅
			this.subscribes[destination].subscribe.unsubscribe()
			// 删除订阅缓存
			delete this.subscribes[destination]
		}
	}
	
	/**
	 * 向服务器发送消息
	 * @param {Object} destination 主题
	 * @param {Object} message 消息内容
	 */
	send(destination, message) {
		if (this.client) {
			this.client.send(destination, {}, message)
		}
	}
}

// 导出之前，直接构造全局单例
export default new Ws()
