<template>
	<view v-if="post.id !== undefined">
        <user-generated-content
            :content="post"
            @update:content="newVal => this.post = newVal"
            :do-favor-api-url="postFavorUrl"
            :do-thumb-api-url="postThumbUrl"
            ></user-generated-content>

        <view class="divider">
            <text class="divider-text">评论区</text>
        </view>
        
        <ugen-content-list
        :list="commentList"
        @update:list="event => this.$set(this.commentList, event.index, event.newVal)"
        :loadStatus="loadStatus"
        :indent="true"
        :withFavor="false"
        :do-thumb-api-url="commentThumbUrl"
        ></ugen-content-list>
        <view style="width: 1rpx;height: 20rpx;"></view>
		
        <add-comment-tag @tag-click="navigateToAddCommentPage()"></add-comment-tag>
	</view>
</template>

<script>
    import logMessages from '../../utils/logmessages.js';
	export default {
		data() {
			return {
				postId: 0,
				post: {
                    // id: "",
                    // title: "",
                    // content: "",
                    // thumbNum: 0,
                    // favourNum: 0,
                    // userId: "",
                    // createTime: "",
                    // updateTime: "",
                    // tagList: [],
                    // user: {
                    //     id: "",
                    //     userName: "",
                    //     userAvatar: "",
                    //     userProfile: null,
                    //     userRole: "",
                    //     createTime: ""
                    // },
                    // hasThumb: null,
                    // hasFavour: null,
                    // images: []
                },
                postFavorUrl: '/api/post_favour/',
                postThumbUrl: '/api/post_thumb/',
                
				loadStatus: 'loadmore',
                pages: 10000,
                commentRequest: {
                    current: 1,
                    pageSize: 5
                },
                commentList: [
                //     {
                //     id: "",
                //     postId: "",
                //     content: "",
                //     thumbNum: 0,
                //     userId: "",
                //     createTime: "",
                //     updateTime: "",
                //     user: {},
                //     hasThumb: false,
                //     hasFavour: false,
                //     commentImages: []
                // },
                ],
                commentThumbUrl: '/api/help_comment_thumb/',
                showFirstTime: true,
			};
		},
		onLoad(options) {
			this.postId = options.id;
            this.getPost();
            this.getCommentList();
		},
        onShow() {
            if (this.showFirstTime) {
                logMessages("post.vue", "show post page for the first time");
                this.showFirstTime = false;
                return;
            }
            logMessages("post.vue", "show post");
            this.refreshCommentList();
        },
		onReachBottom() {
            if (parseInt(this.commentRequest.current) >= parseInt(this.pages)) {
                this.loadStatus = 'noMore';
                return;
            }
			this.commentRequest.current++;
			this.getCommentList();
		},
            
        onPullDownRefresh() {
            this.getPost();
            this.commentList = [];
            this.commentRequest.current = 1;
            this.getCommentList();
            uni.stopPullDownRefresh();
        },
		methods: {
			getCommentList() {
				this.loadStatus = 'loading';
				this.$H
					.post('/api/help_omment/list/page/vo', {
						postId: this.postId,
						...this.commentRequest
					})
					.then(res => {
						this.commentList = [...this.commentList, ...res.records];
                        logMessages("post.vue", "getCommentList then", this.commentList);
                        this.pages = res.pages;
                        if (this.pages <= this.commentRequest.current) this.loadStatus = 'noMore';
                        else this.loadStatus = "more";
                        this.commentList.map((v, i) => v.images = v.commentImages);
                        logMessages("post.vue", "commentList", this.commentList);
					})
                    .catch(err => {
                        logMessages("post.vue", "getCommentList catch", err);
                    });
			},
            
            refreshCommentList() {
                this.loadStatus = 'loading';
                this.commentRequest.current = 1;
                this.$H
                	.post('/api/help_omment/list/page/vo', {
                		postId: this.postId,
                		...this.commentRequest
                	})
                	.then(res => {
                		this.commentList = [...res.records];
                        logMessages("post.vue", "getCommentList then", this.commentList);
                        this.pages = res.pages;
                        if (this.pages <= this.commentRequest.current) this.loadStatus = 'noMore';
                        else this.loadStatus = "more";
                        this.commentList.map((v, i) => v.images = v.commentImages);
                        logMessages("post.vue", "commentList", this.commentList);
                	})
                    .catch(err => {
                        logMessages("post.vue", "getCommentList catch", err);
                    });
            },
			
			async getPost() {
				this.$H
					.get(`/api/post/get/vo?id=${this.postId}`)
					.then(res => {
                        this.post = res;
                        logMessages("post.vue", "getPost then post", this.post);
					})
                    .catch((err) => {
                        uni.$showMsg("加载数据失败，请重试");
                        logMessages("post.vue", "getPost catch", err);
                    });
			},
              
            navigateToAddCommentPage() {
                logMessages(
                    "post.vue",
                    "navigateToAddCommentPage id",
                    this.post.id
                );
                uni.navigateTo({
                    url:`/pages/comment/add-comment?post=`+ encodeURIComponent(JSON.stringify(this.post))
                });
            },
		}
	};
</script>

<style lang="scss" scoped>
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
    

</style>
