<template>
    <view class="login">
        <text class="txt1">应用申请获取以下权限</text>
        <text class="txt2">获取你的头像和昵称信息</text>
        <button class="btn-login" type="primary" @click="getUserProfile">授权登录</button>
        <text class="txt3" @click="goBackHome">暂不登录</text>
    </view>
</template>

<script>
    import logMessages from '../../utils/logmessages.js';
    import login from '../../utils/userlogin.js';
    import chatapp from '../../chatapp/chatapp.js';
    export default {
        data() {
            return {
                avatarUrl: ''
            };
        },
        methods: {
            goBackHome() {
                uni.reLaunch({
                    url: '/pages/index/index'
                });
            },

            async getUserProfile() {
                let res = null;
                uni.$getUserProfile().then(user => {
                    res = user;
                    logMessages("weixin.vue",
                        "getUserProfile then user",
                        res
                    );
                    this.getAndStoreToken(res);
                }).catch(err => {
                    logMessages("weixin.vue",
                        "getUserProfile catch err",
                        err
                    )
                    return uni.$showMsg('授权登录失败')
                });
            },

            async getAndStoreToken(userProfile) {
                const [err, res] = await uni.login();
                if (err || res.errMsg !== 'login:ok') {
                    logMessages("weixin.vue", "uni.login", err, res.errMsg);
                    return uni.$showMsg('出错了，登录失败，请稍后重试');
                }
                
                const request = {
                    code: res.code,
                    avatar: userProfile.userInfo.avatarUrl,
                    nickname: userProfile.userInfo.nickName
                }
                
                logMessages("weixin.vue",
                    "request",
                    request
                );

                this.$H
                    .post('/api/user/login/wx-mini', request)
                    .then((response) => {
                        logMessages("weixin.vue",
                            "post login then response",
                            response
                        );
                        uni.removeStorageSync('token');
                        uni.setStorageSync('token', response.header["Set-Cookie"]);
                        uni.setStorageSync('user', response.data.data);
                        uni.$showMsg("登录成功，即将跳转");
                        setTimeout(() => {
                            uni.navigateBack({
                                success: () => {
                                    chatapp.connect();
                                }
                            });
                        }, 2000)
                    })
                    .catch((err) => {
                        uni.$showMsg("登录失败，请稍后重试");
                        logMessages("weixin.vue",
                            "post login catch",
                            err
                        );
                    });
                    
            }
        }
    }
</script>

<style lang="scss">
    .btn-login {
        width: 90%;
        border-radius: 100px;
    }

    .login {
        display: flex;
        flex-direction: column;
        padding: 100rpx;
    }

    .login .txt1 {
        font-size: 12pt;
        margin-bottom: 20rpx;
        text-align: center;
    }

    .login .txt2 {
        font-size: 12pt;
        color: #999;
        margin-bottom: 50rpx;
        text-align: center;
    }

    .login .txt3 {
        font-size: 10pt;
        color: #8c8c8c;
        margin-top: 30rpx;
        text-align: center;
    }
</style>