import com.iscas.DemoApplication;
import com.iscas.bean.Span;
import com.iscas.service.Jaeger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class TestJaeger {

    @Autowired
    private Jaeger jaeger;

    @Test
    public void test() {
        List<Span> roots = jaeger.sampleErr(40, 20);
        System.out.println("");
    }
}
