import App from './App';
import store from "./store";
import uView from "uview-ui";
import config from './utils/config.js';
import request from './utils/request.js';
import getUserProfile from './utils/userprofile.js';
import logMessages from 'utils/logmessages.js';


Vue.use(uView);
// 挂载Vuex
Vue.prototype.$store = store;
// 配置文件
Vue.prototype.$c = config;
// http请求
Vue.prototype.$H = request;

uni.$getUserProfile = getUserProfile;

uni.$showMsg = function(title = '数据请求失败！', duration = 2000) {
  uni.showToast({
    title,
    duration,
    icon: 'none'
  })
}
	
Vue.prototype.$H.beforeRequest = function(options) {
	// 如果不是对登录接口发起请求，就带上 token 
	if (options.url !== '/api/user/login/wx-mini' && options.url !== '/api/user/login') {
		options.header.Cookie = uni.getStorageSync('token');
	}
    // logMessages(
    //     "before request",
    //     "修改后的options: ",
    //     options
    // );
}

Vue.prototype.$H.afterRequest = function(request, response) {
	// 如果是请求登录接口，那么连头部一起返回，让上层处理
	if (request.originalUrl == '/api/user/login/wx-mini' || request.originalUrl == '/api/user/login') {
		return response;
	}
	// 如果是请求数据接口，那么只返回响应体
    // logMessages(
    //     "after request",
    //     "响应拦截器日志，识别到请求的是数据接口：",
    //     "原response: ",
    //     response
    // );
	const processedResponse = response.data.data;
    // logMessages("after request", "response: ", processedResponse);
	return processedResponse;
}

uni.$H = Vue.prototype.$H;

// #ifndef VUE3
import Vue from 'vue'
Vue.config.productionTip = false
App.mpType = 'app'
const app = new Vue({
    ...App,
	store
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
export function createApp() {
  const app = createSSRApp(App)
  return {
    app
  }
}
// #endif