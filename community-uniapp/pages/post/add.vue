<template>
	<view class="container">
		<input 
            class="title-input"
            placeholder="标题"
            v-model="form.title"  
            />
        <u-line color="'#e8e8e8'"></u-line>
		<textarea 
            class="content-textarea" 
            placeholder="内容" 
            :auto-height="true" 
            maxlength="-1" 
            v-model="form.content"
			></textarea>
		<u-line color="'#e8e8e8'"></u-line>
        
        <view style="width: 1rpx; height: 20rpx;">
            
        </view>
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
		
		<!-- 分类 -->
<!-- 		<view @click="chooseClass" class="choose-item">
			<u-icon class="icon add-icon" name="file-text-fill" color="#999" size="40"></u-icon>
			<text class="txt">{{ cateName || '选择帖子分类' }}</text>
			<u-icon class="u-icon" name="arrow-right"></u-icon>
		</view> -->
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
    import refresh from '../../utils/refresh.js';
    import login from '../../utils/userlogin.js';
	export default {
		data() {
			return {
				btnStyle: {
					color: "#fff",
					backgroundColor: '#333333'
				},
				form: {
                    title: "",
                    content: "",
                    tags: []
				},
				header: {
					Cookie: uni.getStorageSync('token')
				},
                uploadUrl: 'http://127.0.0.1:8101/api/image/upload',
                formData: {
                    biz: 'post',
                    id: 0
                },
                showForTheFirstTime: true,
			};
		},
		onLoad(options) {
            // 检查是否登录
            login.ensureLoggedIn();
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
                uni.switchTab({
                    url: '/pages/index/index'
                });
            }
        },
		methods: {
            onChange(res, index) {
                 logMessages("add.vue", "on-change", res, index);
            },
			async submit(e) {
                if (!this.form.content) {
                	this.$u.toast('内容不能为空');
                	return;
                }
                if (!this.form.title) {
                	this.$u.toast('标题不能为空');
                	return;
                }
                
                try {
                    const res = await this.$H.post('/api/post/add', this.form);
                    logMessages(
                        "add.vue",
                        "res1",
                        res
                    );
                    this.formData.id = res;
                    this.$nextTick(async () => {
                        logMessages(
                            "add.vue",
                            "nextTick uUpload props",
                            this.$refs.uUpload.$props.formData
                        );
                        await this.$refs.uUpload.upload();
                        
                        uni.$showMsg('发布成功');
                        setTimeout(() => {
                            uni.navigateBack({
                                success: () => {
                                    refresh.refreshIndex = true;
                                }
                            });
                        }, 300);
                        
                    });
                    
                } catch(err) {
                    logMessages(
                        "add.vue",
                        "submit catch err",
                        err
                    );
                    uni.$showMsg('发布失败，请重试');
                    setTimeout(() => {
                        uni.navigateBack();
                    }, 300);
                }
            },
        },
    }
</script>

<style lang="scss" scoped>
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
