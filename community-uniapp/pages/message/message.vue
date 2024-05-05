<template>
    <view style="min-height: 1200rpx; background-color: #e8e8e8;">
        <view class="message-area">
            <block v-for="(item, index) in messages" :key="item.id">
                <message
                    :user="item.senderId == thisUser.id ? thisUser : theOtherUser"
                    :is-user-me="item.senderId == thisUser.id"
                    :message="item"
                    ></message>
            </block>
        </view>
        <view class="comment-input">
        	<textarea 
                fixed="true" 
                cursor-spacing="10"
                auto-height="true"
                v-model="messageToSend.content"
                :maxlength="-1"
                ></textarea>
        	<u-button 
                @click="takeAndSendMessage" 
                :custom-style="btnStyle" 
                style="border-radius: 0;"
                >发送</u-button>
            <view style="width: 20rpx;"></view>
            <u-icon name="plus-circle-fill" color="#55aaff" size="60rpx" @click="showPopup = true"></u-icon>
        </view>
        <u-popup v-model="showPopup" mode="bottom" closeable border-radius="20">
            <view class="box-inside-pop-up">
                <u-button type="primary" size="medium" @click="goToShareImagePage">分享图片</u-button>
            </view>
        </u-popup>
    </view>
</template>

<script>
    import logMessages from '../../utils/logmessages.js';
    import { mapActions, mapGetters } from 'vuex';
    import chatapp from '../../chatapp/chatapp.js';
    import store from '../../store/index.js';
    export default {
        data() {
            return {
                messageToSend: {
                    type: "text",
                    content: "",
                    senderId: -1,
                    recipientId: -1,
                    chatId: -1
                },
                btnStyle: {
                	color: "#fff",
                	backgroundColor: '#55aaff'
                },
                showPopup: false,
            };
        },
        computed: {
            ...mapGetters([
                'messages', 
                'thisUser', 
                'theOtherUser', 
                'scrollToBottomWhenSwitched'
            ]),
        },
        watch: {
            scrollToBottomWhenSwitched(newVal, oldVal) {
                this.scrollToBottom();
            }
        },
        async onLoad(options) {
            await this.refreshMessagePage(options.theOtherUsersId, options.chatId);
        },
        onShow() {
            this.setUserCurrentPageToMessage();
        },
        async onUnload() {
            await this.cleanUpAndLeaveMessagePage();
        },
        onHide() {
            this.setUserCurrentPageToOthers();
        },
        async onPullDownRefresh() {
            try {
                const before = store.state.message.messages.length;
                await this.fetchMoreOnPullDown();
                const after = store.state.message.messages.length;
                // 取到新消息并拼到数组前面后，将页面适当向下滑动
                const scrollDelta = (after - before) * 40;
                setTimeout(() => {
                   uni.pageScrollTo({
                       scrollTop: scrollDelta,
                   });
                }, 0);
            }
            finally {
                uni.stopPullDownRefresh();
            }
        },
        methods: {
            ...mapActions([
                'refreshMessagePageData', 
                'setUserCurrentPageToMessage', 
                'setUserCurrentPageToOthers',
                'toggleScrollSwitch',
                'clearMessagePageData',
                'updateLastPresentTime',
                'fetchMoreOnPullDown',
            ]),
            scrollToBottom() {
                setTimeout(() => {
                   console.log("scroll");
                   uni.pageScrollTo({
                       scrollTop: 9999,
                       duration: 150
                   });
                }, 0);
            },
            takeAndSendMessage() {
                if (!this.messageToSend.content)
                    return;
                const messageCopy = {};
                Object.assign(messageCopy, this.messageToSend);
                chatapp.sendChatMessage(messageCopy);
                this.scrollToBottom();
                this.messageToSend.content = '';
            },
            async refreshMessagePage(theOtherUsersId, chatId) {
                this.setUserCurrentPageToMessage();
                const data = await this.refreshMessagePageData({
                    theOtherUsersId: theOtherUsersId,
                    chatId: chatId,
                });
                this.messageToSend.senderId = uni.getStorageSync('user').id;
                this.messageToSend.recipientId = theOtherUsersId;
                this.messageToSend.chatId = chatId;
                this.scrollToBottom();
            },
            async cleanUpAndLeaveMessagePage() {
                this.setUserCurrentPageToOthers();
                await this.updateLastPresentTime();
                this.clearMessagePageData();
            },
            goToShareImagePage() {
                this.showPopup = false;
                uni.navigateTo({
                    url: `/pages/message/share-image?senderId=${this.messageToSend.senderId}&recipientId=${this.messageToSend.recipientId}&chatId=${this.messageToSend.chatId}`
                });
            },
        }
    }
</script>

<style lang="scss" scoped>
    .message-area {
        padding-bottom: 120rpx;
        background-color: #e8e8e8;
    }
	.comment-input {
		position: fixed;
		bottom: 0;
		width: 100%;
		background-color: floralwhite;
		padding: 20rpx;
		display: flex;
        align-items: center;
		z-index: 999;
        font-size: 45rpx;
        border-radius: 20rpx;
        border: solid lightgrey 3rpx;
	}
    .box-inside-pop-up {
        display: flex;
        justify-content: center;
        align-items: center;
        height: 300rpx;
    }
</style>
