/**
 * JS的类和其他语言的类一样，都封装了一个系列方法，包括构造器和其他方法，作为模板供实例化和计算
 * 这个类的主要作用有3个：
 * 1. 在构造对象时调用原生API创建连接
 * 2. 封装一堆回调函数，通过成员和数组的2种形式，并将这些利用回调和闭包传给原生API
 * 3. 将数组和成员封装成一定形式，包括各类回调和发送消息、关闭连接的方法，供Stomp库封装Stomp连接使用
 * ？表示是目前我不理解的地方，但是不重要
 */
class UniWebSocket {
	constructor(url, protocols) {
		// 是否主动关闭连接（？）
		this.activeClose = false
		
		// 连接的各类事件处理器数组
        // 连接的各种生命周期勾子数组
        // 包括连接后，关闭后，出错后，收信后
		this.openListener = []
		this.closeListener = []
		this.errorListener = []
		this.messageListener = []
		
		// 使用uni-app的创建连接API创建1个连接
        //（？为什么全局只有1个连接，如果不是这样，那为什么都是uni.xxx）
		uni.connectSocket({
			url: url,
			protocols: protocols
		})
        
        // 接下来的4段代码
        // 将Stomp传进来的，和数组中的监听器
        // 在uni-app的连接的回调中调用
		
		// 连接开启时
		uni.onSocketOpen(res => {
			this.onopen(res)
			for (let i in this.openListener) {
				this.openListener[i](res)
			}
		})
		
		// 连接关闭时
		uni.onSocketClose(res => {
			// 主动关闭连接不进行回调
			if (this.activeClose) {
				this.activeClose = false
			} else {
				this.onclose(res)
			}
			for (let i in this.closeListener) {
				this.closeListener[i](res)
			}
		})
		
		// 连接异常时
		uni.onSocketError(res => {
			this.onerror(res)
			for (let i in this.errorListener) {
				this.errorListener[i](res)
			}
		})
		
		// 接收到消息时
		uni.onSocketMessage(res => {
			this.onmessage(res)
			for (let i in this.messageListener) {
				this.messageListener[i](res)
			}
		})
	}

	/**
	 * 添加处理器
	 */
	addEventListener(eventName, callback) {
		if (eventName === 'open') {
			this.openListener.push(callback)
		} else if (eventName === 'close') {
			this.closeListener.push(callback)
		} else if (eventName === 'error') {
			this.errorListener.push(callback)
		} else if (eventName === 'message') {
			this.messageListener.push(callback)
		}
	}

	/**
	 * 移除最后一个处理器
	 */
	removeEventListener(eventName) {
		if (eventName === 'open') {
			this.openListener.pop()
		} else if (eventName === 'close') {
			this.closeListener.pop()
		} else if (eventName === 'error') {
			this.errorListener.pop()
		} else if (eventName === 'message') {
			this.messageListener.pop()
		}
	}

	/**
	 * 连接开启时的回调
	 */
	onopen(res) {}

	/**
	 * 连接关闭时的回调
	 */
	onclose(res) {}

	/**
     * 连接异常时的回调
     */
	onerror(res) {}

	/**
	 * 接收到消息时的回调
	 * @param {Object} res
	 */
	onmessage(res) {}

	/**
     * 发送消息的方法
     */
	send(data) {
		uni.sendSocketMessage({
			data: data
		})
	}

	/**
	 * 关闭连接的方法
	 */
	close() {
		this.activeClose = true
		uni.closeSocket()
	}
}

export default UniWebSocket
