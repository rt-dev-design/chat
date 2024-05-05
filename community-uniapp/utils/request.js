import config from './config.js';
import logMessages from './logmessages.js'
export default {
	request(options = {}) {
		return new Promise((resolve, reject) => {
			let url = options.url;
			options.originalUrl = url;
			if (url.indexOf("http://") == -1 && url.indexOf("https://") == -1) {
				options.url = config.domain + url;
			}
			options.complete = (response) => {
				const processedResponse = 
				this.afterRequest 
				&& typeof this.afterRequest === 'function' 
				&& this.afterRequest(options, response);
				
				let bizStatusCode = null;
				if (options.originalUrl !== '/api/user/login/wx-mini' 
				&& options.originalUrl !== '/api/user/login') {
					bizStatusCode = response.data.code;
				} else {
					bizStatusCode = processedResponse.data.code;
				}
				
				// 如果响应码不是 0, reject, 否则 resolve
                    
                if (bizStatusCode === 40100) {
                    uni.$showMsg("请先登录，即将跳转");
                    setTimeout(() => {
                        uni.navigateTo({
                            url: '/pages/login/weixin'
                        })
                    }, 1000);
                }
				else if (bizStatusCode !== 0) {
					reject(response.data);
				} else {
					resolve(processedResponse)
				}
			}
			this.beforeRequest && typeof this.beforeRequest === 'function' && this.beforeRequest(options)
			uni.request(options)
		})
	},

	post(url, data = {}, header = {}) {

		let options = {
			url: url,
			data: data,
			header: header,
			method: "POST"
		}

		return this.request(options);
	},

	get(url, data = {}, header = {}) {
		let options = {
			url: url,
			data: data,
			header: header
		}

		return this.request(options);
	},
	
	beforeRequest: null,
	afterRequest: null
};
