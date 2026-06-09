#!/usr/bin/env python
# -*-coding:utf-8 -*-
# -*-Author：chenrui -*-
import functools
import sys
import json
import logging
import base64
import pandas as pd
from pandas import DataFrame
from pandas import MultiIndex
import time

pd.set_option('display.expand_frame_repr', False)


class Result:
    """
    通用返回结果
    """
    # 是否成功
    success: bool
    # 成功/失败消息
    message: str
    # 结果数据
    result: dict

    @staticmethod
    def ok(msg="success", data=None):
        """
        构造成功返回
        :param msg: 返回消息
        :param data: 结果数据
        :return:
        """
        temp_res = Result()
        temp_res.success = True
        temp_res.message = msg
        temp_res.result = data
        return temp_res

    @staticmethod
    def error(msg="error"):
        """
        构造错误返回
        :param msg: 返回消息
        :return:
        """
        temp_res = Result()
        temp_res.success = False
        temp_res.message = msg
        temp_res.result = None
        return temp_res


# 返回结果标识
RESULT_MARK = "$JM$END$"


def init_args(arg=None):
    """
    初始化参数
    :return:
    """
    opts = {}
    if arg is None:
        argv = sys.argv[1:]
        # print('共有:', len(argv), '个参数。')
        if len(argv) <= 0:
            return opts
        arg = argv[0]

    if arg is not None and len(arg) > 0:
        opts = json.loads(base64.b64decode(arg))
    return opts


def parse_float(num_str: str):
    """
    将string转换为float类型
    :param num_str:
    :return:
    """
    try:
        return float(num_str)
    except ValueError:
        return float('nan')


def obj_2_json(obj: object) -> str:
    """
    对象转json字符串
    :param obj:
    :return:
    """
    return json.dumps(obj, default=lambda o: o.__dict__, separators=(',', ':'), sort_keys=True)


def df_to_json(df: DataFrame) -> str:
    """
    DataFrame转换为json
    :param df:
    :return:
    """
    df.reset_index(inplace=True)
    out_data = df.to_json(path_or_buf=None, orient="records", date_format=None, double_precision=10, force_ascii=True,
                          date_unit='ms', default_handler=None, lines=False, compression=None, index=False)
    return out_data


def build_df_from_json(json_str: str) -> DataFrame:
    """
    从json构建DataFrame
    :param json_str: JsonString
    :return:
    """
    j_data = json.loads(json_str)
    df = pd.json_normalize(j_data)
    return df


def render(opts={}):
    """
    数据分组
    :param opts:    {
                        # 原始数据
                        data:[{col1:val1},{col2:val2}],
                        # 纵向分组配置
                        groupFieldsY:[{index:field1,sort:'asc'}],
                        # 横向分组配置
                        groupFieldsX:[{column:field1,sort:'asc'}],
                        # 分组小计配置  totalFields 分组依据字段; aggField:聚合字段
                        subTotal:{totalFields:[field1,field2],aggField:[{field1:sum}],totalTexts:{totalField1:totalText1}}
                    }
    :return:    {
                    # 表头数据
                    columns:[(2020,01,field1),(2020,01,field2)],
                    # 分组后数据
                    data:[{col1:val1},{col2:val2}]
                }
    """
    data_in = opts.get("data")
    df = build_df_from_json(json.dumps(data_in))

    indexes = []
    index_sorts = []
    if 'groupFieldsY' in opts:
        group_by_field_y = opts['groupFieldsY']
        indexes = [x['index'] for x in group_by_field_y]
        index_sorts = [x['sort'] == 'asc' for x in group_by_field_y]

    columns = []
    column_sorts = []
    if 'groupFieldsX' in opts:
        group_by_field_x = opts['groupFieldsX']
        columns = [x['column'] for x in group_by_field_x]
        column_sorts = [x['sort'] == 'asc' for x in group_by_field_x]

    if indexes is not None and len(indexes) > 0 and columns is not None and len(columns) > 0:
        df = __both_group(df, indexes, index_sorts, columns, column_sorts)
    elif indexes is None or len(indexes) == 0:
        df = __single_group(df, columns, column_sorts, direction='right')
    elif columns is None or len(columns) == 0:
        df = __single_group(df, indexes, index_sorts)

    # 分组小计
    if 'subTotal' in opts:
        total_fields = opts['subTotal']['totalFields']
        agg_field = opts['subTotal']['aggField']
        total_texts = opts['subTotal']['totalTexts']
        if None is not agg_field and len(agg_field) > 0 and None is not total_fields and len(total_fields) > 0:
            df = group_total(df, indexes, index_sorts, total_fields, agg_field, total_texts)

    return render_result(df)


def __single_group(df: DataFrame, group_by, sorts, direction='up'):
    """
    单边分组
    :param df: 数据对象
    :param group_by: 分组字段,list或string; eg. ['a','b'] or 'a'
    :param sorts: 分组字段排序,list或boolean,True正序,False倒序; eg. [True,False] or True
    :param direction: 分组方向,string,up:纵向,right:横向; 默认:up
    :return:
    """
    indexes = []
    columns = []
    if direction == 'up':
        indexes = group_by
    else:
        columns = group_by
    df = df.pivot(index=indexes, columns=columns)
    if not isinstance(df, DataFrame):
        df = df.to_frame()

    # 调整索引顺序
    if direction != 'up':
        for i in range(len(group_by), 0, -1):
            df = df.swaplevel(0, i)
        df = df.T
    # 排序
    df = df.sort_index(level=group_by, axis=0 if direction == 'up' else 1, ascending=sorts)

    return df


def __both_group(df: DataFrame, indexes, index_sorts, columns, column_sorts):
    """
    双边分组(横纵分组)
    :param df: 数据对象
    :param indexes: 索引(纵向分组字段) ,list或string; eg. ['a','b'] or 'a'
    :param index_sorts:索引排序,list或boolean,True正序,False倒序; eg. [True,False] or True
    :param columns: 列(横向分组字段) ,list或string; eg. ['a','b'] or 'a'
    :param column_sorts: 列排序,list或boolean,True正序,False倒序; eg. [True,False] or True
    :return:
    """
    # 转换透视表
    df = pd.pivot_table(df, index=indexes, columns=columns, aggfunc='max')

    # 调整索引顺序
    df = df.T
    for i in range(len(columns), 0, -1):
        df = df.swaplevel(0, i)
    df = df.T

    # 横向排序
    if column_sorts is not None and len(column_sorts) > 0:
        df = df.sort_index(level=columns, axis=1, ascending=column_sorts)

    # 纵向排序
    if index_sorts is not None and len(index_sorts) > 0:
        df = df.sort_index(level=indexes, ascending=index_sorts)

    return df


def group_total(df: DataFrame, group_by_field: list, sorts, total_fields, agg_field: dict, total_texts: dict):
    """
    分组小计
    :param df: DataFrame 原始数据
    :param group_by_field: 分组字段
    :param sorts:索引排序,list或boolean,True正序,False倒序; eg. [True,False] or True
    :param total_fields: 小计依据
    :param agg_field: 小计字段,格式:{字段名,聚合函数};
            支持的聚合函数: min:最小值,max:最大值,sum:求和,mean:平均值,median:中位数,std:标准差,var:方差,count:计数
    :param total_texts: 小计展示文本:{分组依据:展示文本};
    :return:
    """
    origin_df = df

    def sort_func(a, b):
        """
        排序统计依据字段:倒序排列
        :param a:
        :param b:
        :return:
        """
        a_ix = group_by_field.index(a)
        b_ix = group_by_field.index(b)
        if a_ix > b_ix:
            return -1
        elif a_ix < b_ix:
            return 1
        else:
            return 0

    total_fields = sorted(total_fields, key=functools.cmp_to_key(sort_func))
    if isinstance(df.columns, MultiIndex):
        """如果是多级表头,将聚合字段也调整成多级表头"""
        df_cols = df.columns
        temp_agg_field = {}
        for df_col in df_cols:
            for key, func in agg_field.items():
                if key in df_col:
                    temp_agg_field[df_col] = func
                    break
        agg_field = temp_agg_field
    for total_field in total_fields:
        # 小计依据字段在分组字段中的下标
        total_field_ix_in_all = group_by_field.index(total_field)
        # 纵向分组并聚合得到聚合表
        agg_groupby_fields = group_by_field[:total_field_ix_in_all + 1]
        agg = origin_df.map(parse_float).groupby(agg_groupby_fields).agg(agg_field)

        def update_agg_ix_lv(x):
            """
            将聚合表的索引层级与原始表保持一致,因为后面合并原始表与聚合表时需要保证索引数量一致
            :param x:
            :return:
            """
            if isinstance(x, str):
                x = [x]
            else:
                x = list(x)
            new_index = []
            for i in range(len(group_by_field)):
                if i < len(x):
                    new_index.append(x[i])
                else:
                    new_index.append('')
            return tuple(new_index)

        agg.index = agg.index.map(update_agg_ix_lv)

        # 纵向排序
        if sorts is not None and len(sorts) > 0:
            agg = agg.sort_index(level=[x for x in range(len(group_by_field))], ascending=sorts)

        def update_ix(x):
            """
            更新小计行索引内容
            :param x:
            :return:
            """
            x = list(x)
            new_index = []
            for i in range(len(x)):
                if i == total_field_ix_in_all:
                    new_index.append(x[i] + f'^JCTS^{total_texts[total_field]}')
                elif i < total_field_ix_in_all:
                    new_index.append(x[i])
                else:
                    new_index.append('')
            return tuple(new_index) if len(new_index) > 1 else new_index[0]

        # 更新小计行索引内容
        agg.index = agg.index.map(update_ix)
        # 更新聚合表索引名称
        agg.index.names = group_by_field
        agg.index.name = group_by_field[0]

        # 调整小计行的顺序到正确为止
        # 源数据的排序号
        df_sort = []
        # 统计数据的排序号
        agg_sort = []
        # 原始数据的索引 [华北/北京,华中/上海]
        df_indexes = list(df.index)
        # 临时存储的索引值
        temp_index = ''
        # 全局排序号
        sort: int = 0

        for d in df_indexes:
            d = [d] if isinstance(d, str) else d
            if sort == 0:
                df_sort.append(sort)
            elif temp_index != d[total_field_ix_in_all] and len(d[total_field_ix_in_all]) > 0:
                agg_sort.append(sort)
                sort += 1
                df_sort.append(sort)
            else:
                df_sort.append(sort)
            temp_index = d[total_field_ix_in_all]
            sort += 1
        agg_sort.append(sort)
        # 根据columns的层级来构造排序column
        sort_field_name = 'sort'
        if isinstance(df.columns, MultiIndex):
            sort_field_name = tuple(['sort' for i in range(len(df.columns.levels))])
        df.insert(0, sort_field_name, df_sort)
        agg.insert(0, sort_field_name, agg_sort)

        # 合并小计表和原始表
        df = pd.concat([df, agg])
        # 依据排序号排序
        df = df.sort_values([sort_field_name])
        # 删除临时数据
        df = df.drop(columns=[sort_field_name])
        # 移除nan
        df = df.fillna(value='')
    return df


def render_result(df):
    """
    构建渲染结果
    :param df:
    :return:
    """
    # print(str(df))
    json_data = df_to_json(df)
    df_columns = list(x for x in df.columns)
    return {'data': json_data, 'columns': obj_2_json(df_columns)}


begin_time = int(round(time.time() * 1000))
if __name__ == '__main__':
    result = Result.ok("success")
    try:
        options = init_args()
        data = render(options)
        result.result = data
    except Exception as e:
        logging.exception(e)
        result = {
            "success": False,
            "message": str(e),
            "result": None
        }
    # 输出返回脚本执行结果
    print(RESULT_MARK + obj_2_json(result) + RESULT_MARK)
    print("python-end" + str(int(round(time.time() * 1000)) - begin_time))
