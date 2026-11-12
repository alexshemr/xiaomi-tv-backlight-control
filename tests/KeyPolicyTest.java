package local.ha.tvbacklight;

public final class KeyPolicyTest {
    public static void main(String[] args) {
        int[] reserved = {0, 3, 4, 19, 20, 21, 22, 23, 24, 25, 26, 66, 82, 91,
            160, 164, 176, 177, 219, 223, 224, 231};
        for (int key : reserved) {
            if (KeyPolicy.canAssign(key)) throw new AssertionError("Reserved key " + key);
        }
        int[] assignable = {85, 126, 127, 183, 184, 185, 186, 188, 189, 190, 191};
        for (int key : assignable) {
            if (!KeyPolicy.canAssign(key)) throw new AssertionError("Assignable key " + key);
        }
        if (KeyPolicy.canAssign(-1)) throw new AssertionError("Negative key");
        System.out.println("Key assignment policy passed");
    }
}
