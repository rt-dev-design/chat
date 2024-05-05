<template>
    <view class="container">
        <view class="title">{{ 
            briefContent ? 
            (title.length <= 32 ? title : title.substring(0, 31) + "...")
            : title
        }}</view>
        <rich-text class="content-body" :nodes="processedBody"></rich-text>
    </view>
</template>

<script>
    export default {
        name:"title-body-text-content",
        props: {
            title: String,
            body: String,
            briefContent: {
                type: Boolean,
                default: false
            }
        },
        computed: {
            processedBody() {
                return this.briefContent ? 
                       (this.body.length <= 32 ? this.body : this.body.substring(0, 31) + "...")
                       : this.body;
            }
        },
        data() {
            return {
                
            };
        }
    }
</script>

<style scoped lang="scss">
    .container {
        padding: 10rpx 10rpx;
    }
    
    .title {
        font-size: 36rpx;
        font-weight: bold;
    	margin: 20rpx 0rpx;
    }
    
    .content-body {
        font-size: 30rpx;
        margin: 10rpx 0rpx;
    	white-space: pre-wrap;
    }
</style>