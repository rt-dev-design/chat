<template>
    <view>
        <ugen-content-list
        :list="latestPosts"
        @update:list="event => this.$set(this.latestPosts, event.index, event.newVal)"
        :loadStatus="loadStatus"
        :content-page-url="contentPageUrl"
        :do-thumb-api-url="doThumbApiUrl"
        :do-favor-api-url="doFavorApiUrl"
        ></ugen-content-list>
        <view style="width: 1rpx;height: 20rpx;"></view>
    </view>
</template>

<script>
    export default {
        data() {
            return {
                showFirstTime: true,
                contentPageUrl: "/pages/post/post",
                latestPosts: [],
                pages: 0,
                loadStatus: 'loadmore',
                request: {
                	current: 1,
                	pageSize: 10,
                    userId: -1,
                },
                // 给主页上的贴子收藏和点赞
                doFavorApiUrl: "/api/post_favour/",
                doThumbApiUrl: "/api/post_thumb/"
            };
        },
        onLoad() {
            this.request.userId = uni.getStorageSync('user').id;
        	this.getLastPost();
        },
        onPullDownRefresh() {
        	this.request.current = 1;
        	this.latestPosts = [];
        	this.getLastPost();
        	uni.stopPullDownRefresh();
        },
        methods: {
        	getLastPost() {
        		this.loadStatus = 'loading';
        		this.$H
        			.post('/api/post_favour/list/page', {
        				...this.request
        			})
        			.then(res => {
        				this.latestPosts = [...this.latestPosts, ...res.records];
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
    }
</script>

<style lang="scss">

</style>
