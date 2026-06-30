export const shouldEnableTongji = (hmId: string | undefined, isProd: boolean) => {
  return Boolean(isProd && hmId && hmId.trim())
}
