<template>
    <view>
        <view class="user-badge">
        	<image class="user-avatar" :style="{width: avatarWidth, height: avatarWidth}" @click="jumpToUserPage()" :src="user.userAvatar"></image>
        	<view class="name-and-time" :style="{flexDirection: nameAndTimeDirection}">
        		<text class="user-name" :style="{
                    fontWeight: boldName ? 'bold' : 'normal',
                    fontSize: fontSize,
                    }">{{ user.userName }}</text>
            	<text class="create-time" v-if="displayTime">{{ formattedCreateTime }}</text>
            </view>
        </view>
    </view>
</template>

<script>
    import logMessages from '../../utils/logmessages';
    export default {
        name:"user-badge",
        props: {
            user: Object,
            contentCreateTime: String,
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
            },
            nameAndTimeDirection: {
                type: String,
                default: 'column'
            }
        },
        computed: {
            formattedCreateTime() {
                return this.briefTime ? 
                this.formatBriefDatetime(this.contentCreateTime) :
                this.formatDatetime(this.contentCreateTime);
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
                
                const formattedDatetime = `${year}-${month}-${day}  ${hours}:${minutes}`;
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
                logMessages(
                    "user-badge.vue",
                    "userId before jump",
                    this.user.id
                );
            	uni.navigateTo({
            		url: '/pages/user/home?uid=' + this.user.id
            	});
            }
        }
    }
</script>

<style scoped lang="scss">
    .user-badge {
		display: flex;
        
        .user-avatar {
        	width: 80rpx;
        	height: 80rpx;
        	margin-right: 20rpx;
        	border-radius: 45%;
        }

		.name-and-time {
            display: flex;
            flex-direction: column;
            justify-content: space-around;
            
            .user-name {
                font-size: 16px;
                margin-right: 15rpx;
            }
            
            .create-time {
                font-size: 14px;
            }
		}
	}
</style>