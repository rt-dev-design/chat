<template>
	<view>
       <content-list-with-edit-and-delete
            :list="myPosts"
            @update:list="event => this.$set(this.myPosts, event.index, event.newVal)"
            :loadStatus="loadStatus"
            :do-thumb-api-url="doThumbApiUrl"
            :do-favor-api-url="doFavorApiUrl"
            :content-page-url="contentPageUrl"
            @delete="deletePost"
            @edit="editPost"
            :enable-image-grid="false"
            :enable-thumb-and-favor="false"
            :brief-content="true"
        ></content-list-with-edit-and-delete>
        <u-modal 
            v-model="showModal" 
            :title="modalTitle"
            :content="modalContent" 
            :showCancelButton="true"
            @confirm="modalConfirm"
            @cancel="modalCancel"
        ></u-modal>
	</view>
</template>

<script>
    import login from '../../utils/userlogin.js';
    import logMessages from '../../utils/logmessages.js';
    import refresh from '../../utils/refresh.js';
	export default {
		data() {
			return {
                showFirstTime: true,
                contentPageUrl: "/pages/post/post",
				myPosts: [],
				pages: 0,
				loadStatus: 'loadmore',
				request: {
					current: 1,
					pageSize: 10,
				},
                // 给主页上的贴子收藏和点赞
                doFavorApiUrl: "/api/post_favour/",
                doThumbApiUrl: "/api/post_thumb/",
                user: null,
                
                showModal: false,
                modalTitle: "不可逆操作",
                modalContent: "确定删除该贴子？",
                
                deleteIndex: -1,
			};
		},
		onLoad(options) {
            login.ensureLoggedIn();
            this.user = uni.getStorageSync('user');
            this.getMyPosts();
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
                    url: '/pages/user/user'
                });
            }
            this.user = uni.getStorageSync('user');
            if (refresh.refreshMyPostsPage) {
                uni.startPullDownRefresh();
                refresh.refreshMyPostsPage = false;
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
			this.getMyPosts();
		},
        onPullDownRefresh() {
        	this.request.current = 1;
        	this.myPosts = [];
        	this.getMyPosts();
        	uni.stopPullDownRefresh();
        },
		methods: {
			getMyPosts() {
				this.loadStatus = 'loading';
				this.$H
					.post('/api/post/list/page/vo', {
						...this.request,
                        userId: this.user.id
					})
					.then(res => {
						this.myPosts = [...this.myPosts, ...res.records];
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
			},
            deletePost(index) {
                this.deleteIndex = index;
                this.showModal = true;
                logMessages(
                    "post.vue",
                    "delete index", 
                    index
                );
            },
            editPost(index) {
                uni.navigateTo({
                    url: '/pages/post/edit?post=' + encodeURIComponent(JSON.stringify(this.myPosts[index]))
                });
            },
            async modalConfirm() {
                console.log(this.deleteIndex);
                try {
                    const res = await this.$H.post('/api/post/delete-post-logically', {
                        id: this.myPosts[this.deleteIndex].id
                    });
                    this.myPosts.splice(this.deleteIndex, 1);
                    logMessages(
                        "post.vue",
                        "modalConfirm res",
                        res
                    );
                } catch(err) {
                    logMessages(
                        "post.vue",
                        "modalConfirm err",
                        err
                    );
                } finally {
                    this.deleteIndex = -1;
                }
            },
            modalCancel() {
                this.deleteIndex = -1;
            }
		}
	}
</script>

<style lang="scss">
	page {
		background-color: #f5f5f5;
	}
</style>
