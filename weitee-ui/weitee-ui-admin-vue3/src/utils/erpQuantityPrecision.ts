export type ErpQuantityPrecisionProduct = {
  id?: number
  quantityPrecision?: number
}

export const ERP_DEFAULT_QUANTITY_PRECISION = 3
export const ERP_MAX_QUANTITY_PRECISION = 6

export const normalizeQuantityPrecision = (precision?: number) => {
  return Number.isInteger(precision) && precision! >= 0 && precision! <= ERP_MAX_QUANTITY_PRECISION
    ? precision!
    : ERP_DEFAULT_QUANTITY_PRECISION
}

export const getProductQuantityPrecision = (
  products: ErpQuantityPrecisionProduct[],
  productId?: number
) => {
  return normalizeQuantityPrecision(products.find((item) => item.id === productId)?.quantityPrecision)
}

export const getProductQuantityStep = (
  products: ErpQuantityPrecisionProduct[],
  productId?: number
) => {
  const precision = getProductQuantityPrecision(products, productId)
  return precision === 0 ? 1 : Number((10 ** -precision).toFixed(precision))
}

export const roundQuantityByPrecision = (value: number | undefined, precision: number) => {
  return Number(Number(value || 0).toFixed(normalizeQuantityPrecision(precision)))
}
