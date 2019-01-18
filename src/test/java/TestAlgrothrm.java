import com.iscas.DemoApplication;
import com.iscas.bean.result.*;
import com.iscas.dao.*;
import com.iscas.strategy.Heuristic;
import com.iscas.util.Time;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class TestAlgrothrm {

    @Autowired
    private DetectResultDAO detectResultDAO;
    @Autowired
    private TimeoutResultDAO timeoutResultDAO;
    @Autowired
    private RetryResultDAO retryResultDAO;
    @Autowired
    private CircuitBreakerResultDAO circuitBreakerResultDAO;
    @Autowired
    private BulkHeadResultDAO bulkHeadResultDAO;
    @Autowired
    private SummaryDAO summaryDAO;


    private void loadData() throws InterruptedException {
//        // 注入数据
////        List<DetectResult> r1 = new ArrayList<>();
////        for (int i = 1; i <= 5; ++i) {
////            System.out.println("注入数据" + i);
////            r1.add(new DetectResult("1", i, "第" + i + "条结果", Time.getCurTimeStr()));
////            Thread.sleep(2 * 1000);
////        }
////        detectResultDAO.saveAll(r1);
////
////        List<TimeoutResult> r2 = new ArrayList<>();
////        for (int i = 1; i <= 5; ++i) {
////            System.out.println("注入数据" + i);
////            r2.add(new TimeoutResult("1", i, "srcname", "tarname",
////                    0.8, "第" + i + "条结果", Time.getCurTimeStr()));
////            Thread.sleep(2 * 1000);
////        }
////        timeoutResultDAO.saveAll(r2);
////
////        List<RetryResult> r3 = new ArrayList<>();
////        for (int i = 1; i <= 5; ++i) {
////            System.out.println("注入数据" + i);
////            r3.add(new RetryResult("1", i, "url", "srcname", "tarname",
////                    2, "第" + i + "条结果", Time.getCurTimeStr()));
////            Thread.sleep(2 * 1000);
////        }
////        retryResultDAO.saveAll(r3);
////
////        List<CircuitBreakResult> r4 = new ArrayList<>();
////        for (int i = 1; i <= 5; ++i) {
////            System.out.println("注入数据" + i);
////            r4.add(new CircuitBreakResult("1", i, "srcname", "tarname",
////                    0.8, "第" + i + "条结果", Time.getCurTimeStr(), new String[]{"aaa", "bbb"}));
////            Thread.sleep(2 * 1000);
////        }
////        circuitBreakerResultDAO.saveAll(r4);
////
////        List<BulkHeadResult> r5 = new ArrayList<>();
////        for (int i = 1; i <= 5; ++i) {
////            System.out.println("注入数据" + i);
////            r5.add(new BulkHeadResult("1", i, "srcname", "tarname",
////                    0.8, 0.8, "第" + i + "条结果", Time.getCurTimeStr()));
////            Thread.sleep(2 * 1000);
////        }
////        bulkHeadResultDAO.saveAll(r5);
        summaryDAO.save(new Summary("1",Time.getCurTimeStr(),Time.getCurTimeStr(),
                100,400,5,5,5,5));
    }

    @Test
    public void test() throws InterruptedException {
        loadData();
        Summary r = summaryDAO.findById("1");
        System.out.println(r);
    }
}
