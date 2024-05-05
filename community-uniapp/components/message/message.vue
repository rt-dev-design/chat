<template>
    <view>
        <view 
            class="message" 
            :style="{
                flexDirection: isUserMe ? 'row-reverse' : 'row'
            }">
            <image 
                class="avatar"  
                @click="jumpToUserPage()" 
                :src="user.userAvatar"
                ></image>
            <view 
                v-if="message.type == 'text'"
                class="content"
                >{{ message.content }}</view>
            <view v-else-if="message.type == 'image'">
                <image
                	:lazy-load="true" 
                	mode="aspectFill" 
                	class="img-style" 
                	:src="message.content"
                	@tap.stop="previewImage(message.content, [message.content])"></image>
            </view>
        </view>
    </view>
</template>

<script>
    export default {
        props: {
            user: Object,
            message: Object,
            isUserMe: Boolean
        },
        data() {
            return {
                
            };
        },
        methods: {
            jumpToUserPage() {
            	uni.navigateTo({
            		url: '/pages/user/home?uid=' + this.user.id
            	});
            },
            previewImage(url, urls) {
            	uni.previewImage({
            		current: url, // 当前显示图片的http链接
            		urls: urls // 需要预览的图片http链接列表
            	});
            },
        }
    }
</script>

<style scoped lang="scss">
    .message {
        display: flex;
        padding: 18rpx 6rpx;
        background-color: #e8e8e8;
        
        .avatar {
            width: 70rpx;
            height: 70rpx;
            margin: 0 18rpx;
            border-radius: 45%;
        }
        
        .content {
            padding: 12rpx;
            background-color: white;
            overflow-wrap: break-word;
            max-width: 70%;
            border-radius: 20rpx;
            font-size: 32rpx;
        }
        
        .img-style {
        	width: 300rpx;
        	height: 300rpx;
        	border-radius: 10rpx;
        	overflow: hidden;
        }
        
    }
</style>