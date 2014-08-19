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
        int k = 4;
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
