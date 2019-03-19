Lucifer利用基于约束求解的故障注入方法快速对微服务进行测试。针对冗余失效的不同场景，Lucifer从超市模式、重试模式、船舱模式以及熔断模式等角度帮助用户理解系统行为。
=========================

#   效果及功能
##  自动测试
    (1) 点击配置选项，根据名称对测试参数进行配置（如果不进行本步骤，则测试将使用默认参数）
    (2) 点击测试选项，打开界面后单击开始按钮，测试过程将实时显示在界面中
  ![测试过程控制界面](https://raw.githubusercontent.com/ccx1024cc/lucifer/master/images/%E6%B5%8B%E8%AF%95%E8%BF%87%E7%A8%8B%E6%8E%A7%E5%88%B6.png)
    (3) 所有已经结束的测试结果可以通过历史界面查询
  ![测试历史界面](https://raw.githubusercontent.com/microcosmx/train_ticket/master/image/select_contace.png)
 
   
##  服务网格管理
    (1) 通过监控界面，可以自定义查询条件，并生成实时刷新的监控图。除此之外，本工具集成了Grafana以及Jaeger，分别通过点击上方的Statistic和Topology可对相关监控数据进行查询
  ![统计信息可视化界面](https://raw.githubusercontent.com/microcosmx/train_ticket/master/image/login.png)
    (2) 利用故障管理界面，可以查询以及手动增删故障，帮助用户快速验证应用的容错能力
  ![故障管理界面](https://raw.githubusercontent.com/microcosmx/train_ticket/master/image/login.png)
    (3) 利用版本分流界面，可以快速进行灰度发布，便于测试环境的搭建
  ![版本分流界面](https://raw.githubusercontent.com/microcosmx/train_ticket/master/image/select_contace.png)


#    相关技术与设计
    (1) 自动探测算法
  ![算法流程](https://raw.githubusercontent.com/microcosmx/train_ticket/master/image/login.png)
    (2) 系统架构
  ![系统架构](https://raw.githubusercontent.com/microcosmx/train_ticket/master/image/login.png)
    (3) 数据结构
  ![数据结构](https://raw.githubusercontent.com/microcosmx/train_ticket/master/image/login.png)

## 设计与实现详情
   [面向微服务的弹性测试工具](https://raw.githubusercontent.com/microcosmx/train_ticket/master/image/login.png)

  
## 部署
- Z3Prover
- Istio
- k8s
- jdk1.8
- MYSQL5.8

---
## 配置参数（具体值取决于Istio以及相关插件的安装方式）
com.iscas.service.Jaeger中host
com.iscas.service.Telemetry中host
resources/static/*.html中Grafana等工具的查询链接
resources/application.properties中数据库相关配置
