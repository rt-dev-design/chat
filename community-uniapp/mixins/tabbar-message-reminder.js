import { mapGetters } from 'vuex';
import logMessages from '../utils/logmessages';

export default {
    computed: {
        ...mapGetters([
            'isTabbarNewMessageLabelSet', 
            'thereAreNewMessagesInChats'
        ]),
    },
    onShow() {
        // 在每次页面刚展示的时候，设置徽标
        this.checkAndSetBadge();
    },
    watch: {
        // 监听 tabbarLabel 的设置情况是否变化
        isTabbarNewMessageLabelSet(newVal, oldVal) {
            this.checkAndSetBadge();
        },
        // 监听 chats 中的新消息情况是否变化
        thereAreNewMessagesInChats(newVal, oldVal) {
            this.checkAndSetBadge(newVal);
        },
    },
    methods: {
        checkAndSetBadge() {
            const toSet = (this.isTabbarNewMessageLabelSet || this.thereAreNewMessagesInChats);
            logMessages("mixin", "toSet", toSet);
            if (toSet) {
                uni.showTabBarRedDot({
                    index: 2,
                });
            }
            else {
                uni.hideTabBarRedDot({
                    index: 2,
                });
            }
        },
    },
}