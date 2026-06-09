import { defineConfig, toEscapedSelector as e, presetUno } from 'unocss'
// import transformerVariantGroup from '@unocss/transformer-variant-group'

export default defineConfig({
  // ...UnoCSS options
  theme: {
    // ── ERP 设计 Token → UnoCSS Theme ──────────────────────
    // 让 Tailwind 工具类直接使用 ERP 设计系统的值
    colors: {
      erp: {
        primary: {
          50:  '#EBF5FF',
          100: '#D6EBFF',
          200: '#ADD6FF',
          300: '#85C1FF',
          400: '#5CACFF',
          500: '#3396FF',
          600: '#1677FF',
          700: '#0D66E0',
          800: '#0A52B8',
          900: '#073D8F',
        },
        success: {
          50:  '#ECFDF5',
          100: '#D1FAE5',
          200: '#A7F3D0',
          300: '#6EE7B7',
          400: '#34D399',
          500: '#10B981',
          600: '#059669',
          700: '#047857',
          800: '#065F46',
          900: '#064E3B',
        },
        warning: {
          50:  '#FFFBEB',
          100: '#FEF3C7',
          200: '#FDE68A',
          300: '#FCD34D',
          400: '#FBBF24',
          500: '#F59E0B',
          600: '#D97706',
          700: '#B45309',
          800: '#92400E',
          900: '#78350F',
        },
        danger: {
          50:  '#FFF1F2',
          100: '#FFE4E6',
          200: '#FECDD3',
          300: '#FDA4AF',
          400: '#FB7185',
          500: '#F43F5E',
          600: '#E11D48',
          700: '#BE123C',
          800: '#9F1239',
          900: '#881337',
        },
        slate: {
          50:  '#F8FAFC',
          100: '#F1F5F9',
          200: '#E2E8F0',
          300: '#CBD5E1',
          400: '#94A3B8',
          500: '#64748B',
          600: '#475569',
          700: '#334155',
          800: '#1E293B',
          900: '#0F172A',
        },
        amount: '#DC2626',
      },
    },
    spacing: {
      'erp-0':   '0px',
      'erp-0.5': '2px',
      'erp-1':   '4px',
      'erp-1.5': '6px',
      'erp-2':   '8px',
      'erp-3':   '12px',
      'erp-4':   '16px',
      'erp-5':   '20px',
      'erp-6':   '24px',
      'erp-8':   '32px',
      'erp-10':  '40px',
      'erp-12':  '48px',
      'erp-16':  '64px',
    },
    borderRadius: {
      'erp-none': '0px',
      'erp-sm':   '4px',
      'erp-md':   '8px',
      'erp-lg':   '12px',
      'erp-xl':   '16px',
      'erp-full': '9999px',
    },
    boxShadow: {
      'erp-xs':  '0 1px 2px 0 rgba(0,0,0,0.05)',
      'erp-sm':  '0 1px 3px 0 rgba(0,0,0,0.08), 0 1px 2px -1px rgba(0,0,0,0.05)',
      'erp-md':  '0 4px 6px -1px rgba(0,0,0,0.08), 0 2px 4px -2px rgba(0,0,0,0.05)',
      'erp-lg':  '0 10px 15px -3px rgba(0,0,0,0.08), 0 4px 6px -4px rgba(0,0,0,0.05)',
      'erp-xl':  '0 20px 25px -5px rgba(0,0,0,0.08), 0 8px 10px -6px rgba(0,0,0,0.05)',
    },
    fontFamily: {
      'erp-sans': "Inter, -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', 'Helvetica Neue', Helvetica, Arial, sans-serif",
      'erp-mono': "'JetBrains Mono', 'Cascadia Code', 'SF Mono', 'Fira Code', Consolas, 'Liberation Mono', Menlo, monospace",
    },
  },
  rules: [
    [
      /^custom-hover$/,
      ([], { rawSelector }) => {
        const selector = e(rawSelector)
        return `
${selector} {
  display: flex;
  height: 100%;
  padding: 0 10px;
  cursor: pointer;
  align-items: center;
  transition: background var(--transition-time-02);
}
/* you can have multiple rules */
${selector}:hover {
  background-color: var(--top-header-hover-color);
}
.dark ${selector}:hover {
  background-color: var(--el-bg-color-overlay);
}
`
      }
    ],
    [
      /^layout-border__left$/,
      ([], { rawSelector }) => {
        const selector = e(rawSelector)
        return `
${selector}:before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background-color: var(--el-border-color);
  z-index: 3;
}
`
      }
    ],
    [
      /^layout-border__right$/,
      ([], { rawSelector }) => {
        const selector = e(rawSelector)
        return `
${selector}:after {
  content: "";
  position: absolute;
  top: 0;
  right: 0;
  width: 1px;
  height: 100%;
  background-color: var(--el-border-color);
  z-index: 3;
}
`
      }
    ],
    [
      /^layout-border__top$/,
      ([], { rawSelector }) => {
        const selector = e(rawSelector)
        return `
${selector}:before {
  content: "";
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background-color: var(--el-border-color);
  z-index: 3;
}
`
      }
    ],
    [
      /^layout-border__bottom$/,
      ([], { rawSelector }) => {
        const selector = e(rawSelector)
        return `
${selector}:after {
  content: "";
  position: absolute;
  bottom: 0;
  left: 0;
  width: 100%;
  height: 1px;
  background-color: var(--el-border-color);
  z-index: 3;
}
`
      }
    ]
  ],
  presets: [presetUno({ dark: 'class', attributify: false })],
  // transformers: [transformerVariantGroup()],
  shortcuts: {
    'wh-full': 'w-full h-full'
  }
})
