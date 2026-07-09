<template>
  <el-form
    v-show="getShow"
    ref="formSmsResetPassword"
    :model="resetPasswordData"
    :rules="rules"
    class="login-form"
    label-position="top"
    label-width="120px"
    size="large"
  >
    <el-row class="mx-[-10px]">
      <el-col :span="24" class="px-10px">
        <el-form-item>
          <LoginFormTitle class="w-full" />
        </el-form-item>
      </el-col>
      <el-col :span="24" class="px-10px">
        <el-form-item prop="mobile">
          <el-input
            v-model="resetPasswordData.mobile"
            :placeholder="t('login.mobileNumberPlaceholder')"
            :prefix-icon="iconCellphone"
          />
        </el-form-item>
      </el-col>
      <Verify
        ref="verify"
        v-if="resetPasswordData.captchaEnable === 'true'"
        :captchaType="captchaType"
        :imgSize="{ width: '400px', height: '200px' }"
        mode="pop"
        @success="getSmsCode"
      />
      <el-col :span="24" class="px-10px">
        <el-form-item prop="code">
          <el-row :gutter="5" justify="space-between" style="width: 100%">
            <el-col :span="24">
              <el-input
                v-model="resetPasswordData.code"
                :placeholder="t('login.codePlaceholder')"
                :prefix-icon="iconCircleCheck"
              >
                <template #append>
                  <span
                    v-if="mobileCodeTimer <= 0"
                    class="getMobileCode"
                    style="cursor: pointer"
                    @click="getCode"
                  >
                    {{ t('login.getSmsCode') }}
                  </span>
                  <span v-else class="getMobileCode" style="cursor: pointer">
                    {{ mobileCodeTimer }}秒后可重新获取
                  </span>
                </template>
              </el-input>
            </el-col>
          </el-row>
        </el-form-item>
      </el-col>
      <el-col :span="24" class="px-10px">
        <el-form-item prop="password">
          <InputPassword
            v-model="resetPasswordData.password"
            :placeholder="t('login.passwordPlaceholder')"
            class="w-full"
            :strength="true"
          />
        </el-form-item>
      </el-col>
      <el-col :span="24" class="px-10px">
        <el-form-item prop="check_password">
          <InputPassword
            v-model="resetPasswordData.check_password"
            :placeholder="t('login.checkPassword')"
            class="w-full"
            :strength="true"
          />
        </el-form-item>
      </el-col>
      <el-col :span="24" class="px-10px">
        <el-form-item>
          <XButton
            :loading="loginLoading"
            :title="t('login.resetPassword')"
            class="w-full"
            type="primary"
            @click="resetPassword()"
          />
        </el-form-item>
      </el-col>
      <el-col :span="24" class="px-10px">
        <el-form-item>
          <XButton
            :loading="loginLoading"
            :title="t('login.backLogin')"
            class="w-full"
            @click="handleBackLogin()"
          />
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>

<script lang="ts" setup>
import { sendSmsCode, smsResetPassword } from '@/api/login'
import { required } from '@/utils/formRules'
import { useIcon } from '@/hooks/web/useIcon'
import LoginFormTitle from './LoginFormTitle.vue'
import { LoginStateEnum, useFormValid, useLoginState } from './useLogin'

defineOptions({ name: 'ForgetPasswordForm' })

const verify = ref()
const { t } = useI18n()
const message = useMessage()
const formSmsResetPassword = ref()
const loginLoading = ref(false)
const iconCellphone = useIcon({ icon: 'ep:cellphone' })
const iconCircleCheck = useIcon({ icon: 'ep:circle-check' })
const { validForm } = useFormValid(formSmsResetPassword)
const { handleBackLogin, getLoginState, setLoginState } = useLoginState()
const getShow = computed(() => unref(getLoginState) === LoginStateEnum.RESET_PASSWORD)
const captchaType = ref('blockPuzzle')
const mobileCodeTimer = ref(0)
const temporaryCaptchaDisabled = true

const validatePass2 = (_rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== resetPasswordData.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  mobile: [{ required: true, min: 11, max: 11, trigger: 'blur', message: '手机号长度为11位' }],
  password: [
    {
      required: true,
      min: 4,
      max: 16,
      validator: validatePass2,
      trigger: 'blur',
      message: '密码长度为4到16位'
    }
  ],
  check_password: [{ required: true, validator: validatePass2, trigger: 'blur' }],
  code: [required]
}

const resetPasswordData = reactive({
  captchaEnable: temporaryCaptchaDisabled ? 'false' : import.meta.env.VITE_APP_CAPTCHA_ENABLE,
  username: '',
  password: '',
  check_password: '',
  mobile: '',
  code: ''
})

const smsVO = reactive({
  mobile: '',
  captchaVerification: '',
  scene: 23
})

const getCode = async () => {
  if (resetPasswordData.captchaEnable === 'false') {
    await getSmsCode({})
  } else {
    verify.value.show()
  }
}

const getSmsCode = async (params) => {
  smsVO.captchaVerification = params.captchaVerification
  smsVO.mobile = resetPasswordData.mobile
  await sendSmsCode(smsVO).then(async () => {
    message.success(t('login.SmsSendMsg'))
    mobileCodeTimer.value = 60
    const msgTimer = setInterval(() => {
      mobileCodeTimer.value = mobileCodeTimer.value - 1
      if (mobileCodeTimer.value <= 0) {
        clearInterval(msgTimer)
      }
    }, 1000)
  })
}

const resetPassword = async () => {
  const data = await validForm()
  if (!data) return
  loginLoading.value = true
  await smsResetPassword(resetPasswordData)
    .then(async () => {
      message.success(t('login.resetPasswordSuccess'))
      setLoginState(LoginStateEnum.LOGIN)
    })
    .catch(() => {})
    .finally(() => {
      loginLoading.value = false
    })
}
</script>
