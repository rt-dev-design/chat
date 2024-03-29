function logMessages(name, ...messages) {
	console.log("页面/组件/模块" + name + "的日志：");
	for (const message of messages) {
		console.log(message);
	}
}

export default logMessages;