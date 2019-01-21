import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TestRandom {
    public static void main(String[] args) {
        TestRandom exp = new TestRandom();
        exp.exp();
    }

    private List<Integer> expOnce() {
        boolean details = false;
        boolean productpage = false;
        boolean reviews = false;
        boolean reviews4 = false;
        boolean ratings = false;
        Random r = new Random();
        int counter = 0;
        int cnt0 = 0;
        int cnt1 = 0;
        int cnt2 = 0;
        int cnt3 = 0;
        int cnt4 = 0;
        int cnt5 = 0;
        while (!(details && productpage && reviews && ratings && reviews4)) {
            counter += 1;
            // 是否产生productpage
            int cover = 0;
            if (r.nextDouble() < 0.5) {
                cover += 1;
                productpage = true;
            }
            if (r.nextDouble() < 0.5) {
                cover += 1;
                details = true;
            }
            if (r.nextDouble() < 0.5) {
                cover += 1;
                ratings = true;
            }
            if (r.nextDouble() < 0.5) {
                cover += 1;
                reviews4 = true;
            }
            if (r.nextDouble() < 0.5 && r.nextDouble() < 0.5 && r.nextDouble() < 0.5) {
                cover += 1;
                reviews = true;
            }

            switch (cover) {
                case 0:
                    cnt0 += 1;
                    break;
                case 1:
                    cnt1 += 1;
                    break;
                case 2:
                    cnt2 += 1;
                    break;
                case 3:
                    cnt3 += 1;
                    break;
                case 4:
                    cnt4 += 1;
                    break;
                case 5:
                    cnt5 += 1;
                    break;
            }
        }
        List<Integer> result = new ArrayList<>();
        result.add(cnt0);
        result.add(cnt1);
        result.add(cnt2);
        result.add(cnt3);
        result.add(cnt4);
        result.add(cnt5);
        result.add(counter);
        return result;
    }

    private void exp() {
        long counter = 0;
        long cnt0 = 0;
        long cnt1 = 0;
        long cnt2 = 0;
        long cnt3 = 0;
        long cnt4 = 0;
        long cnt5 = 0;
        for (int i = 0; i < 10000; ++i) {
            List<Integer> tmp = expOnce();
            cnt0 += tmp.get(0);
            cnt1 += tmp.get(1);
            cnt2 += tmp.get(2);
            cnt3 += tmp.get(3);
            cnt4 += tmp.get(4);
            cnt5 += tmp.get(5);
            counter += tmp.get(6);
        }
        System.out.println("覆盖零个故障的数量：" + cnt0);
        System.out.println("覆盖一个故障的数量：" + cnt1);
        System.out.println("覆盖两个故障的数量：" + cnt2);
        System.out.println("覆盖三个故障的数量：" + cnt3);
        System.out.println("覆盖四个故障的数量：" + cnt4);
        System.out.println("覆盖五个故障的数量：" + cnt5);
        System.out.println("共执行注入次数:" + counter);
    }
}
