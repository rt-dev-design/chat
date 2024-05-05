function getUserProfile() {
	return new Promise((resolve, reject) => {
		uni.getUserProfile({
			desc: '服务端必要的用户信息收集', // 声明获取用户个人信息后的用途，后续会展示在弹窗中，请谨慎填写
			success(user) {
				resolve(user);
			},
			fail(res) {
				reject(res);
			}
		})
	})
}

export default getUserProfile;