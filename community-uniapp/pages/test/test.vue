<template>
	<view>
       <view>{{ a }}</view>
       <button @click="toggleA">toggle</button>
	</view>
</template>

<script>
    import login from '../../utils/userlogin.js';
    import logMessages from '../../utils/logmessages.js';
    import lodash from 'lodash';
    import { mapGetters, mapActions } from 'vuex';
    
    let spanRatio = 0.45;
	export default {
		data() {
			return {
                ids: [],
                urls: [
                    "https://campushelp-1315041027.cos.ap-guangzhou.myqcloud.com/post/1761601594856394753/W2Fizc0o-0.jpg",
                    "https://campushelp-1315041027.cos.ap-guangzhou.myqcloud.com/post/1761601594856394753/f06JFW9T-1.jpg",
                    "https://campushelp-1315041027.cos.ap-guangzhou.myqcloud.com/post/1761601594856394753/rozio2iq-2.jpg",
                    "https://campushelp-1315041027.cos.ap-guangzhou.myqcloud.com/post/1761601594856394753/A4eSkOrI-3.jpg",
                    "https://campushelp-1315041027.cos.ap-guangzhou.myqcloud.com/post/1761601594856394753/KZ3iYTxa-4.jpg",
                ],
                containerHeight: parseInt(uni.getSystemInfoSync().windowWidth * spanRatio * 1.2) + 'px',
                containerWidth: parseInt(uni.getSystemInfoSync().windowWidth * spanRatio) + 'px',
                content: '这是一个中文字符串，它的长度很长，是一个很大的数，现在我要对其进行截断',
                diaplayImage: true,
                user: uni.getStorageSync('user'),
                lastMessageTime: "2024-03-26 06:22:34",
			};
		},
        computed: {
            ...mapGetters(['a']),
        },
        methods: {
            ...mapActions(['toggleA']),
            pushId() {
                this.ids.push('tempId' + lodash.uniqueId());
            },
            ensureLoggedIn() {
                login.ensureLoggedIn();
            },
            testIfLoggedIn() {
                login.testIfLoggedIn();
            },
            adaptSize(e) {
                const winWid = uni.getSystemInfoSync().windowWidth;
                const cw = winWid * spanRatio;
                const imgh = e.detail.height;
                const imgw = e.detail.width;
                const ratio = imgh / imgw;
                let ch;
                if (ratio >= 0.618 && ratio <= 1.618) {
                    console.log('1111');
                    ch = ratio * cw;
                } else if (ratio < 0.618) {
                    console.log('2222');
                    ch = 0.618 * cw;
                } else {
                    console.log('3333');
                    ch = 1.618 * cw;
                }
                this.containerWidth = parseInt(cw) + 'px';
                this.containerHeight = parseInt(ch) + 'px';
            },
        }
	}
</script>

<style lang="scss">
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
</style>
