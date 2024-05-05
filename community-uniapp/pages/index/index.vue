<template>
	<view>
        <view class="waterfall" v-if="latestPosts.length !== 0">
            <view class="left">
                <block v-for="(item, index) in oddList" :key="index">
                    <view @click.stop="goToContent(item.id)">
                        <adaptive-waterfall-cell
                            :content="item"
                            ></adaptive-waterfall-cell>
                    </view>
                </block>
            </view>
            <view class="right">
                <block v-for="(item, index) in eventList" :key="item.id">
                    <view @click.stop="goToContent(item.id)">
                        <adaptive-waterfall-cell
                            :content="item"
                            ></adaptive-waterfall-cell>
                    </view>
                </block>
            </view>
        </view>
		<!-- 漂浮的发贴入口 -->
		<add-post-tag></add-post-tag>
	</view>
</template>

<script>
	import addPostTag from '@/components/add-post-tag/add-post-tag.vue';
    import logMessages from '../../utils/logmessages.js';
    import refresh from '../../utils/refresh.js';
    import tabbarMessageReminder from '../../mixins/tabbar-message-reminder.js';
	export default {
        mixins: [tabbarMessageReminder],
        computed: {
            oddList() {
                return this.latestPosts.filter((_, index) => index % 2 === 1);
            },
            eventList() {
                return this.latestPosts.filter((_, index) => index % 2 === 0);
            },
        },
		data() {
			return {
                showFirstTime: true,
                contentPageUrl: "/pages/post/post",
				latestPosts: [],
				pages: 0,
				loadStatus: 'loadmore',
				request: {
					current: 1,
					pageSize: 10
				},
                // 给主页上的贴子收藏和点赞
                doFavorApiUrl: "/api/post_favour/",
                doThumbApiUrl: "/api/post_thumb/"
			};
		},
		
		onLoad() {
			this.getLastPost();
            
		},
        onShow() {
            if (refresh.refreshIndex) {
                uni.startPullDownRefresh({
                    complete: () => {
                        uni.stopPullDownRefresh();
                        refresh.refreshIndex = false;
                    }
                })
            }
        },
		onReachBottom() {
			if (parseInt(this.request.current) >= parseInt(this.pages)) {
                logMessages(
                    "index.vue",
                    "bottom 111", 
                    this.request.current,
                    this.pages
                );
				return;
			}
            console.log('bottom')
			this.request.current++;
			this.getLastPost();
		},
		onPullDownRefresh() {
			this.request.current = 1;
			this.latestPosts = [];
			this.getLastPost();
			uni.stopPullDownRefresh();
		},
		methods: {
			//获取最新帖子
            goToContent(id) {
                uni.navigateTo({
                	url: '/pages/post/post?id=' + id
                });
            },
			getLastPost() {
                // /api/post/list/page/vo
                // /api/post/list/page
				this.loadStatus = 'loading';
				this.$H
					.post('/api/post/list/page/vo', {
						...this.request
					})
					.then(res => {
						this.latestPosts = [...this.latestPosts, ...res.records];
						console.log(this.latestPosts);
						this.request.current = res.current;
						this.pages = res.pages;
						if (res.current >= res.pages) {
							this.loadStatus = 'nomore';
						} else {
							this.loadStatus = 'loadmore';
						}
					})
                    .catch(err => {
                        logMessages("index.vue",
                            "err",
                            err
                        );
                    });
			}
		}
	};
</script>

<style lang="scss">
	page {
		background-color: #ebebeb;
	}
    
    .waterfall {
        width: 98%;
        margin: 10rpx auto;
        display: flex;
        padding: 0 4rpx;
        
        .left {
            width: 50%;
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        
        .right {
            width: 50%;
            display: flex;
            flex-direction: column;
            align-items: center;
        }
    }
</style>
