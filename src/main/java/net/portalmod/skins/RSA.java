package net.portalmod.skins;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class RSA {
    private static byte[] toByteArray(char[] input) {
        byte[] output = new byte[input.length];
        for(int i = 0; i < input.length; i++)
            output[i] = (byte)input[i];
        return output;
    }

    private static char[] toCharArray(byte[] input) {
        char[] output = new char[input.length];
        for(int i = 0; i < input.length; i++)
            output[i] = (char)input[i];
        return output;
    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for(int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hex) {
        byte[] val = new byte[hex.length() / 2];
        for(int i = 0; i < val.length; i++) {
            int index = i * 2;
            int j = Integer.parseInt(hex.substring(index, index + 2), 16);
            val[i] = (byte)j;
        }
        return val;
    }

    public static void main(String[] args) {
        String cipher = "WE4TdzlBb3jx+fDDproYV1vn7Zs9t7jyDCSF9JTcb+V9hiUC9TxRpYpZC0EHKUcnAL+KSqY/GdKtNERGJP/Z7qIYQxghO6v7qUlZuDCxK8/3Bl4o0gaY1VgT44TMZyvgB0lKwh9DP5ushxO3jOYebA3PffwkSzBDCoZ4eTwj+yG89p1ICV8MELnCsRQoLckdCZqGw9Z1i/XT7XgZK+VnWmB7pvTiCA50KSaD9IDyYm1LOCiFBQSwEM59GdC2Q/kbozbKcHwMmsamlKNVytFEMKTzjol+eWzble8zCqBiGOr9qn+RSLMXx+kFUfMC6aBRa+HNNr4pEU93ESXj4Jb6Mn3ssTqqu6fIgvqBIVDptjpazdP+UbEctK61hg6E5tdlvFXV4zdzvphHqlonRcUqIQP8n7nHqhEdO7wHEQPS2tAUmMQzT8e/8GvOSzmnaJQl0ZRbn/IFRCdkZwSTlmXB3YYRICqIAfcmWqt7QH/e0sCg0Ew78ZKz0q9YiZVBPHf81TpgJ2S67xhKuJSN3fk5/xh+oCDrZHDu3Aa9E+/cRxeIGdCm+Tl9huhJ1zT9EEjHBff4pGql9uo7zpypLPZw1qfXSnCF5awIE4QGIJb16X6MBNM7qFj34OB9uhyPqFG/L9LLzeYG15+/4HU7xm7ZWInvLs16Up6DcaE0FzY4zZk=";
//        String privateKeyHex = "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";
        String privateKeyHex = "30820942020100300d06092a864886f70d01010105000482092c308209280201000282020100dbba7e03d349a48961d78244c39b16aa3bc7741ac4984af4875a6a1c2f70807d5302a136eb58772e14acbe8f7faefc4a07d4fbdc49db3075850f1ff19dae4b5f6c95ff24d666728b2331e5e41a083c6a4ad3cdd69a52df2763baefca07b6eee51b4e5fd55789a1ddaf4b3f0f2271fd1d06fdd531baef37d73bf65d4086c6e130bdc1e81bcfc29b310b69fb6ef27c67bff5ef35c6f40d32520ef92b7ded178174dc52114d071d79d0633b1ce3aa16f974386734e0c0eb249873a5bc01f7aec837b15a7f94992f7ba6b27a748e8d08b0ab4184c2828e2a652b2203d5896e8d2aa72cdd8e73763df2d59784a1020550073e96c02873c8fcce42873e4b73aea5b2995fa6e7659e2f7974cddc309ca7d565799371a2dd0655195eeec377e33be779ef3bd8e42b4dcb5b35a09cc0861ba3a9c22ad2535fa6f5fa1f7bae7ad7b9bfa342910a28210d98a240cb10e5881ef1b1c3a1c63f1d04903526f8fa409b6b40a99354bc2d05ad04d977394f406dc8d83b5d958d806919e02cc0592a8d27950961c9c8f50fa66feb083163c1a8668e85811b4762dc841118544b2cbaa5a3bef0c63f8a7a059e9a986842adef856ac0f2c6855afcd048528a835fd1dc2f4bc04418c83afc74d0cff4aa5c0dd0aeb1fdc008daff0f86f4b4b824b70d36ac02a07ff92fedd6091973026aad3c76797bd7654ddd0ae4611bc07234d63916589cc4a7edc302030100010282020031e61925ff3e931a0bd928213b96a5c00511578198b1c91bdcfd2069c430354dc0a3313f4a94ed83ce7828526839b1db9209c12496b52b3d14f98954926d797a732b6124d977ac7d82819b6680aeec216b5acc3cf710e8d2862876ba17a4687e290454691b970ae6501fb373596c2f81ff0b77f5f230cf2a96d86cf6f1648ad149d69642729d0c2748234a5138ca1db98f3d42a62ad8305c484037ab42437b47b19d0586cb9ddb0302f5f2aa94fee0a98f0189159d5a091681f4da4c4c162a2bd39feca72f5e84232816fd0f3e6201d4f26d16cb1543f573a526dfc07515a3e11124436c5f26d455c332006a78106384c8315d3e661f5b2bea90b00bd65694c72a03b402197f586df7a22755777cdf780112a3ded0f175675c29bdb3238d0c91d20448fbc840d751491b17de6ecd29c9441d292cc785b2fb1cf319369e244177e100699d91ca34a13b5674a77771906e4c8ec8aabe121d7ad8b7a161dc465172809e7b1ce540fd6ca51af4f987c167e61f3288f12288a5404e4041d83be78174bbe3e1eee8b3afdfb3e563f777f20d52daed11359c14fbd881506dbe80538c308a56629d0941ebcd27d7b5b471cf7cf8043fdbd65e40830a7ca8a508a0ea06dce7ac6d447273d71becca825496add72e4a337f618d3fa36af9bdbda9d7817019018e658a11ccd2cceea513ebbe44543b83af77bca54fb3432f570f8f1c2dc1810282010100e1fa5a18ad00ed1f85e5f44872a51c4b805d74e011c2b945f409f2ac03968962079b364911f24708b5649483069f231ab22113089d459db8fb1bc5dca817ec8624e2f717763aa9e74bdb87a50231ef975a5c9c4026de4f0d182c6936a0c119760fbd29e4dd1cdc1a63fbeb99e6419571112fcc176a6ffb83811ae833e657caea0a0ec2a936759625b5dea8b4452972d80bd50107a7c88f63d392a65087b9bb11231443597380b23cb82dd5bfd654b3f1032fb3d789d421ea40bc31e0d8a465bfe9c48496ee7534665a2b73d5eb36b208305a65b89b499b627b988b024b92c2ae12b05283730837e9f09668a0ac04cbe37e8354d4ec2c8b49b9b344540f92b5030282010100f8eb97b5e02e042614b7ef4f6504aeb7f6dbbea3a6def290d79db5802147f109130aa08d4ac017e7e6dc70b866d9577fd343bef06c42372121e9cabea2e6b1598faaf4de87d6ada7f351c85c95392135bd5712971f02493aeae48b75f69d8815aa6534fca6510a6ca1bad83769ecfc2d4f998b2e2cb2d09aaedf64f4d26ca000e727f9ffac599f30ec4a2bf94b094c418aeb8c481da6f1b87d8de94ac06cdb307e4b07950d84060eb44214bcb17a3ad682c562ecf8f67bb0c6fee69ac68d73fe1ba1d84d49c81719e9452942f34f411d202ca19626ee60f7753dbee5199434971f4a436b185eb5aa58f704ebfd169ab6589ada4a02b0f383ae2faf54528aa8410282010100be389ea4cce4cdb64e613edbc95dc14eb0f47a90af82a9b0fff76bc66e92c67cb5886b0ee1437f960e13678c50039f6a366deef6be64be9adb0e0ae56ebe0cc4f2c1f75b51939a37f03c1097cf1c37e1c117da138a647248c48385e18d3a5ef369d64330bb0c331f27f104e71cd33237e64ec4cf2532b49683142248a5ea5b0ddd7f854f295f9c0f8c63acc16ac2b440b6dd0babe509c7e23153ee1a35d61b69168d551034512e383c95a0ab8680f0a3bd99e71a559433245c6fced0c5acc555e23321a5677f53a7a0d998fd86ad18fa19b1ce94b26b2898a30fca5c193fa60f1ffe3ac71a0addfe581e545e83ced9ce5b4db41833e25f31a55708fa75cd79ff028201003f28e03cc8de4435396a967149cb8f5da4688b8dd0c89f39bb4c0c02afef262ccc451509e4deaaf22fb4dd711cbfa7210b05148cc29b668ba4a442ca6e6124bcf9e441efb8fdf2e03bb013abd23d45bcf20706f38b6db4111c0ad38c220044c8628cfd8de1d18c58960ce7d1dfb1bf26ec1ca4a119bc618bee3546d38c14fde05cc6dafb51cb07c68d1e20dc477adb7b81f95485f3e8294b4ba8577a9d2ac1cf232c7c87087fe85d90c577a78d4cdb6314996b097ffe62d22f1bfc7d6745fb198dff59f7fa71a7d2ee4a9b369eb00c708c7a66a1c7cd1293978f0c41eb649c977fdc03114884b789babb2ae78a37531853912f343a4c95ed62a43cd3a382054102820100669f6943c9b5955ab5289d03c7aa26c620d2ab3d07e0ed32d5ac530cc61990dfb86f68afc0019447001b98eb1e75996b5046010e3ba125fc9905410b16fc18bedc1d743c03a536484e1a51d21fe1f193af294d9a79c7c55ad6537a2e7dccf9f146a70b477c5a888bfdf893e01611fd2dc03beb2f69d3b4741251634d757e51b5c6b5819a88920b40843cea957614f9d7272e27cfbfb1579c5b2b0d427a1c1cdef5c741d7b93d571c7057bb82fb25b76bc741de80a216506448ea101b911b723b45e60ef43bc8e091df1038e513e0a99f4f60b7e33d5d0860944198e545c438b97c1903fe62b37df6ead0b751fd0803192747cacf812cff39ed15ac2abf4729d4";

        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            byte[] cipherBytes = Base64.getDecoder().decode(cipher);
            System.out.println(Arrays.toString(cipherBytes));

//            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyHex));
            EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(hexToBytes(privateKeyHex));
            PrivateKey privateKey = keyFactory.generatePrivate(privateKeySpec);

            Cipher decryptCipher = Cipher.getInstance("RSA/ECB/OAEPPadding");
            OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1",
                    new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);

            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);

            byte[] decryptedMessageBytes = decryptCipher.doFinal(cipherBytes);
            System.out.println(new String(toCharArray(decryptedMessageBytes)));
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }
}