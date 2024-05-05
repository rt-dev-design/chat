<template>
	<view>
		<view class="info-container">
			<u-form ref="uForm" label-width="auto">
				<u-form-item label="头像">
					<u-avatar @click="updateAvatar" mode="square" slot="right" :src="userInfo.userAvatar" size="100"></u-avatar>
				</u-form-item>
				<u-form-item label="昵称" right-icon="arrow-right">
					<u-input  
                        :placeholder="userInfo.userName"
						:disabled="false" 
                        input-align="right"
                        @blur="updateUserName"
                        />
				</u-form-item>
				<u-form-item label="个人说明" right-icon="arrow-right">
					<u-input  
                        :placeholder="userInfo.userProfile" 
                        :disabled="false"
						input-align="right"
                        @blur="updateUserProfile"/>
				</u-form-item>
			</u-form>
		</view>
		<view class="out-btn">
			<u-button :custom-style="btnStyle" @click="logout">退出登录</u-button>
		</view>
	</view>
</template>

<script>
    import logMessages from '../../utils/logmessages';
    import login from '../../utils/userlogin.js';
    import chatapp from '../../chatapp/chatapp.js';
	export default {
		data() {
			return {
				btnStyle: {
					color: "#fff",
					backgroundColor: '#333333'
				},
				userInfo: {},
                userNameTimer: null,
                userProfileTimer: null,
			};
		},
		async onShow(options) {
            const duh = await login.ensureLoggedIn();
            this.userInfo = uni.getStorageSync('user');
		},
        async onLoad() {
            const duh = await login.ensureLoggedIn();
            this.userInfo = uni.getStorageSync('user');
        },
        
		methods: {
            async updateUserProfile(value) {
                clearTimeout(this.userNameTimer);
                this.userNameTimer = setTimeout(async () => {
                    try {
                        const res = await this.$H
                        .post('/api/user/update/my', {userProfile: value});
                        if (!res) throw "更新错误：res = " + res;
                        uni.$showMsg('更新成功');
                    } catch(err) {
                        logMessages(
                            "edit.vue",
                            "updateUserProfile err",
                            err
                        );
                    }
                }, 1000);
            },
            async updateUserName(value) {
                clearTimeout(this.userNameTimer);
                this.userNameTimer = setTimeout(async () => {
                    try {
                        const res = await this.$H
                        .post('/api/user/update/my', {userName: value});
                        if (!res) throw "更新错误：res = " + res;
                        uni.$showMsg('更新成功');
                    } catch(err) {
                        logMessages(
                            "edit.vue",
                            "updateUserName err",
                            err
                        );
                    }
                }, 2000);
            },
			// 修改性别
			saveGender(index) {
				let gender = index[0].value;
				this.$H.post("user/userInfoEdit", {
					gender: gender
				}).then(res => {
					if (res.code == 0) {
						this.userInfo.gender = index[0].label
					}
				})
			},
			async logout() {
                try {
                    const res = await this.$H.post('/api/user/logout');
                    logMessages(
                        "edit.vue",
                        "logout try res",
                        res
                    );
                    if (!res) throw "登出错误：res = " + res;
                    chatapp.disconnect();
                    uni.removeStorageSync("token");
                    uni.removeStorageSync("user");
                    uni.$showMsg('登出成功，即将返回');
                    setTimeout(() => {
                        uni.switchTab({
                        	url: "/pages/index/index",
                        });
                    }, 500);

                } catch(err) {
                    logMessages(
                        "edit.vue",
                        "logout catch err",
                        err
                    );
                }
			},
			updateAvatar() {
				let that = this;
				uni.chooseImage({
					count: 1,
					sizeType: ['original', 'compressed'],
					sourceType: ['album'],
					success: function(res) {
						uni.showLoading({
							mask: true,
							title: "上传头像中"
						})
						uni.uploadFile({
							url: that.$c.domain + '/api/user/upload-avatar',
							filePath: res.tempFilePaths[0],
							name: 'file',
							header: {
								Cookie: uni.getStorageSync("token")
							},
                            formData: {
                                id: that.userInfo.id,
                                biz: 'user_avatar'
                            },
							success: (uploadFileRes) => {
								let data = JSON.parse(uploadFileRes.data);
                                logMessages(
                                    "edit.vue",
                                    "upload success data",
                                    data
                                );
                                that.userInfo.userAvatar = data.data;
                                uni.hideLoading();
                                uni.$showMsg('更新成功');
							}
						});
					}
				});

			},
		}
	}
</script>
<style scoped>
	.info-container {
		padding: 20rpx;
		background-color: #FFFFFF;
	}

	.out-btn {
		margin: 40rpx 30rpx;
	}
</style>
