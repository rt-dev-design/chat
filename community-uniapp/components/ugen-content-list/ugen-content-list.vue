<template>
	<view>
		<!-- 将本地的数据列表循环渲染到页面上 -->
		<block v-for="(item, index) in list" :key="item.id">
            <view @click="goToContentPage(item)">
                <view
                    class="divider"
                    ></view>
                <user-generated-content 
                    :content="list[index]"
                    @update:content="(newVal) => this.$emit('update:list', {index: index, newVal: newVal})"
                    :indent="indent"
                    :with-favor="withFavor"
                    :do-favor-api-url="doFavorApiUrl"
                    :do-thumb-api-url="doThumbApiUrl"
                    ></user-generated-content>
            </view>
		</block>
		
		<!-- 下面两种加载状态的其中之一，始终会显示在页面底部 -->
		<!-- 若云端和本地都没有数据，则显示暂无内容，这是一个边界情况 -->
		<block v-if="list.length === 0 && loadStatus == 'nomore'">
			<u-empty margin-top="100" text="暂无内容" mode="favor"></u-empty>
		</block>
		<!-- 若云端有内容，则不管本地内容如何，按照云端数据加载情况来显示 -->
		<!-- u-loadmore 组件的 status -->
		<!-- more 云端还有 加载更多 默认值 -->
		<!-- noMore 云端没了 没有更多了 -->
		<!-- loading 加载中 正在加载 -->
		<block v-else>
			<view style="margin: 30rpx 0;">
				<u-loadmore :status="loadStatus" />
			</view>
		</block>
	</view>
</template>

<script>
    export default {
        name:"ugen-content-list",
        emits: ['update:list'],
		props: {
			list: Array,
			loadStatus: String,
            contentPageUrl: {
                type: String,
                default: ''
            },
            indent: {
                type: Boolean,
                default: false
            },
            withFavor: {
                type: Boolean,
                default: true
            },
            doFavorApiUrl: String,
            doThumbApiUrl: String
		},
		data() {
			return {
            
			};
		},
		methods: {
			/**
			 * 贴子点击事件处理函数
			 * 跳转到贴子详情页
			 * 
			 * e 是贴子项
			 */
			goToContentPage(e) {
                if (this.contentPageUrl && this.contentPageUrl !== '') {
                    uni.navigateTo({
                    	url: this.contentPageUrl + '?id=' + e.id
                    });
                }
			}
		}
    }
</script>

<style lang="scss" scoped>
    .divider {
        margin: 0 20rpx;
        height: 1rpx;
        background-color: lightgrey;
    }
</style>