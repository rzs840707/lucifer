# -*- coding: utf-8 -*-

import requests
import json

if __name__ == "__main__":
    url = "http://localhost:31111/productpage"
    headers = {}
    data = {}

    r = requests.get(url, headers=headers, data=data)
    message = ""
    if r.status_code != requests.codes.ok:
        message += "无法获取主页"
    else:
        r = r.text
        if "Sorry, product details are currently unavailable for this book." in r:
            message += "详情信息无法获取;"
        if "Sorry, product reviews are currently unavailable for this book." in r:
            message += "评论信息无法获取;"
        if "Sorry, product ratings are currently unavailable for this book." in r:
            message += "评分信息无法获取;"

    if len(message) != 0:
        result = [{
            "name": "主页信息",
            "url": url,
            "message": message
        }]
        print (json.dumps(result, ensure_ascii=False))
