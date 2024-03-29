<template>
	<view>
        <text>发送方：</text><input class="uni-input" v-model="nickname" />
        <text>接收方：</text><input class="uni-input" v-model="fullname" />
		<button @click="connect">连接</button>
		<button @click="disconnect">断开</button>
		<button @click="subscribe">订阅</button>
		<text>消息：</text><input class="uni-input" v-model="msg" />
		<button @click="send">发送消息</button>
		<view v-for="(item, index) in dataList" :key="index">
			{{index}}:{{item.content}}
		</view>
	</view>
</template>

<script>
	import ws from '@/websocket/ws.js'
	export default {
		data() {
			return {
				msg: '123',
                recipientId: 'runtian',
				dataList: [],
                nickname: 'rt1',
                fullname: 'rt2',
			}
		},
		onLoad() {
			// 模拟获取token，此步操作应在登录时去做
		},
		methods: {
			/**
			 * 创建连接
			 */
			connect() {
				ws.connect()
			},
			/**
			 * 断开连接
			 */
			disconnect() {
				ws.disconnect()
			},
			/**
			 * 订阅
			 */
			subscribe() {
				ws.subscribe(`/user/${this.nickname}/queue/messages`, this.message)
			},
			/**
			 * 接收消息
			 * @param {Object} data
			 */
			message(data) {
				console.log(JSON.parse(data.body))
				this.dataList.push(JSON.parse(data.body))
			},
			/**
			 * 向服务端发送消息
			 */
			send() {
				ws.send('/app/test/chat', JSON.stringify({
                    testMessages: [this.fullname, this.msg]
                }))
			}
		}
	}
</script>

<style>
	.uni-input {
		height: 40px;
	}
</style>
