<template>
    <view>
        <view class="menu-container">
            <block v-if="withFavor">
                <block v-if="content.hasFavour">
                    <view 
                        class="p-item" 
                        @click.stop="this.$emit('favor')">
                        <text class="iconfont icon-lujing" style="color: #d81e06;"></text>
                        <text class="number">{{ content.favourNum }}</text>
                    </view>
                </block>
                <block v-else>
                    <view 
                        class="p-item"
                        @click.stop="this.$emit('favor')">
                        <text class="iconfont icon-shoucang"></text>
                        <text class="number">{{ content.favourNum }}</text>
                    </view>
                </block>
            </block>
        	<block v-if="content.hasThumb">
        		<view 
                    class="p-item" 
                    @click.stop="this.$emit('thumb')">
        			<text class="iconfont icon-dianzan" style="color: #d81e06;"></text>
        			<text class="number">{{ content.thumbNum }}</text>
        		</view>
        	</block>
        	<block v-else>
        		<view 
                    class="p-item" 
                    @click.stop="this.$emit('thumb')">
        			<text class="iconfont icon-dianzan1"></text>
        			<text class="number">{{ content.thumbNum }}</text>
        		</view>
        	</block>
        </view>
    </view>
</template>

<script>
    import logMessages from '../../utils/logmessages.js';
    export default {
        name:"thumb-and-favor",
        emits: ['favor', 'thumb'],
        props: {
            content: Object,
            withFavor: {
                type: Boolean,
                default: true
            }
        },
        data() {
            return {
                
            };
        }, 
        methods: {
            
        }
    }
</script>

<style scoped lang="scss">
	.menu-container {
		margin-top: 20rpx;
        margin-bottom: 5rpx;
		display: flex;
		justify-content: space-around;
		font-size: 24rpx;
		color: #616161;

		.p-item {
			display: flex;
			align-items: center;
            margin-right: auto;

			.iconfont {
				font-size: 40rpx;
                margin-right: 20rpx;
			}
            
            .number {
                font-size: 30rpx;
            }

		}
	}
</style>