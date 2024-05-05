<template>
	<view>
		<!--一张图片-->
		<block v-if="images.length == 1">
			<image 
				:lazy-load="true" 
				mode="aspectFill" 
				class="img-style-1" 
				:src="images[0]"
				@tap.stop="previewImage(images[0], images)"></image>
		</block>
		<!--二张图片-->
		<block v-if="images.length == 2">
			<view class="img-style-2">
				<image
					:lazy-load="true" 
					v-for="(imageItem, index2) in images" :key="index2"
					@tap.stop="previewImage(imageItem, images)" 
					mode="aspectFill"
					:src="imageItem"></image>
			</view>
		</block>
		<!--三张图片-->
		<block v-if="images.length == 3">
			<view class="img-style-3">
				<image 
					:lazy-load="true" 
					v-for="(imageItem, index2) in images" :key="index2"
					@tap.stop="previewImage(imageItem, images)" 
					mode="aspectFill"
					:src="imageItem"></image>
			</view>
		</block>
		<!--四张图片-->
		<block v-if="images.length == 4">
			<view class="img-style-4">
				<image 
					:lazy-load="true" 
					v-for="(imageItem, index2) in images" :key="index2"
					@tap.stop="previewImage(imageItem, images)" 
					mode="aspectFill"
					:src="imageItem"></image>
			</view>
		</block>
	</view>
</template>

<script>
	export default {
		name:"image-grid",
		props: {
			imageInfoList: {
                type: Array,
                default: function() {
                    return [];
                }
            }
		},
		computed: {
			images() {
				return this.imageInfoList.map(e => e.url);
			}
		},
		data() {
			return {
				
			};
		},
		methods: {
			/**
			 * 图片点击事件处理函数
			 * 隐藏应用，调出原生插件进行图片预览
			 * 
			 * @param {Object} url 当前图片 url
			 * @param {Object} urls 图片 url 数组
			 */
			previewImage(url, urls) {
				uni.previewImage({
					current: url, // 当前显示图片的http链接
					urls: urls // 需要预览的图片http链接列表
				});
			}
		}
	}
</script>

<style lang="scss">
	.img-style-1 {
		display: block;
		width: 100%;
		height: 600rpx;
		border-radius: 5px;
		overflow: hidden;
	}
		
	.img-style-2 {
		display: flex;
		image {
			margin: 5rpx;
			width: 100%;
			height: 305rpx;
		}
	}
	
	.img-style-3 {
		display: flex;
		flex-wrap: wrap;
		image {
			width: 31.3%;
			height: 200rpx;
			margin: 0.6%;
		}
	}
	
	.img-style-4 {
		display: flex;
		flex-wrap: wrap;
		image {
			width: 48%;
			height: 320rpx;
			margin: 0.5%;
		}
	}
</style>