package enc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.File;
import java.io.FileNotFoundException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.Iterator;
import java.util.Scanner;
import java.util.regex.Pattern;

import java.lang.StringBuilder;

import static enc.Reverse.*;

public class Encoder {

    public Encoder() {
        generateLists();
    }

    protected int[] encode(int[] text) {
        Random rando = new Random();
        text = reverseInts(text);
        int[] modded = new int[text.length * 2 + 4];
        for (int i = 0; i < 4; i += 1) {
            modded[i] = 0xFF;
        }
        // try unpacking loop
        int k = 4;
        for (; k + 32 < modded.length; k += 32) {
            int mod0 = _primes[rando.nextInt(_primes.length - 1)];
            int mod1 = _primes[rando.nextInt(_primes.length - 1)];
            int mod2 = _primes[rando.nextInt(_primes.length - 1)];
            int mod3 = _primes[rando.nextInt(_primes.length - 1)];
            int mod4 = _primes[rando.nextInt(_primes.length - 1)];
            int mod5 = _primes[rando.nextInt(_primes.length - 1)];
            int mod6 = _primes[rando.nextInt(_primes.length - 1)];
            int mod7 = _primes[rando.nextInt(_primes.length - 1)];
            int mod8 = _primes[rando.nextInt(_primes.length - 1)];
            int mod9 = _primes[rando.nextInt(_primes.length - 1)];
            int mod10 = _primes[rando.nextInt(_primes.length - 1)];
            int mod11 = _primes[rando.nextInt(_primes.length - 1)];
            int mod12 = _primes[rando.nextInt(_primes.length - 1)];
            int mod13 = _primes[rando.nextInt(_primes.length - 1)];
            int mod14 = _primes[rando.nextInt(_primes.length - 1)];
            int mod15 = _primes[rando.nextInt(_primes.length - 1)];
            int i0 = modShift(text[(k - 4) / 2], mod0);
            int i1 = modShift(text[(k - 2) / 2], mod1);
            int i2 = modShift(text[k / 2], mod2);
            int i3 = modShift(text[(k + 2) / 2], mod3);
            int i4 = modShift(text[(k + 4) / 2], mod4);
            int i5 = modShift(text[(k + 6) / 2], mod5);
            int i6 = modShift(text[(k + 8) / 2], mod6);
            int i7 = modShift(text[(k + 10) / 2], mod7);
            int i8 = modShift(text[(k + 12) / 2], mod8);
            int i9 = modShift(text[(k + 14) / 2], mod9);
            int i10 = modShift(text[(k + 16) / 2], mod10);
            int i11 = modShift(text[(k + 18) / 2], mod11);
            int i12 = modShift(text[(k + 20) / 2], mod12);
            int i13 = modShift(text[(k + 22) / 2], mod13);
            int i14 = modShift(text[(k + 24) / 2], mod14);
            int i15 = modShift(text[(k + 26) / 2], mod15);
            modded[k] = modShift(mod0, largePrime);
            modded[k + 1] = i0;
            modded[k + 2] = modShift(mod1, largePrime);
            modded[k + 3] = i1;
            modded[k + 4] = modShift(mod2, largePrime);
            modded[k + 5] = i2;
            modded[k + 6] = modShift(mod3, largePrime);
            modded[k + 7] = i3;
            modded[k + 8] = modShift(mod4, largePrime);
            modded[k + 9] = i4;
            modded[k + 10] = modShift(mod5, largePrime);
            modded[k + 11] = i5;
            modded[k + 12] = modShift(mod6, largePrime);
            modded[k + 13] = i6;
            modded[k + 14] = modShift(mod7, largePrime);
            modded[k + 15] = i7;
            modded[k + 16] = modShift(mod8, largePrime);
            modded[k + 17] = i8;
            modded[k + 18] = modShift(mod9, largePrime);
            modded[k + 19] = i9;
            modded[k + 20] = modShift(mod10, largePrime);
            modded[k + 21] = i10;
            modded[k + 22] = modShift(mod11, largePrime);
            modded[k + 23] = i11;
            modded[k + 24] = modShift(mod12, largePrime);
            modded[k + 25] = i12;
            modded[k + 26] = modShift(mod13, largePrime);
            modded[k + 27] = i13;
            modded[k + 28] = modShift(mod14, largePrime);
            modded[k + 29] = i14;
            modded[k + 30] = modShift(mod15, largePrime);
            modded[k + 31] = i15;
        }
        for (; k + 8 < modded.length; k += 8) {
            int mod0 = _primes[rando.nextInt(_primes.length - 1)];
            int mod1 = _primes[rando.nextInt(_primes.length - 1)];
            int mod2 = _primes[rando.nextInt(_primes.length - 1)];
            int mod3 = _primes[rando.nextInt(_primes.length - 1)];
            int i0 = modShift(text[(k - 4) / 2], mod0);
            int i1 = modShift(text[(k - 2) / 2], mod1);
            int i2 = modShift(text[k / 2], mod2);
            int i3 = modShift(text[(k + 2) / 2], mod3);
            modded[k] = modShift(mod0, largePrime);
            modded[k + 1] = i0;
            modded[k + 2] = modShift(mod1, largePrime);
            modded[k + 3] = i1;
            modded[k + 4] = modShift(mod2, largePrime);
            modded[k + 5] = i2;
            modded[k + 6] = modShift(mod3, largePrime);
            modded[k + 7] = i3;
        }
        for (; k < modded.length; k += 2) {
            int mod = _primes[rando.nextInt(_primes.length - 1)];
            int i = modShift(text[(k - 4) / 2], mod);
            modded[k] = modShift(mod, largePrime);
            modded[k + 1] = i;
        }
        modded = makeByteSized(modded);
        return modded;
    }

    private int[] makeByteSized(int[] modded) {
        int mask = 0xFF;
        int[] newArr = new int[modded.length * 2];
        int upper, lower, num;
        for (int i = 0; i < newArr.length; i += 2) {
            num = modded[i / 2];
            upper = num >> 8;
            lower = num & mask;
            newArr[i] = upper;
            newArr[i + 1] = lower;
        }
        return newArr;
    }

    private static int modShift(int c, int mod) {
        return (c * (mod - 1)) % mod;
    }

    protected int[] decode(int[] encoded) {
        int[] decoded = new int[(encoded.length - 8)/ 4];
        for (int k = 0; k < 8; k += 2) {
            int upper = encoded[k];
            int lower = encoded[k + 1];
            int num = (upper << 8) | lower;
            if (num != 0xFF) {
                return null;
            }
        }
        for (int i = 8; i < encoded.length; i += 4) {
            int modUpper = encoded[i];
            int modLower = encoded[i + 1];
            int mod = (modUpper << 8) | modLower;
            mod = modShift(mod, largePrime);
            int xUpper = encoded[i + 2];
            int xLower = encoded[i + 3];
            int x = (xUpper << 8) | xLower;
            x = modShift(x, mod);
            decoded[(i - 8) / 4] = x;
        }
        return reverseInts(decoded);
    }

    private void generateLists() {
        LinkedList<Integer> primes = new LinkedList<Integer>();
        primes.add(17659);
        primes.add(13681);
        primes.add(14851);
        primes.add(10711);
        primes.add(13681);
        primes.add(16069);
        primes.add(3469);
        primes.add(1811);
        primes.add(1597);
        primes.add(1039);
        primes.add(967);
        primes.add(5501);
        primes.add(5591);
        primes.add(3253);
        primes.add(7177);
        primes.add(7187);
        primes.add(769);
        primes.add(8599);
        primes.add(2707);
        primes.add(2713);
        primes.add(1523);
        primes.add(4231);
        primes.add(5273);
        primes.add(4973);
        primes.add(5407);
        primes.add(7109);
        _primes = new int[primes.size()];
        Iterator<Integer> iter = primes.descendingIterator();
        int index = 0;
        for (int i = iter.next(); iter.hasNext(); i = iter.next()) {
            _primes[index] = i;
            index += 1;
        }
    }

    private int[] _primes;
    private static int largePrime = 37633;

}
