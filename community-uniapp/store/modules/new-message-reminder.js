import logMessages from '../../utils/logmessages.js';

const state = {
    tabbarLabel: false,
};

const getters = {
    isTabbarNewMessageLabelSet: (state) => (state.tabbarLabel)
};

const actions = {
    setTabbarLabelToTrue({ commit }) {
        commit('setLabel', true);
    },
    setTabbarLabelToFalse({ commit }) {
        commit('setLabel', false);
    },
};

const mutations = {
    setLabel: (state, value) => {
        state.tabbarLabel = value;
        logMessages("new-message-reminder.js", "tabbarLabel", state.tabbarLabel);
    }
};

export default {
    state,
    getters,
    actions,
    mutations
};
