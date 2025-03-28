import org.junit.jupiter.api.Test;

import ast.Parser;
import ast.model.MethodDefinition;
import ast.visitors.PrintVisitor;

public class Part10Test {
    @Test
    public void testone(){
        String x = """
                void addX(){
                    x = (5 + (3 + 2));
                }
                """;
        MethodDefinition method = new Parser(x).parseMethod();
        PrintVisitor pv = new PrintVisitor();
        method.accept(pv);
        TestUtil.checkOptimization(pv.getResult(), """
void addX() {
  x = 10;
}
                """);
    }
    @Test
    public void testTwo(){
        String x = """
void foo() {
      a = 8;
      b = 30;
      c = 1;
    }
            """;
        MethodDefinition method = new Parser(x).parseMethod();
        PrintVisitor pv = new PrintVisitor();
        method.accept(pv);
        TestUtil.checkOptimization(pv.getResult(), pv.getResult());
    }
    @Test
    public void testThree(){
        String x = """
            void foo() {
                  a = (x + (8 + 5));
                }
                        """;
        MethodDefinition method = new Parser(x).parseMethod();
        PrintVisitor pv = new PrintVisitor();
        method.accept(pv);
        TestUtil.checkOptimization(pv.getResult(), """
void foo() {
  a = (x + 13);
}
                """);
    }
    @Test
    public void testFour(){
        String x = """
            void foo() {
                  x = 12;
                  a = (x + (8 + 5));
                }
                        """;
        MethodDefinition method = new Parser(x).parseMethod();
        PrintVisitor pv = new PrintVisitor();
        method.accept(pv);
        TestUtil.checkOptimization(pv.getResult(), """
void foo() {
  x = 12;
  a = 25;
}
                """);
        String c = """
                void foo() {
                    c = ((((2 - 1) * 5) + u) + ((2 * 3) - u));
                }
                """;
        method = new Parser(c).parseMethod();
        pv = new PrintVisitor();
        method.accept(pv);
        TestUtil.checkOptimization(pv.getResult(), """
void foo() {
  c = ((5 + u) + (6 - u));
}
                """);
    }
    @Test
    public void testFive(){
        String s = """
                void foo(){
                    a = 3;
                    b = (a + a);
                    c = (b + a);
                }
                """;
        MethodDefinition method = new Parser(s).parseMethod();
        PrintVisitor pv = new PrintVisitor();
        method.accept(pv);
        TestUtil.checkOptimization(pv.getResult(), """
void foo() {
  a = 3;
  b = 6;
  c = 9;
}
                """);
    }
    @Test
    public void testSix(){
        String s = """
void foo() {
  a = 2;
   if (x > (2 + 3)) {
    b = (2 + 2);
  } else {
    b = (3 + 3);
  }
  c = 8;
}
                """;
        MethodDefinition method = new Parser(s).parseMethod();
        PrintVisitor pv = new PrintVisitor();
        method.accept(pv);
        long start = System.currentTimeMillis();
        TestUtil.checkOptimization(pv.getResult(), """
void foo() {
  a = 2;
  if (x > 5) {
    b = 4;
  } else {
    b = 6;
  }
  c = 8;
}
                """);
          long total = System.currentTimeMillis() - start;
          System.out.println("It took " + total + " milliseconds to finish this test");
          s = """
void foo() {
  a = 2;
   if (1 + 0) {
    b = (2 + 2);
  } else {
    b = (3 + 3);
  }
  c = 8;
}
                """;
        start = System.currentTimeMillis();
        method = new Parser(s).parseMethod();
        pv = new PrintVisitor();
        method.accept(pv);
        TestUtil.checkOptimization(pv.getResult(), """
void foo() {
  a = 2;
  b = 4;
  c = 8;
}
                """);
        //lets make a test where the if else is constant 0 and if defines variable e as 4 and in the else it says f = e + 1
        total = System.currentTimeMillis() - start;
        System.out.println("It took " + total + " milliseconds to finish this test");
        s = """
            void foo() {
              a = 2;
               if (0 + 0) {
                b = (2 + 2);
                e = 4;
              } else {
                b = (3 + 3);
                f = (e + 1);
              }
              c = 8;
            }
                            """;
            start = System.currentTimeMillis();
            method = new Parser(s).parseMethod();
            pv = new PrintVisitor();
            method.accept(pv);
            TestUtil.checkOptimization(pv.getResult(), """
    void foo() {
      a = 2;
      b = 6;
      f = (e + 1);
      c = 8;
    }
                    """);
            //lets make a test where the if else is constant 0 and if defines variable e as 4 and in the else it says f = e + 1
            total = System.currentTimeMillis() - start;
            System.out.println("It took " + total + " milliseconds to finish this test");
    }
    @Test 
    public void testSeven(){
        String s = """
void foo() {
  a = 2;
   while (x > (2 + 3)) {
    b = (2 + 2);
  }
  c = 8;
}
                """;
        long start = System.currentTimeMillis();
        MethodDefinition method = new Parser(s).parseMethod();
        PrintVisitor pv = new PrintVisitor();
        method.accept(pv);
        TestUtil.checkOptimization(pv.getResult(), """
void foo() {
  a = 2;
  while (x > 5) {
    b = 4;
  }
  c = 8;
}
                """);
            long total = System.currentTimeMillis() - start;
            System.out.println("It took " + total + " milliseconds to finish this test");
    }
    @Test
    public void testEight(){
      long start = System.currentTimeMillis();

      String s = """
          void foo(){
            x = 1;
            y = (1 + 0);
            c = (2 + 1);
            while (x < 10){
              a = 4;
              b = x;
              e = 5;
              x = (x + 1);
            }
            y = (a + 1);
            z = (b + 1);
          }
          """;

      MethodDefinition method = new Parser(s).parseMethod();
      PrintVisitor pv = new PrintVisitor();
      method.accept(pv);
      TestUtil.checkOptimization(pv.getResult(), """
void foo() {
  x = 1;
  y = 1;
  c = 3;
  while (x < 10) {
    a = 4;
    b = x;
    e = 5;
    x = (x + 1);
  }
  y = (a + 1);
  z = (b + 1);
}
              """);


      long finish = System.currentTimeMillis() - start;
      System.out.println("It took " + finish + " milliseconds to finish this test");
    }
    @Test
    public void testNine(){
      long start = System.currentTimeMillis();

      String s = """
          void foo(){
            x = 2;
            if (u < 10){
              b = (x + 3);
            } else {
              b = 3;
            }
            y = (a + 1);
            z = (b + 1);
          }
          """;

      MethodDefinition method = new Parser(s).parseMethod();
      PrintVisitor pv = new PrintVisitor();
      method.accept(pv);
      TestUtil.checkOptimization(pv.getResult(), """
void foo() {
  x = 2;
  if (u < 10) {
    b = 5;
  } else {
    b = 3;
  }
  y = (a + 1);
  z = (b + 1);
}
              """);


      long finish = System.currentTimeMillis() - start;
      System.out.println("It took " + finish + " milliseconds to finish this test");
    }
    @Test
    public void testTen(){
        String s = """
void foo() {
  a = 2;
   if (1 + 0) {
    b = (2 + 2);
  } else {
    b = (3 + 3);
  }
  c = (1 + b);
}
                """;
        MethodDefinition method = new Parser(s).parseMethod();
        PrintVisitor pv = new PrintVisitor();
        method.accept(pv);
        long start = System.currentTimeMillis();
        TestUtil.checkOptimization(pv.getResult(), """
void foo() {
  a = 2;
  if (x > 5) {
    b = 4;
  } else {
    b = 6;
  }
  c = (1 + b);
}
                """);
          long total = System.currentTimeMillis() - start;
          System.out.println("It took " + total + " milliseconds to finish this test");
  }
}
// x = 1;
//   c = 3;
//  [ while (x < 10) {
//     a = 4;
//     b = x;
//     e = 5;
//     x = (x + 1);
//   }
//   y = (a + 1)];
//   z = (b + 1);

