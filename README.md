Lucifer uses constraint-based fault injection to test micro-services quickly. For different scenarios of redundancy failure, Lucifer helps users understand system behavior from the perspectives of timeout mode, retry mode, bulkhead mode and circuitbreaker mode.
=========================

#   Effect and Function
##  Automatic Testing
  (1) Click on the configuration option to configure the test parameters by name (if this step is not taken, the test will use the default parameters)
  (2) Click on the test options, open the interface and click the start button. The test process will be displayed in real time in the interface.
    ![测试过程控制界面](https://raw.githubusercontent.com/ccx1024cc/lucifer/master/images/automatic_testing.png)
  (3) All completed test results can be queried through the history interface.
    ![测试历史界面](https://raw.githubusercontent.com/ccx1024cc/lucifer/master/images/history.png)
 
   
##  Service Mesh Management
  (1) Through the monitoring interface, we can customize the query conditions and generate a real-time refresh monitoring chart. In addition, the tool integrates Grafana and Jaeger, which can query the related monitoring data by clicking Statistic and Topology above respectively.
    ![统计信息可视化界面](https://raw.githubusercontent.com/ccx1024cc/lucifer/master/images/%E7%BB%9F%E8%AE%A1%E4%BF%A1%E6%81%AF%E5%8F%AF%E8%A7%86%E5%8C%96.png)
  (2) Fault management interface can be used to query and manually add and delete faults to help users quickly verify the application's fault-tolerant ability.
    ![故障管理界面](https://raw.githubusercontent.com/ccx1024cc/lucifer/master/images/fault_management.png)
  (3) Using traffic shift interface, gray deployment can be released quickly, which is convenient to build test environment.
    ![版本分流界面](https://raw.githubusercontent.com/ccx1024cc/lucifer/master/images/traffic_shift.png)


#  Relevant Technology and Design
   (1) Automatic Detection Algorithms
    ![算法流程](https://raw.githubusercontent.com/ccx1024cc/lucifer/master/images/architecture.jpg)
   (2) System architecture
    ![系统架构](https://raw.githubusercontent.com/ccx1024cc/lucifer/master/images/%E7%B3%BB%E7%BB%9F%E6%A1%86%E6%9E%B6.jpg)
   (3) Data structure
    ![数据结构](https://raw.githubusercontent.com/ccx1024cc/lucifer/master/images/%E6%95%B0%E6%8D%AE%E7%BB%93%E6%9E%84.jpg)

## Details of Design and Implementation
   [Design and Implementation of Failure Testing Tool for Microservice-based Applications](https://raw.githubusercontent.com/ccx1024cc/lucifer/master/doc/%E9%9D%A2%E5%90%91%E5%BE%AE%E6%9C%8D%E5%8A%A1%E5%BA%94%E7%94%A8%E7%9A%84%E5%BC%B9%E6%80%A7%E6%B5%8B%E8%AF%95%E5%B7%A5%E5%85%B7.pdf)

  
## Deploy
- Z3Prover
- Istio
- k8s
- jdk1.8
- MYSQL5.8

---
## Configuration Parameters (depending on how Istio and related plugins are installed)
* com.iscas.service.Jaeger中host
* com.iscas.service.Telemetry中host
* resources/static/*.html
* resources/application.properties
