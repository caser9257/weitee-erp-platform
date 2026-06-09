#!/usr/bin/env python
# -*-coding:utf-8 -*-
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.common.action_chains import ActionChains
from selenium.webdriver.chrome.options import Options
import time
import sys
import getopt
import json
import os
import shutil
import platform
import re
# import traceback
import base64
import io

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding='utf-8')

# 默认文件下载目录 改成自己服务器的
DEFAULT_BASE_DOWNLOAD_PATH = os.path.dirname(os.path.realpath('__file__')) + os.sep + "downloads" + os.sep
# update-begin---author:chenrui ---date:20240130  for：[QQYUN-8127]通过接口导出功能要获取token和查询参数------------
# 临时下载目录
TEMP_DOWNLOAD_PATH = os.sep + "temp" + os.sep
# update-end---author:chenrui ---date:20240130  for：[QQYUN-8127]通过接口导出功能要获取token和查询参数------------

# 默认积木报表访问地址 改成自己的
DEFAULT_BASE_SHARE_VIEW_URL = "http://localhost:8085/jmreport/view/"

# 结束日志标识
LOG_END_MARK = "$JM$END$"


def append_common_params(payload: str, token: str) -> str:
    """
    拼接报表的通用参数
    :param payload:
    :param token:
    :return:
    """
    if payload is None or len(payload) == 0:
        return ""
    if token is not None and len(token) > 0:
        token = "token=" + token
        if "?" in payload:
            payload += "&" + token
        else:
            payload += "?" + token
    if "?" in payload:
        payload += "&directDld=1"
    else:
        payload += "?directDld=1"
    return payload


def dict_2_str_payload(payload: dict) -> str:
    """
    字典数据转换为查询payload
    :param payload:
    :return:
    """
    if payload is not None and len(payload) > 0:
        return str("&".join([key + "=" + val for key, val in payload.items() if key is not None and len(key) > 0]))
    else:
        return ""


def init_args(argv=None):
    """初始化参数"""
    global opts
    print('共有:', len(argv), '个参数。')
    if len(argv) <= 0:
        raise Exception('参数异常')
    arg = argv[0]
    if arg is not None and len(arg) > 0:
        opts = json.loads(base64.b64decode(arg))
    # 批次号
    batch_no = None
    # 导出类型
    export_type = 'PDF'
    # 报表ids
    report_ids = None
    # 报表参数,与report_ids二选一
    report_params = list()
    # 积木报表预览页面地址
    base_share_view_url = DEFAULT_BASE_SHARE_VIEW_URL
    # 报表下载基础目录
    base_download_path = DEFAULT_BASE_DOWNLOAD_PATH
    # token
    token = ''

    if 'batch_no' in opts:
        batch_no = opts['batch_no']
    if 'export_type' in opts:
        export_type = opts['export_type']
    if 'report_ids' in opts:
        report_ids = opts['report_ids']
    if 'report_params' in opts:
        report_params = opts['report_params']
    if 'jimu_view_url' in opts:
        base_share_view_url = opts['jimu_view_url']
    if 'base_download_path' in opts:
        base_download_path = opts['base_download_path']
    if 'token' in opts:
        token = opts['token']

    if export_type.upper() == "EXCEL":
        export_type = "Excel"
    elif export_type.upper() == "PDF":
        export_type = "PDF"
    else:
        export_type = "PDF"

    # 拼接报表查询参数
    reports: list[dict] = list()
    if report_params is not None and len(report_params) > 0:
        for report_param in report_params:
            report_query = report_param['id']
            if 'params' in report_param:
                params = report_param['params']
                if params is not None and len(params) > 0:
                    payload = dict_2_str_payload(params)
                    if payload is not None and len(payload) > 0:
                        report_query += "?" + payload
            custom_export_type = export_type
            if 'export_type' in report_param:
                custom_export_type = report_param['export_type']
            custom_export_type = export_type if custom_export_type is None else custom_export_type
            reports.append({"url": append_common_params(report_query, token), "export_type": custom_export_type})
    elif report_ids is not None and len(report_ids) > 0:
        reports = [{"url": append_common_params(report_id, token), "export_type": export_type} for report_id in
                   report_ids]

    # 确保传入路径正确,统一修改所有的/ 和 \\为当前系统的盘符
    base_download_path = base_download_path.replace("/", os.sep).replace("\\", os.sep)
    if not os.path.isabs(base_download_path):
        raise Exception("导出失败,下载目录必须是绝对路径")
    if "windows" in platform.platform().lower() and base_download_path.startswith("\\"):
        # windows 系统下 并且没有写盘符时,拼接盘符
        run_path = os.path.dirname(os.path.realpath('__file__'))
        base_download_path = os.path.splitdrive(run_path)[0] + base_download_path

    options = {
        'batch_no': batch_no,
        'export_type': export_type,
        'reports': reports,
        "base_share_view_url": base_share_view_url,
        "base_download_path": base_download_path
    }
    print("运行参数:" + json.dumps(options))
    return options


def auto_export(options):
    print(" >>> java进入Python 脚本方法，options = ", options)
    """
    自动导出函数
    :param options: {batch_no:批次号,export_type:导出类型,reports:[{url:报表url,export_type:导出类型}]}
    """
    # 整理参数
    batch_no = options['batch_no']

    export_type = options['export_type']

    reports = None
    # 优先使用report
    if "reports" in options:
        reports = options['reports']

    if not reports or reports is None:
        if "report_ids" in options:
            report_ids = options['report_ids']
            if not report_ids:
                raise Exception('报表id不能为空')
            else:
                # reports 为空,将report_ids转换为reports
                reports = [{"url": report_id, "export_type": export_type} for report_id in report_ids]
        else:
            raise Exception('报表id不能为空')

    if not batch_no or not reports:
        raise Exception('批次编号或报表id不能为空')

    # 下载目录
    download_path = options["base_download_path"] + batch_no

    # 获取域名
    base_share_view_url = options["base_share_view_url"]
    match = re.match(r'(http[s]?://[^/]+)', base_share_view_url)
    if match:
        base_url = match.group(1)
    else:
        base_url = ""

    # 获取webDriver
    driver = build_web_driver(download_path, base_url)

    # 确保目录存在
    if os.path.exists(download_path) is False:
        os.makedirs(download_path)
    # update-begin---author:chenrui ---date:20240129  for：[QQYUN-8127]通过接口导出功能要获取token和查询参数------------
    else:
        # 清空文件夹并重建
        shutil.rmtree(download_path)
        os.makedirs(download_path)

    # 确保临时目录存在
    if os.path.exists(download_path + TEMP_DOWNLOAD_PATH) is False:
        os.makedirs(download_path + TEMP_DOWNLOAD_PATH)
    # update-end---author:chenrui ---date:20240129  for：[QQYUN-8127]通过接口导出功能要获取token和查询参数------------

    downloaded_count = 0
    download_failure_rids = []

    # 导出失败的报表标题
    failure_report_title = ""
    # 开始自动导出
    for report in reports:
        report_url = report['url']
        custom_export_type = report['export_type']
        if not custom_export_type:
            custom_export_type = export_type
        else:
            if custom_export_type.upper() == "EXCEL":
                custom_export_type = "Excel"
            elif custom_export_type.upper() == "PDF":
                custom_export_type = "PDF"
            else:
                custom_export_type = "PDF"
        print("开始导出报表:" + report_url)
        # 打开url网页
        driver.get(options["base_share_view_url"] + report_url)
        # 等待数据查询完成
        export_el = WebDriverWait(driver, 10, 0.2).until_not(
            EC.presence_of_element_located((By.CLASS_NAME, "ivu-spin-fullscreen"))
        )
        # 等待导出按钮加载完成
        export_el = WebDriverWait(driver, 10, 0.2).until(
            EC.presence_of_element_located((By.CLASS_NAME, "export"))
        )
        # 获取title
        title = driver.title
        if len(title) > 0 and "-" in title:
            title = title[:title.rindex("-")].strip()
        print("报表名称:" + title)
        if not download_check(download_path, title, custom_export_type.lower(), 1):
            # 报表不存在,开始下载
            print("报表{}未下载,开始下载...".format(title))
            # 等待0.5秒,防止页面未完成渲染
            time.sleep(0.5)
            # 鼠标移到导出按钮
            ActionChains(driver).move_to_element(export_el).perform()
            # 点击导出pdf按钮
            driver.find_element(By.ID, custom_export_type).click()
            # 检查是否下载完成
            if not download_check(download_path, title, custom_export_type.lower(), 30, 1):
                print("报表{}下载失败".format(title))
                failure_report_title += title + " "
                download_failure_rids.append(report_url)
            else:
                downloaded_count = downloaded_count + 1
                print("报表:" + report_url + "导出完成")
        else:
            # 报表存在
            downloaded_count = downloaded_count + 1
    # update-begin---author:chenrui ---date:20240129  for：统一py脚本的返回结果格式------------
    err_msg = ""
    if len(reports) != downloaded_count:
        err_msg = "报表[" + failure_report_title + "]导出失败"
    result = {
        "success": len(reports) == downloaded_count,
        "message": err_msg,
        "result": {
            "report_count": len(reports),
            "downloaded_count": downloaded_count,
            "failure_rids": download_failure_rids,
            "download_path": download_path,
            **options
        }
    }
    # update-end---author:chenrui ---date:20240129  for：统一py脚本的返回结果格式------------

    # 退出浏览器
    driver.quit()
    return result


def build_web_driver(download_path, safe_domain=""):
    """
    构建webDriver
    :param safe_domain: 安全域名
    :param download_path: 下载目录
    :return: webDriver
    """
    chrome_options = Options()
    # 不使用沙箱
    chrome_options.add_argument('--no-sandbox')
    # 将浏览器静音
    chrome_options.add_argument("--mute-audio")
    # 当程序结束时，浏览器不会关闭
    # chrome_options.add_experimental_option("detach", True)
    # 开启无界面浏览器(minos必须开启无界面)
    chrome_options.add_argument("--headless")
    # 禁用gpu
    chrome_options.add_argument("--disable-gpu")
    # 添加安全域名
    if safe_domain is not None and bool(safe_domain):
        chrome_options.add_argument("--unsafely-treat-insecure-origin-as-secure=" + safe_domain)
    if 'linux' in platform.platform().lower():
        # fix:DevToolsActivePort file doesn't
        chrome_options.add_argument('--disable-dev-shm-usage')
        chrome_options.add_argument('--remote-debugging-port=9222')
    # 设置下载路径
    prefs = {'profile.default_content_settings.popups': 0,
             'download.prompt_for_download': False,
             'safebrowsing.disable_download_protection': True,
             'download.default_directory': download_path + TEMP_DOWNLOAD_PATH}
    chrome_options.add_experimental_option('prefs', prefs)
    # 忽略不安全的错误
    chrome_options.add_argument('ignore-certificate-errors')
    # Chrome浏览器
    driver = webdriver.Chrome(options=chrome_options)
    return driver


def download_check(check_path, check_file_name, check_ext, check_times=3, check_interval=5):
    """
    检测函数
    :param check_path:检测路径
    :param check_file_name: 检查文件名称
    :param check_ext:检测扩展名
    :param check_times:检测次数（默认值:3）
    :param check_interval:检测时间间隔（默认值:5）
    :return:返回真假
    """
    temp_check_path = check_path + TEMP_DOWNLOAD_PATH
    if os.path.exists(temp_check_path) is False:
        return False
    else:
        for number in range(0, int(check_times)):
            print("验证文件{}是否存在;第{}次检测.".format(check_file_name, str(number + 1)))
            # time.sleep(0.2)
            # 读取目录下所有文件
            # update-begin---author:chenrui ---date:20240129  for：[QQYUN-8127]通过接口导出功能要获取token和查询参数------------
            files = os.listdir(temp_check_path)
            file_number = len(files)
            if file_number > 0:
                # 存在多个文件,检查当前文件是否存在
                for file in files:
                    if str(check_file_name.strip()) in str(file):
                        # 文件存在
                        dest_move_file_path = check_path
                        if os.path.exists(check_path + os.sep + str(file)):
                            print("文件{}存在;重命名该文件.".format(check_file_name, str(number + 1)))
                            filename, extension = os.path.splitext(file)
                            dest_move_file_path += (os.sep + filename
                                                    + "({})".format(str(int(time.time())))
                                                    + extension)
                        shutil.move(temp_check_path + str(file), dest_move_file_path)
                        # update-end---author:chenrui ---date:20240129  for：[QQYUN-8127]通过接口导出功能要获取token和查询参数------------
                        return True
            # 文件不存在
            if check_times != 1 or number < check_times - 1:
                time.sleep(int(check_interval))  # 休眠一会
        return False


if __name__ == '__main__':
    """
    入口
    """
    result = {}
    try:
        print(" >>> java进入Python ==> step.1  进入Main方法")
        args = sys.argv[1:]
        # #本地调试参数
        # args = ['-b', '1713260060264cubcWF', '-r',
        #         '537516331017523200?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJleHAiOjE3MTM1NTgxNDQsInVzZXJuYW1lIjoiMTg2MTE3ODg1MjUifQ.JXZLzqgkuZRcbEYFn0l-L0DXZzOw3hJZIzn0y2EmzQM',
        #         '-t', 'PDF', '-s', 'http://localhost:8087/jmreport/view/', '-d', 'E:\\opt\\jmpydownload\\']
        print(" >>> java进入Python ==> step.2  初始化参数")
        export_options = init_args(args)
        print(" >>> java进入Python ==> step.3  开始执行py脚本")
        result = auto_export(export_options)
        print(" >>> java进入Python ==> step.4  返回执行结果")
    except Exception as e:
        print("异常日志:", e)
        # traceback.print_exc()
        msg = ""
        if hasattr(e, "msg"):
            msg = e.msg
        else:
            msg = str(e)
        # update-begin---author:chenrui ---date:20240129  for：统一py脚本的返回结果格式------------
        result = {
            "success": False,
            "message": msg,
            "result": None
        }
        # update-end---author:chenrui ---date:20240129  for：统一py脚本的返回结果格式------------
    print(LOG_END_MARK + json.dumps(result) + LOG_END_MARK)
