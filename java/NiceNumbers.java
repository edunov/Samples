import java.math.BigInteger;

public class NiceNumbers {

  static BigInteger ten = new BigInteger("10");
  static BigInteger[] digits = new BigInteger[10];

  public static BigInteger sum(boolean[] mask, BigInteger current, int depth){
    if (depth == 0) {
      return current;
    }
    BigInteger res = null;
    for(int i = 0; i < 10; i++) {
      if (!mask[i]) {
        BigInteger next = current.multiply(ten).add(digits[i]);
        mask[i] = true;
        BigInteger r = sum(mask, next, depth-1);
        mask[i] = false;
        if (res == null) {
          res = r;
        } else {
          res = res.add(r);
        }
      }
    }
    return res;
  }

  public static BigInteger sum(int depth){
    boolean[] mask = new boolean[10];
    BigInteger res = null;
    for(int i = 1; i < 10; i++) {
      mask[i] = true;
      BigInteger r = sum(mask, digits[i], depth-1);
      mask[i] = false;
      if (res == null) {
        res = r;
      } else {
        res = res.add(r);
      }
    }
    return res;
  }

  public static void main(String[] args) {
    for(int i = 0; i < 10; i++) {
      digits[i] = new BigInteger("" + i);
    }
    for (int power = 1; power <= 10; power++) {
      BigInteger total = ten.pow(power).multiply(ten.pow(power).subtract(new BigInteger("1"))).divide(new BigInteger("2"));

      BigInteger s = sum(1);
      for (int i = 2; i <= power; i++) {
        s = s.add(sum(i));
      }
      System.out.println(power + " : " + total.subtract(s) );
    }

  }


}
