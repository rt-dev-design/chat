import logMessages from '../../utils/logmessages.js';

const state = {
    a: true,
};

const getters = {
    a: (state) => (state.a)
};

const actions = {
    toggleA({commit, state}) {
        logMessages(
            "test.js",
            "before commit",
            state.a
        ); 
        commit('toggleA');
        logMessages(
            "test.js",
            "after commit",
            state.a
        );
    }
};

const mutations = {
    toggleA: (state) => {state.a = !state.a}
};

export default {
    state,
    getters,
    actions,
    mutations
};
