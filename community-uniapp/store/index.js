import Vue from 'vue';
import Vuex from 'vuex';
import trackUser from './modules/track-user.js';
import message from './modules/message.js';
import chat from './modules/chat.js';
import newMessageReminder from '../store/modules/new-message-reminder.js';
import test from './modules/test.js';

Vue.use(Vuex);

const store = new Vuex.Store({
    modules: {
        trackUser,
        message,
        chat,
        newMessageReminder,
        test
    }
});

export default store;
