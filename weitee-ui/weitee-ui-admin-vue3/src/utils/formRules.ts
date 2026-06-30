const { t } = useI18n()

// required field
export const required = {
  required: true,
  message: t('common.required')
}

export const usernamePattern = /^[\u4e00-\u9fa5a-zA-Z0-9]{1,10}$/

export const usernameRule = {
  pattern: usernamePattern,
  message: '\u8D26\u53F7\u652F\u6301\u6C49\u5B57\u3001\u5B57\u6BCD\u3001\u6570\u5B57\uFF0C\u957F\u5EA6\u4E3A 1-10 \u4F4D',
  trigger: 'blur'
}