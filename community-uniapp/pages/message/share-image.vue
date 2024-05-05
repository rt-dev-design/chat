<template>
	<view class="container">
        <view style="width: 1rpx; height: 20rpx;"></view>
        <u-upload
            ref="uUpload"
            :header="header"
            :action="uploadUrl"
            :form-data="formData"
            :size-type="['original']"
            :auto-upload="false"
        	:maxCount="9"
            @on-change="onChange"
            ></u-upload>
		<view class="button-style">
			<u-button 
                :custom-style="btnStyle" 
                @click="submit" 
                shape="circle"
                type="primary"
                >发送</u-button>
		</view>
	</view>
</template>

<script>
    import logMessages from '../../utils/logmessages.js';
    import refresh from '../../utils/refresh.js';
    import login from '../../utils/userlogin.js';
    import chatapp from '../../chatapp/chatapp.js';
    import store from '../../store/index.js';
	export default {
		data() {
			return {
				header: {
					Cookie: uni.getStorageSync('token')
				},
                uploadUrl: 'http://127.0.0.1:8101/api/file/upload',
                formData: {
                    biz: 'chat_image',
                },
                showForTheFirstTime: true,
                messageToSend: {
                    type: "image",
                    content: "",
                    senderId: -1,
                    recipientId: -1,
                    chatId: -1
                },
                uploadedImage: [],
			};
		},
		onLoad(options) {
            login.ensureLoggedIn();
            this.messageToSend.senderId = options.senderId;
            this.messageToSend.recipientId = options.recipientId;
            this.messageToSend.chatId = options.chatId;
		},
        async onShow() {
            if (this.showForTheFirstTime) {
                this.showForTheFirstTime = false;
                return;
            }
            const res = await login.testIfLoggedIn();
            if (res == null) {
                uni.switchTab({
                    url: '/pages/index/index'
                });
            }
            
        },
		methods: {
            onChange(res, index) {
                 logMessages("share-image.vue", "on-change", JSON.parse(res.data).data, index);
                 const image = JSON.parse(res.data).data;
                 this.uploadedImage.push(image);
                 const messageCopy = {};
                 Object.assign(messageCopy, this.messageToSend);
                 messageCopy.content = image;
                 chatapp.sendChatMessage(messageCopy);
                 store.dispatch('toggleScrollSwitch');
            },
			async submit(e) {
                try {
                    await this.$refs.uUpload.upload();
                    uni.$showMsg('发送成功');
                    setTimeout(() => {
                        uni.navigateBack();
                    }, 300);
                } 
                catch(err) {
                    logMessages("share-image.vue", "submit err", err);
                    uni.$showMsg('发送失败，请稍后重试');
                    setTimeout(() => {
                        uni.navigateBack();
                    }, 300);
                }
            },
        },
    }
</script>

<style lang="scss">
	.container{
		padding: 30rpx;
	}
	.title-input {
		// border-bottom: 1px solid #F5F5F5;
		margin: 15rpx 0;
		padding: 20rpx 0;
	}

	.content-textarea {
		width: 100%;
		padding: 20rpx 0;
		min-height: 300rpx;
	}

	.choose-item {
		display: flex;
		align-items: center;
		padding: 20rpx;
		border-bottom: 1px solid #F5F5F5;

		&:last-child {
			border: 0;
		}

		.txt {
			margin-left: 20rpx;
		}

		.sw {
			margin-left: 300rpx;
		}

		.inputStyle {
			margin-left: 60rpx;
			width: 400rpx;
		}

		.radio {
			margin-left: 320rpx;
		}

		.icon {
			width: 40rpx;
			height: 40rpx;
		}

		.u-icon {
			margin-left: auto;
			color: #999;
		}

		.add-icon {
			margin-left: 0;
		}
	}

	.button-style {
		margin-top: 50rpx;
		color: #F4F4F5;
	}
</style>
