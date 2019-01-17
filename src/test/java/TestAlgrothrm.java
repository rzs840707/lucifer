import com.iscas.DemoApplication;
import com.iscas.strategy.Heuristic;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class TestAlgrothrm {

    @Autowired
    private Heuristic heuristic;

//    @Test
//    public void test(){
//        heuristic.run();
//    }

    @Test
    public void test() throws IOException, URISyntaxException {
        System.out.println(ResourceUtils.getFile("classpath:static/tmp").getAbsolutePath());
    }
}
