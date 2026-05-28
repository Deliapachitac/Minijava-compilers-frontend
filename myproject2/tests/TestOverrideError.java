class TestOverrideError {
    public static void main(String[] a) {
        System.out.println(new Machine().startEngine());
    }
}

class Machine {
    public int startEngine() {
        return 1;
    }
}

class Truck extends Machine {
   public boolean startEngine() {
        return true;
    }
}