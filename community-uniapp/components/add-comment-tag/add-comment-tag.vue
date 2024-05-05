<template>
	<view>
		<view 
            @click.stop="this.$emit('tag-click')" 
            class="float-href-tag">
			<u-icon name="plus-circle" color="#ffffff" size="50"></u-icon>
		</view>
	</view>
</template>

<script>
    export default {
        name:"add-comment-tag",
        emits: ['tag-click'],
        data() {
            return {
                
            };
        }
    }
</script>

<style lang="scss" scoped>
	.float-href-tag {
		position: fixed;
		right: 20rpx;
		bottom: 180rpx;
		width: 100rpx;
		height: 100rpx;
		border-radius: 50%;
		background-color: #123456;
		box-shadow: 0 0 20rpx #999;
		display: flex;
		justify-content: center;
		align-items: center;
	}
</style>