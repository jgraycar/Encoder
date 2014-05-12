package enc;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

import java.util.Scanner;
import java.util.regex.Pattern;

/** Reverses either file inputted or String inputted.
 *  @author Joel Graycar
 */

public class Reverse {

    public static void main(String... args) {
        boolean interactive = false;
        if (args.length > 0) {
            for (String arg : args) {
                if (arg.equals("-i")) {
                    interactive = true;
                } else {
                    String rev = reverseText(arg);
                    System.out.println(rev);
                }
            }
        } else { 
            System.out.println("Error: No input string or file given.");
            System.exit(1);
        }
        if (interactive) {
            interMode();
        }
    }

    /** Returns the file named FILENAME as a BufferedReader.
     *  @param filename is the name of the file being accessed.
     *  @return returns the file as a BufferedReader.
     */
    private static BufferedReader getFile(String filename) {
        InputStream resource =
            enc.Reverse.class.getClassLoader().getResourceAsStream(filename);
        BufferedReader str =
            new BufferedReader(new InputStreamReader(resource));
        return str;
    }

    /** returns the reverse of FILENAME, from back to front.
     *  @param filename is either a file, or just text to be reversed.
     *  @return returns the text of FILENAME or just FILENAME in reverse.
     */
    public static String reverseText(String filename) {
        String text = "";
        try {
            BufferedReader str = getFile(filename);
            for (int c = str.read(); c != -1l; c = str.read()) {
                char[] ch = Character.toChars(c);
                text = text + ch[0];
            }
        } catch (IOException excp) {
            text = filename;
        } catch (NullPointerException np) {
            text = filename;
        }
        char[] textArr = text.toCharArray();
        String revText = "";
        for (int i = textArr.length - 1; i >= 0; i -= 1) {
            revText += textArr[i];
        }
        return revText;
    }

    public static int[] reverseInts(int[] intArr) {
        int[] newArr = new int[intArr.length];
        for (int i = 0; i < intArr.length; i += 1) {
            newArr[intArr.length - i - 1] = intArr[i];
        }
        return newArr;
    }

    /** enter interactive mode. */
    private static void interMode() {
        System.out.println("Entering interactive mode");
        boolean running = true;
        Scanner inp = new Scanner(new InputStreamReader(System.in));
        Pattern pat = Pattern.compile("\\p{Blank}+");
        while (running) {
            System.out.print("> ");
            String line = inp.nextLine();
            String[] parts = pat.split(line);
            for (String str : parts) {
                if (str.equals("quit")) {
                    running = false;
                } else {
                    String revText = reverseText(str);
                    System.out.println(revText);
                }
            }
        }       
    }
}
