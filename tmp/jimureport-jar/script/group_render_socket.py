#!/usr/bin/env python
# -*-coding:utf-8 -*-
# -*-Author：chenrui -*-

import getopt
import logging
import socket
import sys
import threading
import time

import group_render as render
from group_render import Result
from group_render import RESULT_MARK


def main():
    """
    main
    :return:
    """
    argv = sys.argv[1:]
    print('共有:', len(argv), '个参数。')
    # 监听地址
    host = '127.0.0.1'
    # 端口
    post = 17065
    try:
        # 短选项模式
        opts, args = getopt.getopt(argv, "h:p:")
        for opt, arg in opts:
            if opt in ['-h']:
                host = arg
            elif opt in ['-p']:
                post = int(arg)
        # 开启socket服务
        SocketServer(host, post)
    except Exception as e:
        logging.exception(e)
        result = {
            "success": False,
            "message": str(e),
            "result": None,
            "code": e.errno
        }
        # 输出返回脚本执行结果
        print(RESULT_MARK + render.obj_2_json(result) + RESULT_MARK)


class SocketServer:
    """
    Socket服务
    """

    def __init__(self, host='127.0.0.1', post=17065):
        """
        构造方法
        :param host: 监听地址
        :param post: 端口号
        """
        # 设置一个flag判断这个连接是否通过了验证
        self.checked = False
        self.server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.address = (host, post)
        self.server.bind(self.address)
        self.server.listen()
        print()
        result = Result.ok("Python Socket 已开启:" + str(self.server.getsockname()))
        print(RESULT_MARK + render.obj_2_json(result) + RESULT_MARK)
        while True:
            try:
                client, address = self.server.accept()
                self.ServerThreading(client).start()
            except Exception as e:
                logging.exception(e)
                break
        # 关闭socket服务器
        self.server.close()

        print(+"Socket服务已停止")

    class ServerThreading(threading.Thread):
        """
        接收消息的线程
        """

        def __init__(self, client_socket, recv_size=1024 * 1024, encoding="utf-8"):
            """
            构造
            :param client_socket: socket 客户端
            :param recv_size: 接收数据缓存大小
            :param encoding: 编码
            """
            threading.Thread.__init__(self)
            self._socket = client_socket
            self._recv_size = recv_size
            self._encoding = encoding
            pass

        def run(self):
            """
            thread run method
            :return:
            """
            print("开启接收消息线程.....")
            begin_time = int(round(time.time() * 1000))
            try:
                # 接受数据
                msg = ''
                while True:
                    # 从Java端读取recvsize个字节
                    rec = self._socket.recv(self._recv_size)

                    # 解码成字符串
                    msg += rec.decode(self._encoding)
                    print("解码后数据：")
                    print(msg)

                    # 文本接受是否完毕，因为python socket不能自己判断接收数据是否完毕
                    # 所以需要自定义协议标志数据接受完毕
                    if msg.strip().endswith('over'):
                        msg = msg[:-4]
                        break

                result = render.Result.ok("success")
                if msg != "ConnTest":
                    options = render.init_args(msg)
                    data = render.render(options)
                    print("python渲染耗时" + str(int(round(time.time() * 1000)) - begin_time))
                    result.result = data
                resp = render.obj_2_json(result)
                print(resp)

                # 发送字符串数据给Java端
                self._socket.send(("%s" % resp).encode(self._encoding))
                pass
            except Exception as identifier:
                result = render.Result.error(str(identifier))
                resp = render.obj_2_json(result)
                self._socket.send(("%s" % resp).encode(self._encoding))
                logging.exception(identifier)
                pass
            finally:
                self._socket.close()
            print("接收消息结束.....")
            pass

        def __del__(self):
            pass


if __name__ == "__main__":
    main()
