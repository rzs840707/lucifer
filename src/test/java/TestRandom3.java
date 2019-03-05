import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TestRandom3 {
    private String[] services;
    private List<List<String>> traces;


    public static void main(String[] args) {
        TestRandom3 exp = new TestRandom3();
        long total = 0;
        for (int i = 0; i < 10000; ++i) {
            total += exp.expOnce();
        }
        System.out.println(total);
    }


    private TestRandom3() {
        // 服务
        this.services = new String[]{
                "ts-admin-basic-info-service", "ts-admin-order-service", "ts-admin-route-service", "ts-admin-travel-service",
                "ts-admin-user-service", "ts-assurance-service", "ts-basic-service", "ts-cancel-service", "ts-config-service",
                "ts-consign-price-service", "ts-consign-service", "ts-contacts-service", "ts-execute-service", "ts-food-map-service",
                "ts-food-service", "ts-inside-payment-service", "ts-login-service", "ts-news-service", "ts-notification-service",
                "ts-order-other-service", "ts-order-service", "ts-payment-service", "ts-preserve-other-service", "ts-preserve-service",
                "ts-price-service", "ts-rebook-service", "ts-register-service", "ts-route-plan-service", "ts-route-service",
                "ts-seat-service", "ts-security-service", "ts-sso-service", "ts-station-service", "ts-ticket-office-service",
                "ts-ticketinfo-service", "ts-train-service", "ts-travel-plan-service", "ts-travel-service", "ts-travel2-service",
                "ts-verification-code-service", "ts-voucher-service",

                // 冗余服务
                "ts-verification-code-service-back",
                "ts-verification-code-service-back1"
        };
        // 调用链
        // 按照trace统计
        List<List<String>> traces = new ArrayList<>();
        //登陆
        List<String> trace = Arrays.asList("ts-login-service", "ts-sso-service");
        traces.add(trace);
        //验证码
        trace = Arrays.asList("ts-verification-code-service", "ts-verification-code-service-back",
                "ts-verification-code-service-back1"); //冗余服务
        traces.add(trace);
        // 查票
        trace = Arrays.asList("ts-basic-service", "ts-config-service",
                "ts-order-service", "ts-price-service", "ts-route-service",
                "ts-seat-service", "ts-station-service", "ts-ticketinfo-service",
                "ts-train-service", "ts-travel-service");
        traces.add(trace);
        // 保险查询
        trace = Arrays.asList("ts-assurance-service");
        traces.add(trace);
        // 联系人查询
        trace = Arrays.asList("ts-contacts-service");
        traces.add(trace);
        // 查询食物
        trace = Arrays.asList("ts-food-map-service", "ts-food-service", "ts-route-service", "ts-station-service", "ts-travel-service");
        traces.add(trace);
        // 订票
        trace = Arrays.asList("ts-assurance-service", "ts-basic-service", "ts-config-service", "ts-consign-price-service",
                "ts-consign-service", "ts-consign-price-service", "ts-contacts-service", "ts-food-service", "ts-notification-service",
                "ts-order-service", "ts-preserve-service", "ts-price-service", "ts-route-price", "ts-seat-price",
                "ts-security-service", "ts-sso-service", "ts-station-service", "ts-ticketinfo-service", "ts-train-service",
                "ts-travel-service");
        traces.add(trace);
        // 查询订票
        trace = Arrays.asList("ts-order-other-service", "ts-sso-service", "ts-station-service");
        traces.add(trace);
        // 托运
        trace = Arrays.asList("ts-consign-service");
        traces.add(trace);
        // 取消车票
        trace = Arrays.asList("ts-order-service", "ts-sso-service");
        traces.add(trace);
        // 支付车票
        trace = Arrays.asList("ts-inside-payment-service", "ts-order-service");
        traces.add(trace);
        // 查询差价
        trace = Arrays.asList("ts-inside-payment-service", "ts-order-service");
        traces.add(trace);
        // 改迁
        trace = Arrays.asList("ts-basic-service", "ts-config-service", "ts-order-service", "ts-price-service", "ts-rebook-service",
                "ts-route-service", "ts-seat-service", "ts-station-service", "ts-ticketinfo-service", "ts-train-service",
                "ts-travel-service");
        traces.add(trace);
        // 补差价
        trace = Arrays.asList("ts-basic-service", "ts-config-service", "ts-inside-payment-service", "ts-order-service",
                "ts-price-service", "ts-rebook-service", "ts-route-service", "ts-seat-service", "ts-station-service",
                "ts-ticketinfo-service", "ts-train-service", "ts-travel-service");
        traces.add(trace);
        // 按条件查询车票
        trace = Arrays.asList("ts-basic-service", "ts-config-service", "ts-order-other-service", "ts-order-service",
                "ts-price-service", "ts-route-plan-service", "ts-route-service", "ts-seat-service", "ts-station-service",
                "ts-ticketinfo-service", "ts-train-service", "ts-travel-service", "ts-travel-plan-service", "ts-travel2-service");
        traces.add(trace);
        // 检票与进展
        trace = Arrays.asList("ts-execute-service", "ts-order");
        traces.add(trace);
        // 打印发票
        trace = Arrays.asList("ts-voucher-service");
        traces.add(trace);

        // 管理员相关调用链
        // 查询订单
        trace = Arrays.asList("ts-admin-order-service");
        traces.add(trace);
        // 更新与删除订单
        trace = Arrays.asList("ts-admin-order-service", "ts-order-service");
        traces.add(trace);
        // 增删改查路由
        trace = Arrays.asList("ts-admin-route-service", "ts-route-service");
        traces.add(trace);
        // 更新travel
        trace = Arrays.asList("ts-admin-travel-service", "ts-travel-service", "ts-travel2-service");
        traces.add(trace);
        // 增删改查用户
        trace = Arrays.asList("ts-admin-user-service");
        traces.add(trace);
        // 管理联系人
        trace = Arrays.asList("ts-admin-basic-info-service", "ts-contacts-service");
        traces.add(trace);
        // 管理车辆信息
        trace = Arrays.asList("ts-admin-basic-info-service", "ts-train-service");
        traces.add(trace);
        // 管理车站信息
        trace = Arrays.asList("ts-admin-basic-info-service", "ts-station-service");
        traces.add(trace);
        // 管理票价信息
        trace = Arrays.asList("ts-admin-basic-info-service", "ts-price-service");
        traces.add(trace);
        // 配置管理
        trace = Arrays.asList("ts-admin-basic-info-service", "ts-config-service");
        traces.add(trace);
        this.traces = traces;

    }

    private long expOnce() {
        Boolean[] flags = new Boolean[traces.size()];
        for (int i = 0; i < flags.length; ++i)
            flags[i] = false;
        int coverCnt = 0;
        long injectTimes = 0;
        Random r = new Random();

        while (coverCnt < traces.size()) {
            injectTimes += 1;
            String service1 = services[r.nextInt(services.length)];
            String service2 = services[r.nextInt(services.length)];
            String service3 = services[r.nextInt(services.length)];
            String service4 = services[r.nextInt(services.length)];
            for (int i = 0; i < traces.size(); ++i) {
                List<String> trace = traces.get(i);
                if (!trace.contains(service1))
                    continue;
                if (!flags[i]) {

                    // 判断冗余
                    if (i == 1) {
                        if (trace.contains(service1)
                                && trace.contains(service2)
                                && trace.contains(service3)
                                && trace.contains(service4)) {
                            flags[i] = true;
                            coverCnt += 1;
                        }
                    } else {
                    flags[i] = true;
                    coverCnt += 1;
                    }
                }
            }
        }
        return injectTimes;
    }
}
