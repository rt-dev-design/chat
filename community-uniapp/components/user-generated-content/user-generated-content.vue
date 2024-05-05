<template>
    <view>
        <view class="user-generated-content-container">
            <user-badge 
                :user="content.user"
                :content-create-time="content.createTime"
                ></user-badge>

            <view class="indent-container">
                <view class="indent-left" v-if="indent"></view>
                <view class="content-right">
                    <title-body-text-content 
                        :title="content.title" 
                        :body="content.content"
                        :brief-content="briefContent"
                        ></title-body-text-content>

                    <image-grid 
                        v-if="enableImageGrid"
                        :imageInfoList="content.images"
                        ></image-grid>

                    <thumb-and-favor 
                        v-if="enableThumbAndFavor"
                        :content="content" 
                        :withFavor="withFavor" 
                        @favor="doFavor"
                        @thumb="doThumb"></thumb-and-favor>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
    import logMessages from '../../utils/logmessages.js';
    export default {
        name: "user-generated-content",
        emits: ['update:content'],
        props: {
            content: Object,
            enableThumbAndFavor: {
                type: Boolean,
                default: true
            },
            enableImageGrid: {
                type: Boolean,
                default: true
            },
            withFavor: {
                type: Boolean,
                default: true
            },
            indent: {
                type: Boolean,
                default: false
            },
            doFavorApiUrl: String,
            doThumbApiUrl: String,
            briefContent: {
                type: Boolean,
                default: false
            }
        },
        data() {
            return {
                
            };
        },
        methods: {
            doThumb() {
                this.$H
                    .post(this.doThumbApiUrl, {
                        id: this.content.id
                    })
                    .then((res) => {
                        logMessages(
                            "user-generated-content.vue", 
                            "doThumb then res", 
                            res
                        );
                        if (res === 1 || res === -1) {
                            let newContent = this.content;
                            newContent.hasThumb = !newContent.hasThumb;
                            newContent.thumbNum += res;
                            this.$emit('update:content', newContent);
                        } else {
                            throw "错误：res === " + res;
                        }
                    })
                    .catch((err) => {
                        logMessages(
                            "user-generated-content.vue", 
                            "doThumb catch err", 
                            err
                        );
                        uni.$showMsg("出错了，请稍后重试");
                    });

            },
            doFavor() {
                this.$H
                    .post(this.doFavorApiUrl, {
                        id: this.content.id
                    })
                    .then((res) => {
                        logMessages(
                            "user-generated-content.vue", 
                            "doFavor then res", 
                            res
                        );
                        if (res === 1 || res === -1) {
                            let newContent = this.content;
                            newContent.hasFavour = !newContent.hasFavour;
                            newContent.favourNum += res;
                            this.$emit('update:content', newContent);
                        } else {
                            throw "错误：res === " + res;
                        }
                    })
                    .catch((err) => {
                        logMessages(
                            "user-generated-content.vue", 
                            "doThumb catch err", 
                            err
                        );
                        uni.$showMsg("出错了，请稍后重试");
                    });
            }
        }
    }
</script>

<style scoped lang="scss">
    .user-generated-content-container {
        padding: 20rpx;
        padding-top: 30rpx;
        background-color: #ffffff;

        .indent-container {
            display: flex;

            .indent-left {
                flex-grow: 0;
                width: 110rpx;
                height: 1rpx;
            }

            .content-right {
                flex-grow: 1;
            }
        }
    }
</style>