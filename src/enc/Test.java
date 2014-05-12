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

public class Test {

    public Test() {
        generateLists();
    }

    public static void main(String... args) {
        Test enc = new Test();
        if (args.length == 0) {
            System.out.println("Error: Encoder requires at least 1 argument.");
            System.exit(1);
        }
        enc.interactive = false;
        //enc.parseArgs(args);
    }

    /*
    protected int parseArgs(String[] args) {
        boolean name = false;
        String nameFile = "";
        boolean encrypt = true;
        boolean decrypt = false;
        int reps = 1;
        boolean repeat = false;
        boolean prnt = false;
        int ret = 0;
        boolean inter = false;
        for (String arg : args) {
            switch (arg) {
            case "-q":
                ret = 1;
                break;
            case "quit":
                ret = 1;
                break;
            case "-d":
                encrypt = false;
                decrypt = true;
                break;
            case "-e":
                encrypt = true;
                decrypt = false;
                break;
            case "-l":
                String[] files = listCurrFiles();
                for (String fl : files) {
                    System.out.println(fl);
                }
                break;
            case "-n":
                name = true;
                break;
            case "-r":
                repeat = true;
                break;
            case "-p":
                prnt = true;
                break;
            case "-i":
                inter = true;
                break;
            default:
                if (name) {
                    nameFile = arg;
                    name = false;
                } else if (repeat) {
                    try {
                        reps = Integer.parseInt(arg);
                    } catch (NumberFormatException n) {
                        System.err.println("Error: -r flag requires subsequent int.");
                        System.exit(1);
                    }
                    repeat = false;
                } else {
                    String filename = "";
                    String filetext = "";
                    try {
                        BufferedReader text = getAFile(arg);
                        try {
                            for (int c = text.read();
                                 c != -1; c = text.read()) {
                                char[] ch = Character.toChars(c);
                                filetext += ch[0];
                            }
                        } catch (IOException i) {
                            System.out.printf("Something went wrong while" +
                                              " reading file %s.\n", arg);
                            System.exit(1);
                        }
                        text.close();
                        int k = 0;
                        while (k < reps) {
                            if (encrypt) {
                                filetext = encode(filetext);
                            } else if (decrypt) {
                                try {
                                    filetext = decode(filetext);
                                } catch (NumberFormatException err) {
                                    return 3;
                                }
                            }
                            k += 1;
                        }
                        if (!nameFile.equals("")) {
                            filename = nameFile;
                        } else {
                            String[] parts = arg.split("\\.");
                            if (encrypt) {
                                if (parts.length > 1) {
                                    StringBuilder strBld = new StringBuilder();
                                    for (int i = 0; i < (parts.length - 1); i += 1) {
                                        strBld.append(parts[i]);
                                        if (i != (parts.length - 2)) {
                                            strBld.append(".");
                                        }
                                    }
                                    arg = strBld.toString();
                                }
                                filename = arg + ".enc";
                            } else {
                                if (parts[parts.length - 1].equals("enc")) {
                                    StringBuilder strBld = new StringBuilder();
                                    for (int i = 0; i < (parts.length - 1); i += 1) {
                                        strBld.append(parts[i]);
                                        if (i != (parts.length - 2)) {
                                            strBld.append(".");
                                        }
                                    }
                                    arg = strBld.toString();
                                }
                                filename = arg + ".dec";
                            }
                        }
                        FileWriter file = new FileWriter(filename);
                        file.write(filetext);
                        file.close();
                    } catch (NullPointerException nullp) {
                        System.out.printf("NullPointerException: File %s not found.\n", arg);
                        return 1;
                    } catch (IOException io) {
                        System.out.printf("IOException: File %s not found.\n", arg);
                        return 2;
                    }
                    if (prnt) {
                        System.out.print(filetext);
                    }
                    break;
                }
            }
        }
        if (inter) {
            interactiveMode();
        }
        return ret;
    }
    */

    protected int[] encode(int[] text) {
        Random rando = new Random();
        text = reverseInts(text);
        int[] modded = new int[text.length * 2];
        for (int k = 0; k < modded.length; k += 2) {
            int rand = rando.nextInt(_primes.length - 1);
            int mod = _primes[rand];
            int i = modShiftChar(text[k / 2], mod);
            modded[k] = (mod * (largePrime - 1)) % largePrime;
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

    private static int modShiftChar(int c, int mod) {
        int h = (c * (mod - 1)) % mod;
        return h;
    }

    protected static BufferedReader getAFile(String filename) {
        InputStream resource =
            enc.Encoder.class.getClassLoader().getResourceAsStream(filename);
        BufferedReader str =
            new BufferedReader(new InputStreamReader(resource));
        return null;
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
        LinkedList<String> puncts = new LinkedList<String>();
        puncts.add("\t");
        puncts.add("\n");
        puncts.add(" ");
        puncts.add(" ");
        puncts.add(" ");
        puncts.add(" ");
        puncts.add(" ");
        puncts.add(" ");
        puncts.add("\r");
        _puncts = new String[puncts.size()];
        Iterator<String> puncIter = puncts.descendingIterator();
        index = 0;
        for (String str = puncIter.next(); puncIter.hasNext(); str = puncIter.next()) {
            _puncts[index] = str;
            index += 1;
        }
    }

    protected int[] decode(int[] encoded) {
        int[] decoded = new int[encoded.length / 4];
        for (int i = 0; i < encoded.length; i += 4) {
            int modUpper = encoded[i];
            int modLower = encoded[i + 1];
            int mod = (modUpper << 8) | modLower;
            mod = (mod * (largePrime - 1)) % largePrime;
            int xUpper = encoded[i + 2];
            int xLower = encoded[i + 3];
            int x = (xUpper << 8) | xLower;
            x = (x * (mod - 1)) % mod;
            decoded[i / 4] = x;
        }
        return reverseInts(decoded);
    }

    /*
    private void interactiveMode() {
        if (interactive) {
            System.out.println("Already running interactive mode.");
        } else {
            interactive = true;
            System.out.println("Entering interactive mode");
            boolean cont = true;
            Scanner inp = new Scanner(new InputStreamReader(System.in));
            Pattern pat = Pattern.compile("\\p{Blank}+");
            int ret;
            while (cont) {
                System.out.print("> ");
                String line = inp.nextLine();
                String[] parts = pat.split(line);
                ret = parseArgs(parts);
                if (ret == 1) {
                    cont = false;
                }
            }
            System.out.println("Goodbye.");
        }
    }
    */

    protected static String[] listCurrFiles() {
        File file = new File(System.getProperty("user.dir"));
        String[] files = file.list();
        if (files.length == 0) {
            files = new String[1];
            files[0] = "No files found in current directory.";
        }
        return files;
    }

    private String[] _puncts;
    private int[] _primes;
    private static int largePrime = 37633;
    private boolean interactive;

}
