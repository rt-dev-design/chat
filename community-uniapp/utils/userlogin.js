import logMessages from './logmessages.js'
/**
 * 用session请求后端的获取当前登录用户的接口
 * 
 * 若未登录或者登录失效，则后端返回40001，会被前端响应拦截器拦截，跳转到登录界面
 * 若登录有效，则存储最新的用户视图
 */
async function ensureLoggedIn() {
    try {
        const res = await uni.$H.get('/api/user/get/login');
        uni.setStorageSync('user', res);
        return res;
    } catch(err) {
        logMessages(
            "userlogin.js",
            "ensureLoggedIn() err",
            err
        );
    }
}

/**
 * 用session请求后端接口测试是否登录
 * 
 * 若未登录或者登录失效，则后端返回null
 * 若登录有效，则后端返回用户视图，前端存储最新的用户视图
 */

async function testIfLoggedIn() {
    try {
        const res = await uni.$H.get('/api/user/get/login-or-null');
        if (res !== null) uni.setStorageSync('user', res);
        return res;
    } catch(err) {
        logMessages(
            "userlogin.js",
            "testIfLoggedIn() err",
            err
        );
    }
}

export default {
    ensureLoggedIn,
    testIfLoggedIn,
}