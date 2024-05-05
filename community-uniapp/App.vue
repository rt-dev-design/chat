<script>
    import chatapp from './chatapp/chatapp.js';
    import login from './utils/userlogin.js';
	export default {
        onLaunch: async function() {
			console.log('App Launch')
            const res = await login.testIfLoggedIn();
            if (res) chatapp.connect();
		},
		onShow: async function() {
			console.log('App Show')
            const res = await login.testIfLoggedIn();
            if (res) chatapp.connect();
		},
		onHide: function() {
			console.log('App Hide')
		},
		onPageNotFound() {
			// 跳转到 404 页面：
			uni.redirectTo({
				url: "pages/error/error"
			});
		}
	}
</script>

<style lang="scss">
	/* 注意要写在第一行，同时给style标签加入lang="scss"属性 */
	@import "uview-ui/index.scss";
	@import "static/css/iconfont.css";
</style>