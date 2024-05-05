<template>
    <view>
       <view class="chat">
        	<image 
                class="user-avatar" 
                :style="{width: avatarWidth, height: avatarWidth}" 
                @click="jumpToUserPage()" 
                :src="chatView.theOtherUser.userAvatar"
                ></image>
            <view class="content-right">
                <view class="name-and-time">
                    <text 
                        class="user-name" 
                        :style="{
                            fontWeight: boldName ? 'bold' : 'normal',
                            fontSize: fontSize,
                        }">{{ chatView.theOtherUser.userName }}</text>
                    <text 
                        class="last-message-time" 
                        v-if="displayTime"
                        >{{ formattedCreateTime }}</text>
                </view>
                <view class="message-and-dot">
                    <text 
                        class="lastMessage"
                        >{{ 
                            (chatView.lastMessage.startsWith("https") || chatView.lastMessage.startsWith("http"))
                            ? "[分享了图片或文件]"
                            : (chatView.lastMessage.length <= 20 ? chatView.lastMessage : chatView.lastMessage.substring(0, 18) + "...") 
                        }}</text>
                    <view 
                        class="dot" 
                        v-show="chatView.thereAreNewMessages"
                        ></view>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
    import logMessages from '../../utils/logmessages';
    export default {
        props: {
            chatView: {
                type: Object,
                default: null,
            },
            // data
            user: Object,
            lastMessageTime: {
                type: String,
                default: ''
            },
            lastMessage: {
                type: String,
                default: ''
            },
            thereAreNewMessages: {
                type: Boolean,
                default: false
            },
            // style
            boldName: {
                type: Boolean,
                default: true
            },
            fontSize: {
                type: String,
                default: '16px'
            },
            briefTime: {
                type: Boolean,
                default: false
            },
            displayTime: {
                type: Boolean,
                default: true
            }, 
            avatarWidth: {
                type: String,
                default: '80rpx'
            }
        },
        computed: {
            formattedCreateTime() {
                return this.briefTime ? 
                this.formatBriefDatetime(this.chatView.lastMessageTime) :
                this.formatDatetime(this.chatView.lastMessageTime);
            }
        },
        data() {
            return {
                
            };
        },
        methods: {
            formatDatetime(datetimeString) {
                const datetime = new Date(datetimeString);
                
                const year = datetime.getFullYear();
                const month = String(datetime.getMonth() + 1).padStart(2, "0");
                const day = String(datetime.getDate()).padStart(2, "0");
                const hours = String(datetime.getHours()).padStart(2, "0");
                const minutes = String(datetime.getMinutes()).padStart(2, "0");
                
                // const formattedDatetime = `${year}-${month}-${day}  ${hours}:${minutes}`;
                const formattedDatetime = `${month}-${day}  ${hours}:${minutes}`;
                return formattedDatetime;
            },
            formatBriefDatetime(datetimeString) {
                const datetime = new Date(datetimeString);
                
                const year = datetime.getFullYear();
                const month = String(datetime.getMonth() + 1).padStart(2, "0");
                const day = String(datetime.getDate()).padStart(2, "0");
                
                const formattedDatetime = `${year}-${month}-${day}`;
                return formattedDatetime;
            },
            jumpToUserPage() {
            	uni.navigateTo({
            		url: '/pages/user/home?uid=' + this.user.id
            	});
            }
        }
    }
</script>

<style scoped lang="scss">
    .chat {
		display: flex;
        margin: 50rpx 30rpx;
        
        .user-avatar {
        	width: 80rpx;
        	height: 80rpx;
        	margin-right: 20rpx;
        	border-radius: 45%;
        }
        
        .content-right {
            flex: 1;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            
            .name-and-time {
                display: flex;
                justify-content: space-between;
                align-items: center;
                
                .user-name {
                    font-size: 16px;
                }
                
                .last-message-time {
                    font-size: 14px;
                }
            }
            
            .message-and-dot {
                display: flex;
                justify-content: space-between;
                align-items: center;
                
                .lastMessage {
                    font-size: 14px;
                }
                
                .dot {
                    background-color: red;
                    border-radius: 48%;
                    width: 20rpx;
                    height: 20rpx;
                }
                
            }
        }
	}
</style>