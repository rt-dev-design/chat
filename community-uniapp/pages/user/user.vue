<template>
	<view>
		<view class="head">
            <block v-if="isLoggedIn">
				<view class="userinfo" @click="goUser">
					<u-avatar :src="userInfo.userAvatar"></u-avatar>
					<view class="username">
						<text>{{ userInfo.userName }}</text>
						<text class="sub-txt">{{ userInfo.userProfile ? userInfo.userProfile : '' }}</text>
					</view>
					<u-icon name="arrow-right" class="arrow-right"></u-icon>
				</view>
            </block>
            <block v-else>
				<view class="btn-login">
					<u-button type="default" shape="circle" @click="wxLogin" plain>登录</u-button>
				</view>
            </block>
		</view>
		<view class="block-wrap">
			<u-grid :col="3" :border="false" style="margin: 20rpx 0;" @click="toNav">
				<u-grid-item index="/pages/my/post">
					<image class="gn-icon" src="/static/img/question.png"></image>
					<view class="grid-text">我的帖子</view>
				</u-grid-item>
				<u-grid-item index="/pages/my/favor">
					<image class="gn-icon" src="/static/img/love.png"></image>
					<view class="grid-text">我的收藏</view>
				</u-grid-item>
				<u-grid-item index="/pages/my/comment">
					<image class="gn-icon" src="/static/img/comment.png"></image>
					<view class="grid-text">我的评论</view>
				</u-grid-item>				
			</u-grid>
		</view>
	</view>
</template>

<script>
	import addPostTag from '@/components/add-post-tag/add-post-tag.vue';
    import logMessages from '../../utils/logmessages.js';
    import login from '../../utils/userlogin.js';
    import tabbarMessageReminder from '../../mixins/tabbar-message-reminder.js';
	export default {
        mixins: [tabbarMessageReminder],
		components: {
			addPostTag
		},
		data() {
			return {
				userInfo: {},
				isLoggedIn: false,
			};
		},
		async onLoad() {
            // 检查是否登录，是则在页面上保存用户信息和设置登录态，否则设置登录态未false（保持默认）
            const res = await login.testIfLoggedIn();
            if (res) {
                this.userInfo = res;
                this.isLoggedIn = true;
            } 
            logMessages(
                "user.vue",
                "onLoad userInfo",
                this.userInfo,
                "onLoad isLoggedIn",
                this.isLoggedIn
            );
		},
		async onShow() {
            const res = await login.testIfLoggedIn();
            if (res) {
                this.userInfo = res;
                this.isLoggedIn = true;
            } else {
                this.isLoggedIn = false;
            }
            logMessages(
                "user.vue",
                "onLoad userInfo",
                this.userInfo,
                "onLoad isLoggedIn",
                this.isLoggedIn
            );
		},
		methods: {
			goUser() {
				uni.navigateTo({
					url: '/pages/user/edit'
				});
			},
			toNav(url) {
				uni.navigateTo({
					url: url
				});
			},
            wxLogin() {
                uni.navigateTo({
                	url: '/pages/login/weixin'
                });
            }
		}
	};
</script>

<style>
	page {
		background-color: #f5f5f5;
	}
</style>
<style lang="scss" scoped>
	.head {
		padding: 20rpx;
		background-color: #fff;

		.sub-txt {
			font-size: 24rpx;
			color: #616161;
			display: block;
			display: -webkit-box;
			-webkit-box-orient: vertical;
			-webkit-line-clamp: 1;
			overflow: hidden;
		}

		margin-bottom: 20rpx;
	}

	.userinfo {
		display: flex;
		align-items: center;
		padding: 20rpx;
	}

	.userinfo .username {
		display: flex;
		flex-direction: column;
		margin-left: 20rpx;
	}

	.grid-text {
		color: #999;
		font-size: 12px;
		margin-bottom: 20rpx;
	}

	.userinfo u-avatar {
		margin-right: 20rpx;
	}

	.userinfo .arrow-right {
		margin-left: auto;
	}

	.btn-login {
		margin: 40rpx 0;
	}

	.gn-icon {
		width: 60rpx;
		height: 60rpx;
		margin: 20rpx 0;
	}

	/*服务按钮*/
	.btn-wrap {
		display: flex;
		margin-top: 30rpx;
	}

	.btn-wrap .btn-contact {
		background-color: #fff;
		margin-left: 15rpx;
		margin-right: 15rpx;
		padding: 20rpx;
		line-height: unset;
		font-size: 12px;
		color: #999;
	}

	.btn-wrap .btn-contact:active {
		background-color: #f5f5f5;
	}

	.btn-wrap .btn-contact .txt {
		margin-top: 20rpx;
	}

	.btn-wrap .btn-contact::after {
		border: unset;
		position: unset;
	}

	.icon-size {
		font-size: 50rpx;
	}

	.block-wrap {
		background-color: #fff;
		border-radius: 20rpx;
		margin: 20rpx;
		overflow: hidden;

		.block-title {
			background-color: #fff;
			padding: 20rpx;
		}
	}
</style>