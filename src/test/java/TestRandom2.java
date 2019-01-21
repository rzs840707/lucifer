import java.util.*;

public class TestRandom2 {

    public static void main(String[] args) {
        TestRandom2 r = new TestRandom2();
        r.exp();
    }

    private List<Integer> expOnce() {
        // 覆盖details,ratings,productpage,reviews4
        boolean details = false;
        boolean ratings = false;
        boolean productpage = false;
        boolean reviews4 = false;

        List<Integer> result = new ArrayList<>();

        int counter = 0;
        int repeat = 0;
        Random r = new Random();
        while (!(details && ratings && productpage && reviews4)) {
            counter += 1;
            int seed = r.nextInt(4);
            switch (seed) {
                case 0:
                    if (details)
                        repeat += 1;
                    details = true;
                    break;
                case 1:
                    if (ratings)
                        repeat += 1;
                    ratings = true;
                    break;
                case 2:
                    if (productpage)
                        repeat += 1;
                    productpage = true;
                    break;
                case 3:
                    if (reviews4)
                        repeat += 1;
                    reviews4 = true;
                    break;
            }
        }
        result.add(repeat);
        result.add(counter);

        counter = 0;
        repeat = 0;
        boolean reviews1 = false;
        boolean reviews2 = false;
        boolean reviews3 = false;
        Set<String> tokens = new HashSet<>();
        while (!(reviews1 && reviews2 && reviews3)) {
            counter += 1;
            reviews1 = reviews2 = reviews3 = false;
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 3; ++i) {
                int seed = r.nextInt(7);
                switch (seed) {
                    case 4:
                        reviews1 = true;
                        break;
                    case 5:
                        reviews2 = true;
                        break;
                    case 6:
                        reviews3 = true;
                        break;
                }
                sb.append(seed);
            }
            if (tokens.contains(sb.toString()))
                repeat += 1;
            else
                tokens.add(sb.toString());
        }
        result.add(repeat);
        result.add(counter);
        return result;
    }

    private void exp() {
        long repeat1 = 0;
        long repeat2 = 0;
        long total1 = 0;
        long total2 = 0;
        for (int i = 0; i < 10000; ++i) {
            List<Integer> r = expOnce();
            repeat1 += r.get(0);
            total1 += r.get(1);
            repeat2 += r.get(2);
            total2 += r.get(3);
        }

        System.out.println("重复数量：" + repeat1);
        System.out.println("长度为1的服务覆盖注入次数:" + total1);
        System.out.println("重复数量：" + repeat2);
        System.out.println("长度为3的服务覆盖注入次数：" + total2);
    }
}
