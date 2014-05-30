double pi() {
    return ( ((((1 - (1.0/3) + (1.0/5)) - (1.0/7) + (1.0/9) - (1.0/11.0)) + (1.0/13))) - (1.0/15) + (1.0/17) )*4;
}

void printSomeNumbers() {
    print 1;
    print 2;
    print 3.0;
}

int intSum(int a, int b) {
    return a + b;
}

double doubleIntSum(int a, double b) {
     return a + b;
}

double powerTen(double num) {
    int power;
    power = 10;

    num = num*power;

    return num;
}

void main() {
    print pi();
    printSomeNumbers();
    print intSum(3,intSum(2,-10.2));
    print doubleIntSum(intSum(2,-10),doubleIntSum(1553, -23.54));
    print powerTen(1345.22);

    int a;

    a = 7.8;

    print a;
}

