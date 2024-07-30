# A接口公共库演示示例

在context中同时运行了服务端和客户端，实际使用时应该只会用到一个。

自定义参数
```properties
{{droneCode}}
{{robotCode}}
{{nestCode}}
```

上下文参数
```properties
{{task_code}}
{{task_patrolled_id}}
```

内置参数
```properties
{{uuid}}
# yyyy-MM-dd HH:mm:ss
{{now}}
# yyyyMMddHHmmss
{{simpleNow}}
# yyyy-MM-dd
{{nowDate}}
```


[Swagger在线文档地址](http://127.0.0.1:18088/demo/doc.html)

自定义参数设置 /demo/automation/defaultParams