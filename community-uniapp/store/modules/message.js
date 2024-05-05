import logMessages from '../../utils/logmessages.js';
import lodash from 'lodash';

const state = {
    thisUser: null, // 此用户，sender
    theOtherUser: null, // 另一用户，recipient
    chatId: -1,  // chat的id，但是不重要
    messagesRequest: {
      current: 1,
      pageSize: 20,
      beforeTime: null,
    },
    // HTTP请求体，加上2个用户的id，用于请求message[0]之前的一页消息
    // 所以，beforeTime === message[0].createTime 需要一直成立
    
    messages: [],
    // 按时序的此聊天的消息
    
    enableStompMessageAppending: true,
    stompMessageBuffer: [],
    // HTTP请求方法会禁用Stomp消息中聊天消息的追加，等HTTP请求完成并追加完成后再启用
    // 当Stomp追加被禁用时，消息先进缓冲区
    // 最后再由HTTP请求的回调将缓冲区的消息筛选后追加到messages，同时清空缓冲区
    
    scrollToBottomWhenSwitched: true,
};

const getters = {
    messages: (state) => (state.messages),
    thisUser: (state) => (state.thisUser),
    theOtherUser: (state) => (state.theOtherUser),
    scrollToBottomWhenSwitched: (state) => (state.scrollToBottomWhenSwitched),
};

const actions = {
    toggleScrollSwitch({ commit }) {
        commit('toggleScrollSwitch');
    },
    appendMessageFromStompConnection({ state, commit, dispatch }, message) {
        if (!message.id) {
            message.id = 'tempId' + lodash.uniqueId();
        }
        if (state.enableStompMessageAppending) {
            logMessages("message.js", "append to messages", message);
            commit('appendOneToMessages', message);
        } 
        else {
            logMessages("message.js", "append to buffer", message);
            commit('appendOneToBuffer', message);
        }
    },
    async refreshMessagePageData(
        { state, commit, dispatch }, 
        { theOtherUsersId, chatId },
    ) {
        dispatch('clearMessagePageData');
        try {
            const user = await uni.$H.get(`/api/user/get/vo?id=${theOtherUsersId}`);
            if (!user.id || user.id <= 0) 
                throw user;
            commit('setTheOtherUser', user);
            commit('setChatId', chatId);
            
            const messagesResponse = await dispatch('fetchMessages');
            commit('prependMessages', messagesResponse.records);
            if (messagesResponse.records[0].createTime) 
                commit('setBeforeTime', messagesResponse.records[0].createTime);
            logMessages("message.js", "beforetime after request", messagesResponse.records[0], state.messagesRequest.beforeTime);
            return messagesResponse;
        } 
        catch(error) {
            logMessages("message.js", "refreshMessagePage error", error);
        }
    },
    async fetchMoreOnPullDown({ state, commit, dispatch }) {
        if (!state.messagesRequest.beforeTime) {
            if (!state.messages[0].createTime)
                return;
            else 
                commit('setBeforeTime', state.messages[0].createTime);
        }
        try {
            const messagesResponse = await dispatch('fetchMessages');
            if (messagesResponse.records[0].createTime) 
                commit('setBeforeTime', messagesResponse.records[0].createTime);
            logMessages("message.js", "beforetime after request", messagesResponse.records[0], state.messagesRequest.beforeTime);
            commit('prependMessages', messagesResponse.records);
            return messagesResponse;
        } 
        catch(error) {
            logMessages("message.js", "fetchMoreOnPullDown error", error);
        }
    },
    async fetchMessages({ state, commit, dispatch }) {
        commit('setEnableStompMessageAppending', false);
        try {
            // 这里的DTO是MessageQueryRequest
            const response = await uni.$H.post('/api/message/message-vos-page', {
                chatId: state.chatId,
                senderId: state.thisUser.id,
                recipientId: state.theOtherUser.id,
                ...state.messagesRequest,
            });
            return response;
        } 
        catch(error) {
            logMessages("message.js", "fetchMessages error", error);
        } 
        finally {
            const toAppend = state.stompMessageBuffer.filter((e) => {
                return e.recipientId === state.thisUser.id && e.senderId === state.theOtherUser.id;
            })
            logMessages("message.js", "stomp buffer", state.stompMessageBuffer, "toAppend",toAppend);
            commit('appendMessages', toAppend);
            commit('setEnableStompMessageAppending', true);
        }
    },
    clearMessagePageData({ state, commit, dispatch }) {
        commit('setThisUser', uni.getStorageSync('user'));
        commit('setTheOtherUser', null);
        commit('setChatId', -1);
        commit('setMessagesRequest', {
            current: 1,
            pageSize: 20,
            beforeTime: null,
        });
        commit('setMessages', []);
        commit('setEnableStompMessageAppending', true);
        commit('setStompMessageBuffer', []);
        // scrollToBottomWhenSwitched无需重置
    },
    async updateLastPresentTime({ state, commit, dispatch }) {
        try {
            const response = await uni.$H.post('/api/chat/update-last-present-time', {
                id: state.chatId,
                thisUsersId: state.thisUser.id,
                theOtherUsersId: state.theOtherUser.id,
            });
        } 
        catch(error) {
            logMessages("message.js", "updateLastPresentTime error", error);
        }
    },
};

const mutations = {
    appendOneToMessages: (state, message) => {
        state.messages.push(message)
    },
    appendOneToBuffer: (state, message) => {
        state.messages.push(message)
    },
    setEnableStompMessageAppending: (state, toSet) => {
        state.enableStompMessageAppending = toSet;
    },
    setMessages: (state, newMessages) => {
        state.messages = newMessages;
    },
    appendMessages: (state, toAppend) => {
        state.messages = [...state.messages, ...toAppend];
    },
    prependMessages: (state, toPrepend) => {
        state.messages = [...toPrepend, ...state.messages];
    },
    setMessagesRequest: (state, toSet) => {
        state.messagesRequest = toSet;
    },
    setThisUser: (state, toSet) => {
        state.thisUser = toSet;
    },
    setTheOtherUser: (state, toSet) => {
        state.theOtherUser = toSet;
    },
    setStompMessageBuffer: (state, toSet) => {
        state.stompMessageBuffer = toSet;
    },
    setBeforeTime: (state, toSet) => {
        state.messagesRequest.beforeTime = toSet;
    },
    toggleScrollSwitch: (state) => {
        state.scrollToBottomWhenSwitched = !state.scrollToBottomWhenSwitched;
    },
    setChatId: (state, toSet) => {
        state.chatId = toSet;
    },
};

export default {
    state,
    getters,
    actions,
    mutations
};
