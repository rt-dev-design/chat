<template>
	<view class="outer-container" :style="{width: containerWidth}" v-if="content">
        <view
            class="image-or-text-container"
            :style="{
                height: containerHeight,
                width: containerWidth,
            }"> 
            <view v-if="content.images" style="width: 100%; height: 100%">
                <image
                    :src="content.images[0].url" 
                    mode="aspectFill" 
                    @load="adaptSize"
                    style="width: 100%; height: 100%"
                    ></image>
            </view>
            <view v-else class="text-container" style="width: 100%; height: 100%"> 
                <text class="inner-text">{{ content.content.length <= 32 ? content.content : content.content.substring(0, 31) + "..." }}</text>
            </view>
        </view>
        
        <view class="title-and-user">
            <view class="title">{{ content.title }}</view>
            <user-badge
                :user="content.user"
                :content-create-time="content.createTime"
                :bold-name="false"
                :fontSize="'15px'"
                :brief-time="true"
                :avatar-width="'45rpx'"
                :name-and-time-direction="'row'"
                ></user-badge>
        </view>
	</view>
</template>

<script>
    import logMessages from '../../utils/logmessages.js';
    let spanRatio = 0.47;
    export default {
        name:"adaptive-waterfall-cell",
        props: {
            content: Object
        },
        data() {
            return {
                containerHeight: parseInt(uni.getSystemInfoSync().windowWidth * spanRatio * 1.2) + 'px',
                containerWidth: parseInt(uni.getSystemInfoSync().windowWidth * spanRatio) + 'px',
            };
        },
        methods: {
            adaptSize(e) {
                const winWid = uni.getSystemInfoSync().windowWidth;
                const cw = winWid * spanRatio;
                const imgh = e.detail.height;
                const imgw = e.detail.width;
                const ratio = imgh / imgw;
                let ch;
                if (ratio >= 0.5 && ratio <= 1.618) {
                    ch = ratio * cw;
                } else if (ratio < 0.5) {
                    ch = 0.5 * cw;
                } else {
                    ch = 1.618 * cw;
                }
                this.containerWidth = parseInt(cw) + 'px';
                this.containerHeight = parseInt(ch) + 'px';
            },
        }
    }
</script>

<style lang="scss">
    .outer-container{
        box-sizing: border-box;
        background-color: white;
        border-radius: 15rpx;
        overflow: hidden;
        margin-bottom: 10rpx;
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        
        .image-or-text-container{
            display: flex;
            justify-content: center;
            align-items: center;
             
            .text-container {
                display: flex;
                justify-content: center;
                align-items: center;
                padding: 0 30rpx;
                
                .inner-text {
                    font-size: 40rpx;
                }
            }
        }
        
        .title-and-user {
            margin-bottom: 10rpx;
            margin-left: 12rpx;
            
            .title {
                font-size: 30rpx;
                font-weight: bold;
                color: dimgrey;
                margin: 15rpx 0;
            }
        }
    }
</style>