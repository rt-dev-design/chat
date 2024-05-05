<template>
    <view class="container">
        <view class="add-comment-prompt">为这个贴子添加评论：</view>
        <u-card :title="post.title">
        	<view slot="body">
        		<view class="u-body-item u-border-bottom u-col-between u-p-t-0">
        			<view class="u-body-item-title u-line-2">
                  {{ 
                       post.content.length <= 32 ? 
                        post.content : post.content.substring(0, 31) + "..."
                        }}
                        </view>
        			<image :src="post.images[0].url" mode="aspectFill"></image>
        		</view>
        	</view>
        </u-card>
        
        <u-line color="'#e8e8e8'"></u-line>
        <textarea 
            class="content-textarea" 
            placeholder="内容" 
            :auto-height="true" 
            maxlength="-1" 
            v-model="form.content"
        	></textarea>
        <u-line color="'#e8e8e8'"></u-line>
        
        <view style="width: 1rpx; height: 20rpx;"></view>
        <u-upload
            ref="uUpload"
            :header="header"
            :action="uploadUrl"
            :form-data="formData"
            :size-type="['original']"
            :auto-upload="false"
        	:maxCount="4"
            @on-change="onChange"
            ></u-upload>
        
        <view class="button-style">
        	<u-button 
                :custom-style="btnStyle" 
                @click="submit" 
                shape="circle"
                >发布</u-button>
        </view>
    </view>
</template>

<script>
    import logMessages from '../../utils/logmessages.js';
    import login from '../../utils/userlogin.js';
    export default {
        data() {
            return {
                btnStyle: {
                	color: "#fff",
                	backgroundColor: '#333333'
                },
                post: {},
                form: {
                    postId: 0,
                    content: "",
                },
                header: {
                	Cookie: uni.getStorageSync('token')
                },
                uploadUrl: 'http://127.0.0.1:8101/api/comment_image/upload',
                formData: {
                    biz: 'comment',
                    id: 0
                },
                showForTheFirstTime: true
            };
        },
        onLoad(option) {
            this.post = JSON.parse(decodeURIComponent(option.post));
            this.form.postId = this.post.id;
            login.ensureLoggedIn();
            logMessages("add-comment", "post", this.post.content.length);
        },
        async onShow() {
            if (this.showForTheFirstTime) {
                this.showForTheFirstTime = false;
                return;
            }
            // 如果不是第一次展示，也就是，可能是从登录界面上跳转回来，但是还未登录的
            // swtichTab
            const res = await login.testIfLoggedIn();
            if (res == null) {
                uni.navigateBack();
            }
        },
        methods: {
            onChange(res, index) {
                 logMessages(
                    "add-comment.vue", 
                    "on-change", 
                    res, index);
            },
            async submit(e) {
                if (!this.form.content) {
                	this.$u.toast('内容不能为空');
                	return;
                }
                try {
                    const res = await this.$H.post('/api/help_omment/add', this.form);
                    logMessages(
                        "add.vue",
                        "res1",
                        res
                    );
                    this.formData.id = res;
                    this.$nextTick(async () => {
                        logMessages(
                            "add-comment.vue",
                            "nextTick uUpload props",
                            this.$refs.uUpload.$props.formData
                        );
                        await this.$refs.uUpload.uploadFile();
                        uni.$showMsg('发布成功');
                        setTimeout(() => {
                            uni.navigateBack();
                        }, 200);
                    });
                    
                } catch(err) {
                    logMessages(
                        "add-comment.vue",
                        "submit catch err",
                        err
                    );
                    uni.$showMsg('发布失败，请重试');
                    setTimeout(() => {
                        uni.navigateBack();
                    }, 200);
                }
            }
        }
    }
</script>

<style lang="scss">
	.container{
		padding: 30rpx;
        
        .add-comment-prompt {
            margin: 20rpx 0;
        }
	}
	.title-input {
		border-bottom: 1px solid #F5F5F5;
		margin: 20rpx 0;
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
    
    .u-card-wrap { 
    	background-color: $u-bg-color;
    	padding: 1px;
    }
    
    .u-body-item {
    	font-size: 32rpx;
    	color: #333;
    	padding: 20rpx 10rpx;
        display: flex;
        justify-content: space-between;
        align-items: center;
    }
    	
    .u-body-item image {
    	width: 120rpx;
    	flex: 0 0 120rpx;
    	height: 120rpx;
    	border-radius: 8rpx;
    	margin-left: 12rpx;
    }
</style>
