<template>
	<view class="error">
		<text class="info">出错了</text>
		<u-button type="primary" class="btn-home" @click="goHome">返回首页</u-button>
	</view>
</template>
<script>
	export default {
		data() {
			return {

			};
		},
		methods: {
			goHome() {
				uni.switchTab({
					url: '/pages/index/index'
				})
			}
		}
	}
</script>
<style lang="scss" scoped>
	.error {
		display: flex;
		align-items: center;
		justify-content: center;
		flex-direction: column;
		padding-top: 150rpx;

		.info {
			margin-bottom: 100rpx;
			color: #aaaaff;
			font-weight: 600;
			font-size: 60rpx;
		}
	}
</style>