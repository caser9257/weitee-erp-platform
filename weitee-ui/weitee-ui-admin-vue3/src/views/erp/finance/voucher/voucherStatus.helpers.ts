export interface VoucherRecomputeSource {
  status?: number
  bizType?: number
  bizId?: number
}

const VOIDED_STATUS = 50

export const canRecomputeVoucher = (voucher: VoucherRecomputeSource) =>
  voucher.status !== VOIDED_STATUS && Number.isInteger(voucher.bizType) && Number.isInteger(voucher.bizId)
