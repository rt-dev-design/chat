import ws from './ws.js';
import logMessages from '../utils/logmessages.js';
import store from '../store/index.js';
import JSONbig from 'json-bigint';

const onMessage = (message) => {
    const data = JSONbig.parse(message.body);
    if (store.state.trackUser.userIsCurrentlyOnPage === 'others') {
        store.dispatch('setTabbarLabelToTrue');
    } 
    else if (store.state.trackUser.userIsCurrentlyOnPage === 'chat') {
        store.dispatch('refreshChats');
    } 
    else {
        if (store.state.message.theOtherUser.id !== data.senderId.toString() || store.state.message.thisUser.id !== data.recipientId.toString()) {
            store.dispatch('setTabbarLabelToTrue');
        } 
        else {
            store.dispatch('appendMessageFromStompConnection', data);
            store.dispatch('toggleScrollSwitch');
        }
    }
};

const onConnect = (frame) => {
    logMessages(
        "chatapp.js",
        "connect success",
        frame,
        "userId",
        uni.getStorageSync('user').id
    );
    const userId = uni.getStorageSync('user').id;
    ws.send('/app/user-connect', JSON.stringify({
        id: userId
    }));
    uni.$H.post('/api/chat/unread', {
        id: userId
    }).then((response) => {
        logMessages(
            "chatapp.js",
            "unread then",
            response,
            "trackUser",
            store.state.trackUser.userIsCurrentlyOnPage
        );
        
        if (response) {
            if (store.state.trackUser.userIsCurrentlyOnPage === 'others') {
                store.dispatch('setTabbarLabelToTrue');
            } else {
                store.dispatch('refreshChats');
            }
        }
    }).catch((error) => {
        logMessages(
            "chatapp.js",
            "unread catch",
            error
        );
    }).finally(() => {
        const user = uni.getStorageSync('user');
        ws.subscribe(`/user/${user.id}/queue/messages`, onMessage);
    }); 
};

const onError = (error) => {
    logMessages(
        "chatapp.js",
        "connect error",
        error
    );
};

const connect = () => {
    ws.connect(
        {},
        onConnect,
        onError
    );
};

const disconnect = () => {
    logMessages(
        "chatapp.js",
        "disconnect user",
        uni.getStorageSync('user')
    );
    ws.send('/app/user-disconnect', JSON.stringify({
        id: uni.getStorageSync('user').id
    }));
    ws.disconnect();
}

const sendChatMessage = (message) => {
    logMessages(
        "chatapp.js",
        "sending message...",
        message
    );
    ws.send('/app/chat', JSON.stringify({
        ...message
    }));
    
    store.dispatch('appendMessageFromStompConnection', message);
    console.log("add");
}

export default {
    connect,
    disconnect,
    sendChatMessage
};