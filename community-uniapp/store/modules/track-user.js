import logMessages from '../../utils/logmessages.js';

const state = {
    userIsCurrentlyOnPage: "others",
    // 或 "message", 或 "chat"
    // 有对应的getters和actions, 在页面的钩子和Stomp模块里调用
    // 用户在chat页 <=>  userIsCurrentlyOnPage === "chat"
    // 用户在message页 <=>  userIsCurrentlyOnPage === "message"
};

const getters = {
    userCurrentPage: (state) => (state.userIsCurrentlyOnPage)
};

const actions = {
    setUserCurrentPageToOthers({commit}) {
        commit('setUserIsCurrentlyOnPage', 'others');
    },
    setUserCurrentPageToChat({commit}) {
        commit('setUserIsCurrentlyOnPage', 'chat');
    },
    setUserCurrentPageToMessage({commit}) {
        commit('setUserIsCurrentlyOnPage', 'message');
    },
};

const mutations = {
    setUserIsCurrentlyOnPage: (state, page) => {
        logMessages(
            "track-user.js",
            "setUserIsCurrentlyOnPage page",
            page
        );
        state.userIsCurrentlyOnPage = page;
    }
};

export default {
    state,
    getters,
    actions,
    mutations
};
