<template>
    <view>
       <view class="my-search-container">
            <view class="my-search-box">
                <view class="search-icon-and-placeholder">
                    <u-icon name="search" size="40"></u-icon>
                    <view class="the-input">
                        <u-input
                            placeholder="搜索"
                            :border="none"
                            v-model="value"
                            :clearable="false"
                            ></u-input>
                    </view>
                </view>
                <view class="close-icon" @click.stop="clearKeywordAndContent">
                    <u-icon name="close-circle" size="40"></u-icon>
                </view>
            </view>
        </view>
        

        <view
        	v-for="(item, index) in resultList"
        	:key="index"
            class="result-item"
            @click.stop="goToContent(item.id)"
            >
            <text class="result-text">{{ item.content }}</text>
        </view>

    </view>
</template>

<script>
    import logMessages from '../../utils/logmessages.js';
    import tabbarMessageReminder from '../../mixins/tabbar-message-reminder.js';
    export default {
        mixins: [tabbarMessageReminder],
        data() {
            return {
                value: '',
                resultList: [],
                
                inputChangeTimeout: null,
            };
        },
        watch: {
           value: {
              handler(newVal, oldVal) {
                 this.change(newVal);
              }
           }
        },
        methods: {
            bindClick(e) {
                logMessages(
                    "search.vue",
                    "bind",
                    e
                );
            },
            change(value) {
                clearTimeout(this.inputChangeTimeout);
                this.inputChangeTimeout = setTimeout(async () => {
                    if (this.value && this.value !== '') {
                        const { records } = await this.search();
                        this.resultList = [...records];
                    } else {
                        this.clearKeywordAndContent();
                    }
                }, 500);
            },
            clearKeywordAndContent() {
                this.value = '';
                this.resultList = [];
            },
            async search() {
                try {
                    const res = await this.$H.post(
                    '/api/post/search/page/vo', {
                        current: 1,
                        pageSize: 20,
                        searchText: this.value
                    });
                    logMessages(
                        "search.vue",
                        "then",
                        res
                    );
                    return res;
                } catch(err) {
                    logMessages(
                        "search.vue",
                        "catch",
                        err
                    );
                }
            },
            goToContent(id) {
                uni.navigateTo({
                	url: '/pages/post/post?id=' + id
                });
            }
        }
    }
</script>

<style lang="scss">
  .my-search-container {
    height: 50px;
    display: flex;
    align-items: center;
    padding: 0 10px;

    .my-search-box {
      height: 40px;
      background-color: #FFFFFF;
      border-radius: 18px;
      width: 100%;
      display: flex;
      justify-content: space-between;
      align-items: center;
      border: darkgrey solid;
      
      .search-icon-and-placeholder {
        flex: 1;
        display: flex;
        margin-left: 12px;
        
        .the-input {
          flex: 1;
          margin-left: 10px;
        }
      }
      
      .close-icon {
          padding: 20rpx;
      }
    }
  }
  
  .result-item {
      margin: 30rpx 30rpx;
      padding-bottom: 20rpx;
      border-bottom: #ebebeb solid;
      
      .result-text {
          margin-left: 20rpx;
          
          font-size: 30rpx;
      }
  }
  
</style>
