import logMessages from '../../utils/logmessages.js';

const state = {
    chats: [],
    // 用户的按最新消息时间逆序的一页聊天的请求
    // 客户端在发请求还需要加上当前用户的userId
    chatsRequest: {
      current: 1,
      pageSize: 10,
      sortField: "lastMessageTime",
      sortOrder: "descend"
    },
    pages: 0,
};

const getters = {
    chats: (state) => (state.chats),
    thereAreNewMessagesInChats: (state) => {
        const chats = state.chats;
        for (let i = 0; i < chats.length; i ++) {
            if (chats[i].thereAreNewMessages) return true;
        }
        return false;
    },
};

const actions = {
    async fetchChats({ state, commit, dispatch }) {
        try {
            const response = await uni.$H.post('/api/chat/chat-vos-page', {
                ...state.chatsRequest,
                userId: uni.getStorageSync('user').id
            });
            commit('setPages', response.pages);
            commit('setChatsRequest', {
                current: parseInt(response.current),
                pageSize: 10,
                sortField: state.chatsRequest.sortField,
                sortOrder: state.chatsRequest.sortOrder
            });
            return response.records;
        } 
        catch(error) {
            logMessages("chat.js", "fetchChats error", error);
        }
    },
    async refreshChats({ state, commit, dispatch }) {
        const freshChatsRequest = {
            current: 1,
            pageSize: 10,
            sortField: "lastMessageTime",
            sortOrder: "descend", 
        };
        commit('setChatsRequest', freshChatsRequest);
        try {
            const newChats = await dispatch('fetchChats');
            commit('setChats', newChats);
        } 
        catch(error) {
            logMessages("chat.js", "refreshChats error", error);
        }
    },
    async loadMoreChats({ state, commit, dispatch }) {
        if (parseInt(state.chatsRequest.current) >= parseInt(state.pages))
        	return;
        console.log('loadMoreChats');
        const newChatsRequest = {
            current: parseInt(state.chatsRequest.current) + 1,
            pageSize: 10,
            sortField: "lastMessageTime",
            sortOrder: "descend", 
        };
        commit('setChatsRequest', newChatsRequest);
        try {
            const newChats = await dispatch('fetchChats');
            commit('setChats', [...state.chats, ...newChats]);
        } 
        catch(error) {
            logMessages("chat.js", "refreshChats error", error);
        }
    },
    
};

const mutations = {
    setChats: (state, newChats) => {
        state.chats = newChats
    },
    setChatsRequest: (state, newChatsRequest) => {
        state.chatsRequest = newChatsRequest
    },
    setPages: (state, newPages) => {
        state.pages = newPages
    },
};

export default {
    state,
    getters,
    actions,
    mutations
};
