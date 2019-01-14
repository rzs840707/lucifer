import com.iscas.DemoApplication;
import com.iscas.strategy.Heuristic;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = DemoApplication.class)
public class TestAlgrothrm {

    @Autowired
    private Heuristic heuristic;

    @Test
    public void test(){
        System.out.println("hello");
    }
}
