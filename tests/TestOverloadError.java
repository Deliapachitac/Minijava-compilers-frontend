class TestOverloadError {
    public static void main(String[] a) {
        System.out.println(new Controller().run());
    }
}

class Machine {
    int id;
}

class Truck extends Machine {
    int weight;
}

class Controller {
    public int process(Machine m) {
        return 1;
    }

    public int process(Truck t) {
        return 2;
    }

    public int run() {
        return 0;
    }
}