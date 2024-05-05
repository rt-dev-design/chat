<template>
	<view>
		<view style="position: absolute;">
        <u-navbar
            title="用户主页"
            :autoBack="true"
        >
        </u-navbar>
		</view>
		<!-- 主体容器，盛放信息卡片和贴子 -->
		<view class="container">
			<!-- 信息卡片 -->
			<view class="info-wrap">
				<u-avatar
					:src="userInfo.userAvatar"
                    :size="120"
					></u-avatar>
				<view class="username">{{userInfo.userName}}</view>
                <view class="user-profile" v-if="userInfo.userProfile">{{userInfo.userProfile}}</view>
				<view class="btn-box" v-if="currentUid !== uid">
					<u-button :custom-style="btnStyle2" @click="goToMessagePage" shape="circle" size="mini">
						<text style="margin: 0 15rpx;">私信</text>
					</u-button>
				</view>
			</view>
			<!-- 该用户发的帖子 -->
            <view class="divider">
                <text class="divider-text">Ta的贴子</text>
            </view>
            
            <ugen-content-list
            :list="postList"
            @update:list="event => this.$set(this.postList, event.index, event.newVal)"
            :loadStatus="loadStatus"
            :do-thumb-api-url="postFavorUrl"
            :do-favor-api-url="postThumbUrl"
            :content-page-url="'/pages/post/post'"
            ></ugen-content-list>
		</view>
		<!-- 发贴入口 -->
		<add-post-tag></add-post-tag>
	</view>
</template>

<script>
	import addPostTag from '@/components/add-post-tag/add-post-tag.vue';
    import logMessages from '../../utils/logmessages';
	export default {
		data() {
			return {
				btnStyle: {
					color: "#fff",
					backgroundColor: '#000000'
				},
				btnStyle2: {
					border: '1px solid #000000',
					color: "#000000"
				},
				background: {
					backgroundColor: 'unset'
				},
                loading: true,
                uid: 0,
                currentUid: 0,
                
				userInfo: {},
                postList: [],
				loadStatus: "loading",
                request: {
                	current: 1,
                	pageSize: 5
                },
				pages: 10000,
                postFavorUrl: '/api/post_favour/',
                postThumbUrl: '/api/post_thumb/',
			};
		},
		onLoad(options) {
			this.uid = options.uid;
            logMessages(
                "home.vue",
                "onLoad uid",
                this.uid
            );
			this.getUserInfo();
			this.getPostList();
            let cur = uni.getStorageSync('user').id;
			if (cur) {
				this.currentUid = cur;
			}
		},
		onReachBottom() {
            if (this.pages <= this.request.current) return;
			this.request.current++;
			this.getPostList();
		},
		methods: {
			onBack() {
				uni.navigateBack();
			},
            goToMessagePage() {
                uni.navigateTo({
                    url: '/pages/message/message?theOtherUsersId=' + this.uid
                });
            },
			getPostList() {
				this.loadStatus = "loading";
				this.$H.post('/api/post/list/page/vo', {
					...this.request,
                    userId: this.uid,
				}).then(res => {
                    logMessages(
                        "home.vue",
                        "getPostList then res",
                        res
                    );
                    
					this.postList = this.postList.concat(res.records);
                    this.request.current = res.current;
                    this.request.pageSize = res.pageSize;
                    this.pages = res.pages;
					if (this.pages <= this.request.current) {
						this.loadStatus = "nomore";
					} else {
						this.loadStatus = "loadmore"
					}
				}).catch(err => {
                    logMessages(
                        "home.vue",
                        "getPostList then res",
                        err
                    );
                })
			},
			getUserInfo() {
				this.$H.get('/api/user/get/vo', {
					id: this.uid
				}).then(res => {
                    this.userInfo = res;
                    uni.setNavigationBarTitle({
                    	title: this.userInfo.userName
                    });
					this.loading = false;
				}).catch(err => {
                    logMessages(
                        "home.vue",
                        "getUserInfo catch",
                        err
                    );
                    uni.$showMsg('出错了，即将跳转');
                    setTimeout(() => {
                        uni.switchTab({
                            url: '/pages/index/index'
                        })
                    }, 1000);
                });
			}
		}
	}
</script>

<style>
	page {
		background-color: #f5f5f5;
	}
</style>
<style lang="scss" scoped>
	.container {
		padding-top: 130rpx;
		width: 100%;
		
		.info-wrap {
			background: #fff;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			border-radius: 10rpx;
			border: 1px solid lightgray;
			box-shadow: 0 1px 1px rgba(0, 0, 0, 0.1);
			padding: 20px;
			margin: 20rpx 8rpx;
			.username {
				font-size: 40rpx;
				font-weight: bold;
				margin-top: 30rpx;
			}
            
            .user-profile {
            	font-size: 30rpx;
            	margin-top: 20rpx;
            }
			
			.btn-box {
				margin-top: 30rpx;
			}
		}
        
        .divider {
          width: 100%;
          text-align: center;
          margin: 10px 0;
          
          .divider-text {
            display: inline-block;
            padding: 10px 20px;
            font-size: 18px;
            font-weight: bold;
          }
        }
		
	}

</style>
