<template>
  <el-form class="login-content-form" size="large">
    <el-form-item class="login-animation1">
      <el-input v-model="state.ruleForm.username" :placeholder="$t('message.account.accountPlaceholder1')"
                autocomplete="off"
                clearable text>
        <template #prefix>
          <el-icon class="el-input__icon">
            <ele-User/>
          </el-icon>
        </template>
      </el-input>
    </el-form-item>
    <el-form-item class="login-animation2">
      <el-input
          v-model="state.ruleForm.password"
          :placeholder="$t('message.account.accountPlaceholder2')"
          :type="state.isShowPassword ? 'text' : 'password'"
          autocomplete="off"
      >
        <template #prefix>
          <el-icon class="el-input__icon">
            <ele-Unlock/>
          </el-icon>
        </template>
        <template #suffix>
          <i
              :class="state.isShowPassword ? 'icon-yincangmima' : 'icon-xianshimima'"
              class="iconfont el-input__icon login-content-password"
              @click="state.isShowPassword = !state.isShowPassword"
          >
          </i>
        </template>
      </el-input>
    </el-form-item>
    <el-form-item class="login-animation3">
      <el-col :span="15">
        <el-input
            v-model="state.ruleForm.verificationCode"
            :placeholder="$t('message.account.accountPlaceholder3')"
            autocomplete="off"
            clearable
            maxlength="4"
            text
        >
          <template #prefix>
            <el-icon class="el-input__icon">
              <ele-Position/>
            </el-icon>
          </template>
        </el-input>
      </el-col>
      <el-col :span="1"></el-col>
      <el-col :span="8">
        <el-button v-waves class="login-content-code">
          <el-image :src="state.captchaUrl" @click="changeCaptcha">
            <template #placeholder>加载中...</template>
          </el-image>
        </el-button>

      </el-col>
    </el-form-item>
    <el-form-item class="login-animation4">
      <el-button v-waves :loading="state.loading.signIn" class="login-content-submit" round type="primary"
                 @click="onSignIn">
        <span>{{ $t('message.account.accountBtnText') }}</span>
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script lang="ts" name="loginAccount" setup>
import {computed, onMounted, reactive} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {ElMessage} from 'element-plus';
import {useI18n} from 'vue-i18n';
import Cookies from 'js-cookie';
import {storeToRefs} from 'pinia';
import {useThemeConfig} from '/@/stores/themeConfig';
import {initFrontEndControlRoutes} from '/@/router/frontEnd';
import {initBackEndControlRoutes} from '/@/router/backEnd';
import {Session} from '/@/utils/storage';
import {formatAxis} from '/@/utils/formatTime';
import {NextLoading} from '/@/utils/loading';
import {useLoginApi} from '/@/api/login';

// 定义变量内容
const {t} = useI18n();
const storesThemeConfig = useThemeConfig();
const {themeConfig} = storeToRefs(storesThemeConfig);
const route = useRoute();
const router = useRouter();
const loginApi = useLoginApi();
const state = reactive({
  isShowPassword: false,
  ruleForm: {
    username: 'admin',
    password: '123123cC',
    verificationCode: '1234',
    captchaKey: '',
  },
  loading: {
    signIn: false,
  },
  captchaUrl: '',
});

// 时间获取
const currentTime = computed(() => {
  return formatAxis(new Date());
});
// 登录
const onSignIn = async () => {
  state.loading.signIn = true;

  loginApi.signIn(state.ruleForm).then(async res => {
    const accessToken = res.accessToken
    // 存储 token 到浏览器缓存
    Session.set('token', accessToken);
    // 模拟数据，对接接口时，记得删除多余代码及对应依赖的引入。用于 `/src/stores/userInfo.ts` 中不同用户登录判断（模拟数据）
    Cookies.set('userName', state.ruleForm.username);

    if (!themeConfig.value.isRequestRoutes) {
      // 前端控制路由，2、请注意执行顺序
      const isNoPower = await initFrontEndControlRoutes();
      signInSuccess(isNoPower);
    } else {
      // 模拟后端控制路由，isRequestRoutes 为 true，则开启后端控制路由
      // 添加完动态路由，再进行 router 跳转，否则可能报错 No match found for location with path "/"
      const isNoPower = await initBackEndControlRoutes();
      // 执行完 initBackEndControlRoutes，再执行 signInSuccess
      signInSuccess(isNoPower);
    }
  }).catch(() => {
    changeCaptcha()
  }).finally(() => {
    state.loading.signIn = false;
  })
};
// 登录成功后的跳转
const signInSuccess = (isNoPower: boolean | undefined) => {
  if (isNoPower) {
    ElMessage.warning('抱歉，您没有登录权限');
    Session.clear();
  } else {
    // 初始化登录成功时间问候语
    let currentTimeInfo = currentTime.value;
    // 登录成功，跳到转首页
    // 如果是复制粘贴的路径，非首页/登录页，那么登录成功后重定向到对应的路径中
    if (route.query?.redirect) {
      router.push({
        path: <string>route.query?.redirect,
        query: Object.keys(<string>route.query?.params).length > 0 ? JSON.parse(<string>route.query?.params) : '',
      });
    } else {
      router.push('/');
    }
    // 登录成功提示
    const signInText = t('message.signInText');
    ElMessage.success(`${currentTimeInfo}，${signInText}`);
    // 添加 loading，防止第一次进入界面时出现短暂空白
    NextLoading.start();
  }
  state.loading.signIn = false;
};

const changeCaptcha = async () => {
  const data = await loginApi.captcha();
  state.captchaUrl = 'data:image/jpg;base64,' + data.captchaImg
  state.ruleForm.captchaKey = data.captchaKey
}
onMounted(() => {
  // 初始化验证码
  changeCaptcha();
});
</script>

<style lang="scss" scoped>
.login-content-form {
  margin-top: 20px;
  @for $i from 1 through 4 {
    .login-animation#{$i} {
      opacity: 0;
      animation-name: error-num;
      animation-duration: 0.5s;
      animation-fill-mode: forwards;
      animation-delay: calc($i/10) + s;
    }
  }

  .login-content-password {
    display: inline-block;
    width: 20px;
    cursor: pointer;

    &:hover {
      color: #909399;
    }
  }

  .login-content-code {
    width: 100%;
    padding: 0;
    font-weight: bold;
    letter-spacing: 5px;
  }

  .login-content-submit {
    width: 100%;
    letter-spacing: 2px;
    font-weight: 300;
    margin-top: 15px;
  }
}
</style>
