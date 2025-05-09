<script setup>
import Footer from "@/views/web/layout/Footer.vue";
import {router} from "@/router/index.js";
import {onMounted, reactive, ref} from "vue";
import {getUserInfoAPI, userLogoutAPI} from "@/api/user/Auth.js";
import {dailySignAPI, getUserAssetAPI, rechargeAPI, withdrawAPI} from "@/api/user/UserInfo.js";
import {Message} from "@arco-design/web-vue";
import {
  IconUser,
  IconExport,
  IconUnorderedList,
  IconStar,
  IconHistory,
  IconSettings,
  IconRelation
} from '@arco-design/web-vue/es/icon';
import {useRoute} from 'vue-router';
import {useUserStore} from "@/stores/userStore.js";


// 存储用户信息
const userInfo = ref({});
const token = localStorage.getItem('token');
const isAdmin = ref(false);
const userAsset = ref({
  totalAmount: "",     //累计金额
  currentAmount: "",   //可用金额
  totalPoints: "",     //累计积分
  currentPoints: ""    //可用积分
});

onMounted(() => {
  // 设置初始激活菜单
  activeMenu.value = route.path;

  // 从 token 中解析用户角色
  const tokenParts = token.split('.');
  const claims = JSON.parse(atob(tokenParts[1]));
  isAdmin.value = claims.claims.role.includes('superAdmin') || claims.claims.role.includes('admin');

  //获取用户登录信息
  try {
    const token = localStorage.getItem('token');
    if (token) {
      getUserInfoAPI().then(res => {
        if (res.code === 200) {
          userInfo.value = res.data;
          // 更新本地存储的用户信息
          localStorage.setItem('userInfo', JSON.stringify(res.data));
        } else {
          Message.error('获取用户信息失败');
        }
      });

      //获取用户资产
      getUserAssetAPI().then(res => {
        userAsset.value = res.data;
      })
    }
  } catch (error) {
    console.error('获取用户信息错误:', error);
    Message.error('获取用户信息失败');
  }


});


//用户信息菜单数据
const menuItems = [
  {name: '个人资料', icon: IconUser, url: '/userInfo'},
  {name: '我的订单', icon: IconUnorderedList, url: '/userInfo/order'},
  {name: '我的收藏', icon: IconStar, url: '/userInfo/collection'},
  {name: '浏览历史', icon: IconHistory, url: '/userInfo/history'},
  {name: '资产流动', icon: IconRelation, url: '/userInfo/assetFlows'},
  {name: '账号设置', icon: IconSettings, url: '/userInfo/settings'}
];

// 添加当前激活菜单状态
const route = useRoute();
const activeMenu = ref(route.path);

// 点击菜单项
const onClickMenuItem = (item) => {
  if (item.url) {
    activeMenu.value = item.url;
    router.push(item.url);
  } else {
    Message.warning('该菜单项没有配置跳转地址');
  }
};

// 添加防抖函数
const debounce = (fn, delay) => {
  let timer = null;
  return function (...args) {
    if (timer) clearTimeout(timer);
    timer = setTimeout(() => {
      fn.apply(this, args);
    }, delay);
  };
};

// 用户签到
const dailySign = debounce(() => {

  const token = localStorage.getItem('token');
  if (token) {
    dailySignAPI().then(res => {
      if (res.code === 200) {
        Message.success(res.data);
        //更新用户资产
        getUserAssetAPI().then(res => {
          userAsset.value = res.data;
        });
      } else {
        Message.error(res.msg);
      }
    }).finally(() => {
    });
  }
}, 700); // 防抖延迟

// 充值相关
const rechargeVisible = ref(false);
const rechargeAmount = ref(0); // 修改为数字类型的初始值

const handleRecharge = () => {
  rechargeVisible.value = true;
};

const confirmRecharge = () => {
  if (!rechargeAmount.value || isNaN(rechargeAmount.value)) {
    Message.error('请输入有效的充值金额');
    return;
  }

  rechargeAPI({
    amount: Number(rechargeAmount.value)
  }).then(res => {
    if (res.code === 200) {
      Message.success('充值成功');
      rechargeVisible.value = false;
      rechargeAmount.value = 0;  // 重新初始化
      // 刷新
      window.location.reload();
    } else {
      Message.error(res.msg || '充值失败');
    }
  });
};

// 提现相关
const withdrawVisible = ref(false);
const withdrawAmount = ref(0);
const withdrawAccount = ref('');

const handleWithdraw = () => {
  withdrawVisible.value = true;
};

const confirmWithdraw = () => {
  if (!withdrawAmount.value || isNaN(withdrawAmount.value)) {
    Message.error('请输入有效的提现金额');
    return;
  }
  if (!withdrawAccount.value) {
    Message.error('请输入提现账号');
    return;
  }

  withdrawAPI({
    amount: Number(withdrawAmount.value),
    account: withdrawAccount.value
  }).then(res => {
    if (res.code === 200) {
      Message.success('提现申请已提交');
      withdrawVisible.value = false;
      withdrawAmount.value = 0;
      withdrawAccount.value = '';
      // 刷新
      window.location.reload();
    } else {
      Message.error(res.msg || '提现失败');
    }
  });
};

// 获取 store 实例
const userStore = useUserStore();

// 退出登录
const handleLogout = () => {
  const token = localStorage.getItem('token');
  if (token) {
    userStore.logout();
  } else {
    Message.warning('您尚未登录');
    router.push('/login');
  }
};
</script>

<template>

  <div class="user-info">
    <a-layout-header class="header">
      <div class="header-left" @click="router.push('/')">
        故梦阑珊
      </div>

      <div class="header-right">
        <template v-if="token">
          <a-dropdown position="bottom" trigger="hover">
            <a-space>
              <a-avatar :image-url="userInfo.userPic || '/image/gumeng.png'" :size="40"
                        style="cursor: pointer">
                <template #fallback>
                  <icon-user/>
                </template>
              </a-avatar>
              <span style="color: white">{{ userInfo.nickname || '用户' }}</span>
            </a-space>
            <template #content>
              <a-doption v-if="isAdmin" @click="router.push('/sys')">
                <icon-settings/>
                管理系统
              </a-doption>
              <a-doption @click="router.push('/visualization/trend')">
                <icon-settings/>
                非遗数据大屏
              </a-doption>
              <a-doption @click="handleLogout">
                <icon-export/>
                退出登录
              </a-doption>
            </template>
          </a-dropdown>
        </template>
      </div>

    </a-layout-header>

    <a-layout-content class="content">
      <div class="content-title">
        <div class="user-profile">
          <a-avatar :image-url="userInfo.userPic || '/image/gumeng.png'" :size="80">
            <template #fallback>
              <icon-user/>
            </template>
          </a-avatar>
          <div class="user-info-text">
            <h2>{{ userInfo.nickname || '用户' }}</h2>
          </div>
        </div>
        <div class="user-bag">
          <div class="user-bag-item">
            <h3>累计积分</h3>
            <p>{{ userAsset.totalPoints }}</p>
          </div>
          <div class="user-bag-item">
            <h3>累计金额</h3>
            <p>{{ userAsset.totalAmount }}</p>
          </div>
          <div class="user-bag-item">
            <h3>可用积分</h3>
            <p>{{ userAsset.currentPoints }}</p>
          </div>
          <div class="user-bag-item">
            <h3>可用余额</h3>
            <p>{{ userAsset.currentAmount }}</p>
          </div>
        </div>
        <div class="user-bag-button">
          <a-button @click="handleWithdraw">提现</a-button>
          <a-button @click="handleRecharge">充值</a-button>
          <a-button @click="dailySign">签到</a-button>
        </div>
      </div>

      <div class="content-container">
        <div class="content-list">
          <div
              v-for="item in menuItems"
              :key="item.url"
              :class="{ active: activeMenu === item.url }"
              class="menu-item"
              @click="onClickMenuItem(item)"
          >
            <component :is="item.icon"/>
            <span>{{ item.name }}</span>
          </div>
        </div>

        <div class="content-item">
          <RouterView></RouterView>
        </div>
      </div>
    </a-layout-content>

    <Footer class="footer"></Footer>
  </div>

  <!-- 充值弹窗 -->
  <a-modal
      v-model:visible="rechargeVisible"
      :cancel-button-props="{
        style: {
          color: '#C2101C',
          borderColor: '#C2101C'
        }
      }"
      :footer-style="{
        borderTop: '1px solid rgba(194, 16, 28, 0.1)',
        padding: '12px 24px',
        backgroundColor: 'rgba(194, 16, 28, 0.02)'
      }"
      :modal-style="{
        borderRadius: '10px',
        backgroundColor: '#FFF7E9',
        padding: '24px',
        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.15)',
      }"
      :ok-button-props="{
        style: {
          backgroundColor: '#C2101C',
          borderColor: '#C2101C'
        }
      }"
      :title-style="{
        color: '#C2101C',
        fontWeight: '500',
        fontSize: '18px',
        borderBottom: '2px solid rgba(194, 16, 28, 0.1)'
      }"
      cancel-text="取消"
      ok-text="确认充值"
      title="充值"
      @cancel="() => {
        rechargeVisible = false;
        rechargeAmount = 0;
      }"
      @ok="confirmRecharge"
  >
    <a-form :model="{ amount: rechargeAmount }">
      <a-form-item :style="{ color: '#C2101C' }" label="充值金额">
        <a-input-number
            v-model="rechargeAmount"
            :hover-style="{
              borderColor: '#C2101C',
            }"
            :precision="2"
            :style="{
              backgroundColor: '#FFF',
              borderColor: 'rgba(194, 16, 28, 0.3)',
            }"
            hide-button
            placeholder="请输入充值金额"
            style="width: 100%"
        />
      </a-form-item>
    </a-form>
  </a-modal>

  <!-- 提现弹窗 -->
  <a-modal
      v-model:visible="withdrawVisible"
      :cancel-button-props="{
        style: {
          color: '#C2101C',
          borderColor: '#C2101C'
        }
      }"
      :footer-style="{
        borderTop: '1px solid rgba(194, 16, 28, 0.1)',
        padding: '12px 24px',
        backgroundColor: 'rgba(194, 16, 28, 0.02)'
      }"
      :modal-style="{
        borderRadius: '10px',
        backgroundColor: '#FFF7E9',
        padding: '24px',
        boxShadow: '0 2px 8px rgba(0, 0, 0, 0.15)',
      }"
      :ok-button-props="{
        style: {
          backgroundColor: '#C2101C',
          borderColor: '#C2101C'
        }
      }"
      :title-style="{
        color: '#C2101C',
        fontWeight: '500',
        fontSize: '18px',
        borderBottom: '2px solid rgba(194, 16, 28, 0.1)'
      }"
      cancel-text="取消"
      ok-text="确认提现"
      title="提现"
      @cancel="() => {
        withdrawVisible = false;
        withdrawAmount = 0;
        withdrawAccount = '';
      }"
      @ok="confirmWithdraw"
  >
    <a-form :model="{ amount: withdrawAmount, account: withdrawAccount }">
      <a-form-item :style="{ color: '#C2101C' }" label="提现金额">
        <a-input-number
            v-model="withdrawAmount"
            :hover-style="{
              borderColor: '#C2101C',
            }"
            :precision="2"
            :style="{
              backgroundColor: '#FFF',
              borderColor: 'rgba(194, 16, 28, 0.3)',
            }"
            hide-button
            placeholder="请输入提现金额"
            style="width: 100%"
        />
      </a-form-item>
      <a-form-item :style="{ color: '#C2101C' }" label="提现账号">
        <a-input
            v-model="withdrawAccount"
            :hover-style="{
              borderColor: '#C2101C',
            }"
            :style="{
              backgroundColor: '#FFF',
              borderColor: 'rgba(194, 16, 28, 0.3)',
            }"
            placeholder="请输入提现账号"
        />
      </a-form-item>
    </a-form>
  </a-modal>

</template>

<style scoped>

.user-info {
  min-height: 100vh;
  max-width: 100vw;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
}

.header {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  height: 64px;
  width: 100vw;
  background-color: #C2101C;
}

.header-left {
  position: absolute;
  left: 24px; /* 增加左边距 */
  display: flex;
  align-items: center; /* 垂直居中 */
  color: white;
  height: 64px;
  font-size: 24px; /* 设置字体大小 */
  font-weight: bold; /* 加粗 */
  letter-spacing: 2px; /* 字间距 */
  font-family: 'Courier New', Courier, monospace; /* 设置字体 */
  border: none; /* 移除边框 */
}

.header-right {
  position: absolute;
  right: 24px; /* 增加右边距 */
  display: flex;
  align-items: center; /* 垂直居中 */
  color: white;
  height: 64px;
  font-size: 24px; /* 设置字体大小 */
  font-weight: bold; /* 加粗 */
  letter-spacing: 2px; /* 字间距 */
  font-family: 'Courier New', Courier, monospace; /* 设置字体 */
  border: none; /* 移除边框 */
}

.content {
  width: 100vw;
  min-height: calc(100vh - 128px);
  background-color: #F8F2E8;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.content-title {
  height: 16vh;
  width: 80vw;
  display: flex;
  justify-content: space-between;
  align-items: center;
  border-radius: 10px 10px 0 0;
  margin-top: 15px;
  background: linear-gradient(135deg, #C2101C 0%, #8B1F1F 100%);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.content-container {
  width: 80vw;
  height: 63vh;
  display: flex;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15); /* 添加阴影 */
}

.content-list {
  width: 15vw; /* 调整宽度 */
  padding: 20px 0;
  border-radius: 0 0 0 10px; /* 修改圆角 */
  background-color: #FFF7E9;
  display: flex;
  flex-direction: column;
  border-right: 1px solid rgba(0, 0, 0, 0.1);
}

.content-list .menu-item {
  padding: 15px 30px; /* 增加内边距 */
  cursor: pointer;
  transition: all 0.3s ease;
  color: #333;
  font-size: 16px;
  display: flex;
  align-items: center;
  gap: 12px; /* 增加图标和文字间距 */
}

.content-item {
  flex: 1; /* 让内容区域自适应剩余宽度 */
  padding: 20px; /* 添加内边距 */
  border-radius: 0 0 10px 0;
  background-color: #FFF7E9;
  overflow-y: auto; /* 添加滚动条 */
}

.content-list .menu-item:hover {
  background-color: rgba(194, 16, 28, 0.1); /* 使用主题色的淡化版本 */
  color: #C2101C; /* 使用主题色 */
}

.content-list .menu-item.active {
  background-color: rgba(194, 16, 28, 0.15);
  color: #C2101C;
  font-weight: 500;
}

.content-item {
  display: flex;
  width: 70vw;
  border-radius: 0 0 10px 0;
  background-color: #FFF7E9;
}

.user-profile {
  display: flex;
  align-items: center;
  padding: 20px 40px;
  color: white;
}

.user-info-text {
  margin-left: 20px;
}

.user-info-text h2 {
  margin: 0;
  font-size: 28px; /* 增大字号 */
  color: #FFE4E1; /* 使用偏暖的白色 */
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2); /* 添加文字阴影 */
  font-weight: 600; /* 加粗 */
  letter-spacing: 1px; /* 添加字间距 */
}

.user-bag {
  height: 16vh;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  gap: 50px;
  font-size: 20px;
  color: white;
  padding: 0 40px;
}

.user-bag-item {
  p {
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.user-bag h3 {
  margin: 0;
  font-size: 16px;
  color: rgba(255, 255, 255, 0.8);
  font-weight: normal;
}

.user-bag p {
  margin: 8px 0 0;
  font-size: 28px;
  font-weight: bold;
  background: linear-gradient(to right, #FFE4E1, #FFF);
  -webkit-background-clip: text;
  color: transparent;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-bag-button {
  display: flex;
  flex-direction: row; /* 改为水平排列 */
  gap: 20px; /* 调整按钮间距 */
  padding: 0 40px;
  margin-right: 20px; /* 添加右边距 */
}

.user-bag-button :deep(.arco-btn) {
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  transition: all 0.3s ease;
  padding: 8px 24px;
  font-size: 15px;
  border-radius: 20px;
  backdrop-filter: blur(5px);
}

.user-bag-button :deep(.arco-btn:hover) {
  background: #ffd700; /* 改为金色背景 */
  border-color: #ffd700; /* 边框改为金色 */
  color: #8B1F1F; /* 文字改为深红色，增加对比度 */
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.3); /* 添加金色阴影 */
}

.user-bag-button :deep(.arco-btn:active) {
  transform: translateY(0);
  background: #f8d000; /* 点击时稍微暗一点的金色 */
  border-color: #f8d000;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.menu-item :deep(svg) {
  font-size: 18px;
}


.footer {
  display: flex;
  bottom: 0;
}
</style>
