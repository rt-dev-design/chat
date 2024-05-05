<template>
    <view>
        <view 
            class="go-to-login-page" 
            v-if="!userIsLoggedIn" 
            style="display: flex; justify-content: center; align-items: center; height: 1000rpx;"
            ><button type="primary" @click="goToLoginPage">前往登录</button>
        </view>
        <view class="chat-page" v-else>
            <view 
                @click="goToMessagePage(item.theOtherUser.id, item.id)"
                v-for="(item, index) in chats"
                :key="item.id">
                <chat :chatView="item"></chat>  
            </view>
        </view>
    </view>
</template>

<script>
    import login from '../../utils/userlogin.js';
    import { mapGetters, mapActions } from 'vuex';
    import logMessages from '../../utils/logmessages.js';
    import tabbarMessageReminder from '../../mixins/tabbar-message-reminder.js';
    export default {
        mixins: [tabbarMessageReminder],
        async onReady() {
            await this.refreshChatPage();
        },
        async onShow() {
            await this.refreshChatPage();
        },
        async onPullDownRefresh() {
            await this.refreshChatPage();
            uni.stopPullDownRefresh();
        },
        onHide() {
            this.setUserCurrentPageToOthers();
        },
        onUnload() {
            this.setUserCurrentPageToOthers();
        },
        onReachBottom() {
            this.loadMoreChats();
        },
        data() {
            return {
                userIsLoggedIn: false,
            };
        },
        computed: {
            ...mapGetters(['chats']),
        },
        methods: {
            ...mapActions([
                'refreshChats', 
                'setUserCurrentPageToChat', 
                'setUserCurrentPageToOthers', 
                'setTabbarLabelToFalse',
                'loadMoreChats',
            ]),
            async refreshChatPage() {
                this.setUserCurrentPageToChat();
                this.setTabbarLabelToFalse();
                const loginResult = await login.testIfLoggedIn();
                this.userIsLoggedIn = loginResult ? true : false;
                if (loginResult) 
                    this.refreshChats();
            },
            goToMessagePage(theOtherUsersId, chatId) {
                uni.navigateTo({
                    url: `/pages/message/message?theOtherUsersId=${theOtherUsersId}&chatId=${chatId}`
                });
            },
            goToLoginPage() {
                uni.navigateTo({
                    url: '/pages/login/weixin'
                });
            },
        },
    }
</script>

<style lang="scss">
    .chat-page {
        margin-top: -40rpx;
        padding-bottom: 10rpx;
    }
</style>
