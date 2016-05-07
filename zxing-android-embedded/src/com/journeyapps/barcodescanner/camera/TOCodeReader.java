package com.journeyapps.barcodescanner.camera;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.ResultPointCallback;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.GridSampler;
import com.google.zxing.common.PerspectiveTransform;
import com.google.zxing.qrcode.detector.Detector;

/**
 * This implementation can detect and decode Text Oriented Codes in an image.
 *
 * @author movedon2otherthings@protonmail.com
 */
public class TOCodeReader implements Reader {

    public static String fp = "";

    boolean debug = false;

    // Text Oriented calls this callback to handle the actual code payload data
    // pixels (content)

    public interface Callback {
        String handlematrix(ResultPoint lu, ResultPoint ld, ResultPoint ru,
                            ResultPoint rd, BitMatrix m, int width);
    }

    // character dumper
    public static void dumpchar(long letter) {
        System.out.println(((letter >> 3) & 1) + "" + ((letter >> 2) & 1) + ""
                + ((letter >> 1) & 1) + "" + ((letter >> 0) & 1) + "" + 0 + "");
        System.out.println(((letter >> 7) & 1) + "" + ((letter >> 6) & 1) + ""
                + ((letter >> 5) & 1) + "" + ((letter >> 4) & 1) + "" + 0 + "");
        System.out.println(((letter >> 11) & 1) + "" + ((letter >> 10) & 1)
                + "" + ((letter >> 9) & 1) + "" + ((letter >> 8) & 1) + "" + 0
                + "");
        System.out.println(((letter >> 15) & 1) + "" + ((letter >> 14) & 1)
                + "" + ((letter >> 13) & 1) + "" + ((letter >> 12) & 1) + ""
                + 0 + "");
        System.out.println(((letter >> 19) & 1) + "" + ((letter >> 18) & 1)
                + "" + ((letter >> 17) & 1) + "" + ((letter >> 16) & 1) + ""
                + 0 + "");
        System.out.println(((letter >> 23) & 1) + "" + ((letter >> 22) & 1)
                + "" + ((letter >> 21) & 1) + "" + ((letter >> 20) & 1) + ""
                + 0 + "");
        System.out.println(((letter >> 27) & 1) + "" + ((letter >> 26) & 1)
                + "" + ((letter >> 25) & 1) + "" + ((letter >> 24) & 1) + ""
                + 0 + "");
        System.out.println(((letter >> 31) & 1) + "" + ((letter >> 30) & 1)
                + "" + ((letter >> 29) & 1) + "" + ((letter >> 28) & 1) + ""
                + 0 + "");
        System.out.println();
    }

    // default implementation of the callback, capable to decode ascii

    // we prepare a callback to read actual data content in the found barcode
    class AsciiCallback implements TOCodeReader.Callback {

        public long bestscore = 9223372036854775807L;

        public String handlematrix(ResultPoint lu, ResultPoint ld,
                                   ResultPoint ru, ResultPoint rd, BitMatrix m, int width) {

            if ((width % 5) == 4) {

                // System.out.println("pony");

                // doactual(0, lu,ld,ru,rd, 0L, m, filePath, width);

                long[] downside = (doscan(lu, ld, ru, rd, m, width));
                long[] upside = (doscan(rd, ru, ld, lu, m, width));

                int terminatelen = (width + 1) / 5;

                downside = scan_oneside(downside, 66977792, 134184446,
                        134213630, terminatelen);
                upside = scan_oneside(upside, 66977792, 134184446, 134213630,
                        terminatelen);

                // compare score from upside and downside scan
                // results are replaced with offset to ascii set
                long[] result;
                if (downside[42] < upside[42]) {
                    result = downside;
                    dump(lu, ld, ru, rd, m, width);
                } else {
                    result = upside;
                    dump(rd, ru, ld, lu, m, width);
                }

                // System.out.println("INNOVATIONNE??");

                // check the score within this detection
                if (result[42] >= bestscore) {
                    return "";
                } else {
                    bestscore = result[42];

                    System.out.println("bestscore=" + bestscore);

                }

                String out = "";

                // encode result to ascii alphabet
                for (int i = 0; i < terminatelen; i++) {

                    out += asciicd.charAt((int) result[i]);
                }

                // System.out.println("cute resul="+bestscore+" dataout="+out);

                return out;

            }

            if (width == 186) {
                System.out.println("Result ignored");
            }

            // System.out.println("WE GOT THIS FAR");

            return "unknownBarcodeWidth=" + width;
        }
    }

    // ascii coding glyphs (font), to be made final
    private final static long asciicodes[] = new long[] {
            0, 33694242, 2730, 133660528, 82143858, 62139840, 130009664, 1092,
            38028320, 69345856, 3951, 456192, 208011264, 61440, 106954752, 147087921, 111016854,
            35793506, 260317590, 236003614, 36677698, 110225551, 110730886, 71705503, 110717334,
            102209942, 6686304, 208012896, 2393120, 986880, 4330048, 67379606, 110091158, 161085846,
            245091230, 110659734, 244947358, 260634767, 143190159, 110737559, 161085849, 239354958,
            110170385, 161139353, 260606088, 161061369, 163577305, 110729622, 143563166, 95000982,
            162326942, 244410519, 71582798, 110729625, 78293401, 167352729, 160787097, 35809689,
            264528447, 121914439, 20079816, 237117998, 164, 251658240, 620, 127344128, 244948616,
            126387968, 127506193, 126850560, 71585347, 1769583872, 161062792, 119678466, 1762726657,
            162310536, 35791394, 268435200, 161077760, 110728704, 2297011712L, 293181184, 143182592,
            236357376, 54808132, 127506688, 78223616, 167768064, 156674304, 1630902528, 260448000,
            38044738, 71582788, 69341732, 2640,
//		260815759, 205800524,	parts of finder
            //alphabet 2, the last and the first on another segment
            261724575,268435455,
    };
    private final String asciicd = " !\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~ ";


    // scan the code assuming it is not upside down and report the score
    // put results to chars, the first char is used to store simply the score
    // the 96 set bits in the ascii0,1,2 control what ascii chars would be
    // recognized
    public static long[] scan_oneside(long[] chars, int ascii0, int ascii1,
                                      int ascii2, int len) {
        int score = 0;// the less the better

        // System.out.println("LEN "+asciicodes.length + " " + ascii.length() +
        // " " + asciicd.length());

		/*
		 * System.out.println("SKIPP "+ascii0 + " " + ascii1 + " " + ascii2);
		 *
		 * for (int i = 0; i < 96; i++) {
		 *
		 * int wj = i % 96; // wrapped j
		 *
		 * int whatasciibitmap = 0; switch (wj / 32) { case 0: whatasciibitmap =
		 * ascii0;break; case 1: whatasciibitmap = ascii1;break; case 2:
		 * whatasciibitmap = ascii2;break; } if (0 == ((whatasciibitmap >> (wj %
		 * 32))&1)) { // System.out.println(whatasciibitmap+" SKIP"+wj);
		 * continue; }
		 *
		 * dumpchar(asciicodes[i]); System.out.println(asciicd.charAt(i)+ " " +
		 * i + "  " );
		 *
		 * }
		 */
        long pad;
        boolean padded = false;

        // if string should be padded
        if (true) {
            // here we find the endcharacter
            long p = chars[len-1];
            long q = 268435455;
            long r = 0;
            long qs =  popcount(p^q);
            long rs =  popcount(p^r);

            if ((rs<7) || (qs < 7)) {
                padded = true;
            }

            if (rs < qs) {
                pad = 0;
            } else {
                pad = 268435455;
            }
            System.out.println("padded: "+padded+" "+pad);

        }

//		System.out.println("asciilen: "+asciicodes.length+" ");


        for (int i = len-1; i >= 0; i--) {
            long p = chars[i];

            // dumpchar(p);

            int bj = 0 + Integer.numberOfTrailingZeros(ascii0);
            ;// index of best character
            if (ascii0 == 0) {
                bj = 32 + Integer.numberOfTrailingZeros(ascii1);
                if (ascii1 == 0) {
                    bj = 64 + Integer.numberOfTrailingZeros(ascii2);

                }

            }
            // System.out.println(" bj"+bj);

            for (int j = 0; j < asciicodes.length; j++) {

                int wj = j % 96; // wrapped j

                int whatasciibitmap = 0;
                switch (wj / 32) {
                    case 0:
                        whatasciibitmap = ascii0;
                        break;
                    case 1:
                        whatasciibitmap = ascii1;
                        break;
                    case 2:
                        whatasciibitmap = ascii2;
                        break;
                }
                if (0 == ((whatasciibitmap >> (wj % 32)) & 1)) {
                    // System.out.println(whatasciibitmap+" SKIP"+wj);

                    // but what if the code is padded.

                    if ((padded) && (wj == 0) ) {
                        // do nothing
                    } else {
                        continue;
                    }
                }

                long q = asciicodes[j];
                long r = asciicodes[bj];

                if (popcount(p ^ q) < popcount(p ^ r)) {
                    bj = j;
                }

//				if (wj != 0) {
//					padded = false;
//				}
            }

			/*
			 * if (bj == 17) { dumpchar(p); System.out.println(bj + " "+
			 * ascii.charAt(bj % 96) + " p = " + p);
			 *
			 *
			 * }
			 */

            // here, I obtain the difference between best character and actual
            // character p
            score += popcount(p ^ asciicodes[bj]);
            // the smallest score is best

            // System.out.print(" "+ p);



            // if (false)
            // System.out.print(" "+ ascii.charAt(bj));
            chars[i] = bj % 96;
            // out += ascii.charAt(bj);

            // stop padding upon first non-padding character

            if (chars[i] != 0) {
                padded = false;
            }
        }
        // System.out.println();



        chars[42] = score;

        return chars;

    }

    public float clockwise(ResultPoint a, ResultPoint b, ResultPoint c) {

        float i = (b.getX() - a.getX()) * (b.getY() + a.getY());
        float j = (c.getX() - b.getX()) * (c.getY() + b.getY());
        float k = (a.getX() - c.getX()) * (a.getY() + c.getY());

        return i + j + k;

    }

    public static int bean = 1;

    public static void dump(ResultPoint lu, ResultPoint ld, ResultPoint ru,
                            ResultPoint rd, BitMatrix m, int i) {

        if (fp.length() == 0) {
            return;
        }

        // Now i would like to read the actual barcode from matrix m
        PerspectiveTransform t = PerspectiveTransform
                .quadrilateralToQuadrilateral(3.5f, 3.5f, 12.5f + i, 3.5f,
                        12.5f + i, 9.5f, 3.5f, 9.5f,

                        lu.getX(), lu.getY(), ru.getX(), ru.getY(), rd.getX(),
                        rd.getY(), ld.getX(), ld.getY());

        BitMatrix bits = null;

        try {

            bits = sampleGrid(m, t, 16 + i, 13);
            // DEBUG save to file
            bean++;
        } catch (NotFoundException  e) {
            // System.out.println("error");

            // e.printStackTrace();
        }
    }

    public static long[] doscan(ResultPoint lu, ResultPoint ld, ResultPoint ru,
                                ResultPoint rd, BitMatrix m, int i) {

        long[] ret = new long[43];

        ret[42] = 0xffffffff; // the last integer will be used to store a score

        // Now i would like to read the actual barcode from matrix m
        PerspectiveTransform t = PerspectiveTransform
                .quadrilateralToQuadrilateral(3.5f, 3.5f, 12.5f + i, 3.5f,
                        12.5f + i, 9.5f, 3.5f, 9.5f,

                        lu.getX(), lu.getY(), ru.getX(), ru.getY(), rd.getX(),
                        rd.getY(), ld.getX(), ld.getY());

        BitMatrix bits = null;

        try {

            bits = sampleGrid(m, t, 16 + i, 13);
            // DEBUG save to file
            // MatrixToImageWriter.writeToFile(bits, fp.substring(fp
            // .lastIndexOf('.') + 1), new File(fp+"."+bean+".png"));
            // bean++;

        } catch (NotFoundException e) {
            // System.out.println("error");

            // e.printStackTrace();
            return null;
        }

        for (int z = 0; z <= i; z += 5) {
            long n = 0;
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 4; x++) {
                    boolean b = bits.get(x + 8 + z, 10 - y);
                    n <<= 1;
                    if (b)
                        n |= 1;
                }
            }
            ret[z / 5] = n;

        }

        return ret;

    }

    // just popcount of nonzero bits
    private static int popcount(long n) {
        return Integer.bitCount((int) (n & 0xffffffff))
                + Integer.bitCount((int) (n >> 32));
    }

    // cool fast inverse square root
    static private float MathInvSqrt(float x) {
        float xhalf = 0.5f * x;
        int i = Float.floatToIntBits(x);
        i = 0x5f375a86 - (i >> 1); // gives initial guess y0
        x = Float.intBitsToFloat(i);
        x = x * (1.5f - xhalf * x * x); // Newton step, repeating increases
        // accuracy
        return x;
    }

    // just sampler
    private static BitMatrix sampleGrid(BitMatrix image,
                                        PerspectiveTransform transform, int x, int y)
            throws NotFoundException {

        GridSampler sampler = GridSampler.getInstance();
        return sampler.sampleGrid(image, x, y, transform);
    }

    // Pitch detector - cached what letter spaces are at x point (width 600)
    // detects letter empty spaces
    final static int[] arr = { 0, 0, 0, 0, 0, 2088960, 2097024, 1048568, 16383,
            1023, 1572991, 2064399, 2093059, 261632, 32640, 8160, 2040, 508,
            127, 31, 15, 7, 1, 0, 1572864, 1835008, 2031616, 491520, 253952,
            61440, 30720, 15360, 7680, 1792, 896, 448, 480, 240, 1048688,
            1835064, 917532, 458780, 229390, 98310, 49159, 57347, 28675, 14337,
            6145, 3072, 3584, 1050112, 1049344, 1573760, 786816, 393664,
            393408, 196704, 98400, 98352, 49200, 24600, 24600, 12312, 4108,
            1054732, 1050630, 1575942, 787458, 787971, 393731, 131841, 196865,
            65921, 98688, 49344, 49344, 16448, 24672, 1056864, 1060896,
            1577008, 530448, 788496, 265240, 394248, 132104, 198156, 66052,
            98820, 33540, 49414, 16642, 1065346, 1073282, 532611, 532673,
            790593, 266305, 268385, 133152, 133152, 68640, 66576, 66576, 34320,
            1081872, 1098248, 1065480, 540936, 549128, 532740, 270724, 274564,
            135300, 135298, 135234, 67650, 67650, 67650, 1082401, 1082401,
            1082401, 541729, 541201, 541200, 279056, 270352, 270608, 139528,
            135432, 135432, 69768, 69768, 1116292, 1050756, 1083460, 557124,
            558148, 541760, 279554, 279586, 279074, 8738, 139808, 139777,
            131089, 69905, 1118481, 1118481, 0, 559232, 559240, 559240, 2184,
            278536, 279552, 279616, 1092, 131140, 139332, 139780, 1057312,
            1114656, 69666, 593954, 528642, 557314, 33042, 296976, 264208,
            280721, 16513, 147585, 132233, 1180681, 1057800, 1056840, 73800,
            590400, 590400, 528900, 37380, 36900, 294948, 262436, 264480,
            18688, 149506, 1198082, 1179666, 1048722, 9362, 599184, 599168,
            598016, 1, 585, 299593, 299593, 299593, 0, 0, 1198080, 1198372,
            1198372, 2340, 36, 589828, 599040, 74880, 9360, 1168, 295058,
            294930, 36882, 37378, 1053250, 1180224, 148040, 147528, 18440,
            526345, 592137, 65793, 74017, 8225, 271392, 263204, 295940,
            1082500, 1085572, 1052804, 135312, 135696, 16912, 541200, 541186,
            591938, 67650, 67650, 272450, 270600, 270600, 1057032, 1082376,
            1082400, 164897, 136225, 135201, 528513, 544901, 540804, 16900,
            66052, 68116, 329744, 264208, 1321040, 1056832, 1089600, 33090,
            164098, 132354, 656394, 529418, 529416, 20520, 16424, 82080,
            327840, 328320, 1311360, 1051137, 10753, 10245, 40965, 172053,
            688212, 655440, 524624, 1344, 5376, 21504, 86016, 1392640, 1376258,
            1310730, 42, 682, 2730, 43680, 699008, 698368, 655360, 0, 0, 0, 85,
            1398101, 1398101, 1398101, 85, 0, 0, 0, 655360, 698368, 699008,
            43680, 2730, 682, 42, 1310730, 1376258, 1392640, 86016, 21504,
            5376, 1344, 524624, 655440, 688212, 172053, 40965, 10245, 10753,
            1051137, 1311360, 328320, 327840, 82080, 16424, 20520, 529416,
            529418, 656394, 132354, 164098, 33090, 1089600, 1056832, 1321040,
            264208, 329744, 68116, 66052, 16900, 540804, 544901, 528513,
            135201, 136225, 164897, 1082400, 1082376, 1057032, 270600, 270600,
            272450, 67650, 67650, 591938, 541186, 541200, 16912, 135696,
            135312, 1052804, 1085572, 1082500, 295940, 263204, 271392, 8225,
            74017, 65793, 592137, 526345, 18440, 147528, 148040, 1180224,
            1053250, 37378, 36882, 294930, 295058, 1168, 9360, 74880, 599040,
            589828, 36, 2340, 1198372, 1198372, 1198080, 0, 0, 299593, 299593,
            299593, 585, 1, 598016, 599168, 599184, 9362, 1048722, 1179666,
            1198082, 149506, 18688, 264480, 262436, 294948, 36900, 37380,
            528900, 590400, 590400, 73800, 1056840, 1057800, 1180681, 132233,
            147585, 16513, 280721, 264208, 296976, 33042, 557314, 528642,
            593954, 69666, 1114656, 1057312, 139780, 139332, 131140, 1092,
            279616, 279552, 278536, 2184, 559240, 559240, 559232, 1118481,
            1118481, 1118481, 69905, 131089, 139777, 139808, 8738, 279074,
            279586, 279554, 541760, 558148, 557124, 1083460, 1050756, 1116292,
            69768, 69768, 135432, 135432, 139528, 270608, 270352, 279056,
            541200, 541201, 541729, 1082401, 1082401, 1082401, 67650, 67650,
            67650, 135234, 135298, 135300, 274564, 270724, 532740, 549128,
            540936, 1065480, 1098248, 1081872, 34320, 66576, 66576, 68640,
            133152, 133152, 268385, 266305, 790593, 532673, 532611, 1073282,
            1065346, 16642, 49414, 33540, 98820, 66052, 198156, 132104, 394248,
            265240, 788496, 530448, 1577008, 1060896, 1056864, 24672, 16448,
            49344, 32896, 98688, 65921, 196865, 131841, 393731, 787971, 787458,
            1575942, 1050630, 1054732, 4108, 12312, 24600, 24600, 49200, 98352,
            98400, 196704, 393408, 393664, 786816, 1573760, 1049344, 1050112,
            3584, 3072, 6145, 14337, 28675, 57347, 49159, 98310, 229390,
            458780, 917532, 1835064, 1048688, 240, 480, 448, 896, 1792, 7680,
            15360, 30720, 61440, 253952, 491520, 2031616, 1835008, 1572864, 0,
            1, 7, 15, 31, 127, 508, 2040, 8160, 32640, 261632, 2093059,
            2064399, 1572991, 1023, 16383, 1048568, 2097024, 2088960, 0, 0, 0,
            0,

            0 };

    // gets the pitch of the barcode, in other words the number of characters
    // the barcode consists of
    private static int getpitch(ResultPoint lu, ResultPoint ld, ResultPoint ru,
                                ResultPoint rd, BitMatrix m, String fp) {

        final int width = 600;
        int height = 1;

        // Now i would like to read the actual barcode from matrix m
        PerspectiveTransform t = PerspectiveTransform
                .quadrilateralToQuadrilateral(0.5f, 0.5f, width + 0.5f, 0.5f,
                        width + 0.5f, height + 0.5f, 0.5f, height + 0.5f,

                        lu.getX(), lu.getY(), ru.getX(), ru.getY(), rd.getX(),
                        rd.getY(), ld.getX(), ld.getY());

        BitMatrix bits = null;

        try {

            bits = sampleGrid(m, t, width, height);

            // DEBUG save to file
            // MatrixToImageWriter.writeToFile(bits, fp.substring(fp
            // .lastIndexOf('.') + 1), new File(fp+".png"));

        } catch (NotFoundException e) {
            e.printStackTrace();
            // } catch (IOException e) {
            // e.printStackTrace();
        }

        // System.out.println(" pitch lol "+(pitch+22));

        int[] pitches = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0 };

        // for (int x = 0; x < width; x++) {
        // for (int i = 0; i < 21; i++) {
        // if ((arr[x] & (1 << i)) > 0) {
        // pitches[i]++;
        // }
        // }
        // }

        for (int i = 0; i < 21; i++) {
            for (int x = 0; x < width; x++) {
                int n = (arr[x] >> i) & (1);
                boolean b = bits.get(x, 0);

                if ((n == 1) && (b == false)) {
                    pitches[i]++;

                }
            }
        }

        int bestpitch = 0;
        for (int i = 1; i < 21; i++) {
            if (pitches[i] >= pitches[bestpitch]) {
                bestpitch = i;
            }
        }
        return bestpitch;

        // System.out.println(" SUM= "+pitches[pitch]);

        // for (int i = 0; i < 21; i++) {
        // System.out.println(" pitch "+(i+22) + " = "+pitches[i]);
        // }

        // for (int x = 0; x < width; x++) {
        // if (bits.get(x, 0) == false) {
        // arr[x] |= 1 << pitch;
        // }
        // }

        // if (pitch == 20) {
        // for (int x = 0; x < width; x++) {
        // System.out.println(arr[x]+",");
        //
        // }
        // }
        //
    }

    // computes the area of empty spaces above and below left right finders
    // used in finder radial (diameter) estimation
    private static long cutlen(ResultPoint lu, ResultPoint ld, ResultPoint ru,
                               ResultPoint rd, BitMatrix m, String fp) {

        int width = 217;
        int height = 52;

        // Now i would like to read the actual barcode from matrix m
        PerspectiveTransform t = PerspectiveTransform
                .quadrilateralToQuadrilateral(3.5f, 3.5f, 213.5f, 3.5f, 213.5f,
                        48.5f, 3.5f, 48.5f,

                        lu.getX(), lu.getY(), ru.getX(), ru.getY(), rd.getX(),
                        rd.getY(), ld.getX(), ld.getY());

        BitMatrix bits = null;

        try {

            bits = sampleGrid(m, t, width, height);

            // DEBUG save to file
            // MatrixToImageWriter.writeToFile(bits, fp.substring(fp
            // .lastIndexOf('.') + 1), new File(fp+".png"));

        } catch (NotFoundException e) {
            e.printStackTrace();
            // } catch (IOException e) {
            e.printStackTrace();
        }

        int l = 0;
        int r = 0;

        for (int x = 1; x < 6; x++) {

            int lup = 0;
            int ldw = 0;
            int rup = 0;
            int rdw = 0;

            for (int y = 0; (y < 26)
                    && ((lup == 0) || (ldw == 0) || (rup == 0) || (rdw == 0)); y++) {
                if ((lup == 0) && bits.get(x, y)) {
                    lup = y + 1;
                }
                if ((ldw == 0) && bits.get(x, height - y - 1)) {
                    ldw = y + 1;
                }
                if ((rup == 0) && bits.get(width - x - 1, y)) {
                    rup = y + 1;
                }
                if ((rdw == 0) && bits.get(width - x - 1, height - y - 1)) {
                    rdw = y + 1;
                }
            }

            lup += ldw;
            rup += rdw;

            // DEBUG
            // System.out.println(" L: " + (lup-2) + " R: " + (rup-2));

            if (lup > 2)
                l += lup - 2;
            if (rup > 2)
                r += rup - 2;

        }

        return 65536 * l + r;

    }

    @Override
    public Result decode(BinaryBitmap image) throws NotFoundException,
            ChecksumException, FormatException {
        return decode(image, null);
    }

    // the complete algorithm
    @Override
    public final Result decode(BinaryBitmap image, Map<DecodeHintType, ?> hints)
            throws NotFoundException, FormatException {

        // Use callback to find 2 finders

        class Cb implements ResultPointCallback {
            public ResultPoint[] finders = new ResultPoint[15];
            public int i = 0;

            public void foundPossibleResultPoint(ResultPoint point) {
                if (i >= 15)
                    return;
                finders[i] = point;
                i++;
            }
        }

        Cb finders = new Cb();

        Map<DecodeHintType, ResultPointCallback> hm = new HashMap<DecodeHintType, ResultPointCallback>();
        hm.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, finders);

        BitMatrix m = image.getBlackMatrix();

        try {

            // Entry point to QR code finders search
            new Detector(m).detect(hm);

        } catch (FormatException | NotFoundException e) {
            // intentionally ignore exceptions
        }

        // / normal continue
        // / must be 2 finders
        if (finders.i < 2) {
            // dump finders
            // if (debug)
            // System.out.println("Finder:" + finders.finders[0]);

            // throw exception
            new Detector(null).detect(hm);
        }

        // debug = true;

        // Prepare callback and initial ret
        Callback c = (Callback) hints.get(DecodeHintType.OTHER);

        String ret = "";

        // but we must also prepare the internal default ascii callback
        AsciiCallback ac = new AsciiCallback();

        int res_x = 0, res_y = 0;

        // Quadratic algorithm. But typically there isn't many finders
        for (int y = 0; y < finders.i; y++) {

            // dump finders
            // if (debug)
            // System.out.println("Finder:" + finders.finders[y]);

            for (int x = 0; x < y; x++) {

                // if ((y != 2 ) || (x != 1)) {
                // continue;
                // }
                // System.out.println("Between: " + x + " " + y + " of " +
                // finders.finders[y] + " and " + finders.finders[x]);

				/*
				 *
				 * // Swap finders (testcases needed this) ResultPoint l, r;
				 *
				 * if (finders.finders[0].getX() > finders.finders[1].getX()) {
				 * l = finders.finders[1]; r = finders.finders[0]; } else { l =
				 * finders.finders[0]; r = finders.finders[1];
				 *
				 * }
				 */

                // Finders axis gradient

                float axisgradx = finders.finders[y].getX()
                        - finders.finders[x].getX();
                float axisgrady = finders.finders[y].getY()
                        - finders.finders[x].getY();

                // assumed axis length, pythagorean
                float axis = (float) Math.sqrt(axisgradx * axisgradx
                        + axisgrady * axisgrady);

                // a multiplier to make sure we get an overestimate of the
                // radiuses
                // if more narrow codes are needed, tune by increasing this
                float max_magic = 1.165f;

                // 0=default= automatic pitch detection
                // how many actual pixel characters are there in code
                int datawidthpixels = 0;

                // obtain algorithm parameters
                // use this int array to control this algorithm
                if (hints.containsKey(DecodeHintType.ALLOWED_LENGTHS)) {
                    int[] lens = (int[]) hints
                            .get(DecodeHintType.ALLOWED_LENGTHS);
                    // get the first = the pitch override
                    if (lens.length > 0) {
                        datawidthpixels = lens[0];
                    }
                }

                // auto code number of characters detection
                if (datawidthpixels == 0) {
                    if (debug)
                        System.out.println("Pitchdetection:" + 1);

                    int pitch = getpitch(finders.finders[y],
                            finders.finders[y], finders.finders[x],
                            finders.finders[x], m, null);

                    if (debug)
                        System.out.println("Pitchdetection:" + pitch);

                    datawidthpixels = 109 + 5 * pitch;

                    if (debug)
                        System.out.println("WIDTH::" + datawidthpixels);

                }

                // use formula to get the guess of radial (diameter of finder)

                float radial_guess = axis * max_magic
                        * (6f / (datawidthpixels + 8f));

                // the multiplier used to calculate corners from the finders
                // the calculation will be done from left right radiuses using
                // pythagorean

                float konst = MathInvSqrt(axisgradx * axisgradx + axisgrady
                        * axisgrady);

                if (debug)
                    System.out.println("gradx=" + axisgradx + " grady="
                            + axisgrady + " konst=" + konst + " axis=" + axis
                            + " rad_guess=" + radial_guess);

                // assign the guessed radial
                float radialr = radial_guess;
                float radiall = radial_guess;

                if (debug)
                    System.out.println("RadialL:" + radiall);
                if (debug)
                    System.out.println("RadialR:" + radialr);

                // points from radius, gradient and pytagorean

                ResultPoint lu = new ResultPoint(finders.finders[x].getX()
                        + (konst * axisgrady * radiall * 0.5f),
                        finders.finders[x].getY()
                                - (konst * axisgradx * radiall * 0.5f));
                ResultPoint ld = new ResultPoint(finders.finders[x].getX()
                        - (konst * axisgrady * radiall * 0.5f),
                        finders.finders[x].getY()
                                + (konst * axisgradx * radiall * 0.5f));
                ResultPoint ru = new ResultPoint(finders.finders[y].getX()
                        + (konst * axisgrady * radialr * 0.5f),
                        finders.finders[y].getY()
                                - (konst * axisgradx * radialr * 0.5f));
                ResultPoint rd = new ResultPoint(finders.finders[y].getX()
                        - (konst * axisgrady * radialr * 0.5f),
                        finders.finders[y].getY()
                                + (konst * axisgradx * radialr * 0.5f));
                if (debug)
                    System.out.println("BEFORE" + lu + " " + ld + " " + ru
                            + " " + rd);

                // cutlen returns the area (integral) above and below the
                // finders

                long lr = cutlen(lu, ld, ru, rd, m, null);
                float total = 5 * 52;

                float fl = 1f - (lr / 65536) / total;
                float fr = 1f - (lr % 65536) / total;

                // make the left right finder vertical diameters(radials in
                // pixels) exact

                radiall = fl * radial_guess;
                radialr = fr * radial_guess;

                if (debug)
                    System.out.println("AreaL AreaR: \t" + radiall + " \t"
                            + radialr);

                // and now do points again without guess.
                // because we know radials

                lu = new ResultPoint(finders.finders[x].getX()
                        + (konst * axisgrady * radiall * 0.5f),
                        finders.finders[x].getY()
                                - (konst * axisgradx * radiall * 0.5f));
                ld = new ResultPoint(finders.finders[x].getX()
                        - (konst * axisgrady * radiall * 0.5f),
                        finders.finders[x].getY()
                                + (konst * axisgradx * radiall * 0.5f));
                ru = new ResultPoint(finders.finders[y].getX()
                        + (konst * axisgrady * radialr * 0.5f),
                        finders.finders[y].getY()
                                - (konst * axisgradx * radialr * 0.5f));
                rd = new ResultPoint(finders.finders[y].getX()
                        - (konst * axisgrady * radialr * 0.5f),
                        finders.finders[y].getY()
                                + (konst * axisgradx * radialr * 0.5f));

                if (debug)
                    System.out.println("ALMOST" + lu + " " + ld + " " + ru
                            + " " + rd);

                // call callback
                // to handle actual payload (letters) in the code
                if (c != null) {

                    String recognized = c.handlematrix(lu, ld, ru, rd, m,
                            datawidthpixels);
                    if (recognized.length() > 0) {
                        ret = recognized;
                        res_x = x;
                        res_y = y;
                    }

                } else {

                    // if user didn't provide solution
                    // try our built-in ascii decoder

                    String recognized = ac.handlematrix(lu, ld, ru, rd, m,
                            datawidthpixels);
                    if (recognized.length() > 0) {
                        ret = recognized;
                        res_x = x;
                        res_y = y;

                    }

                }

            }
        }

        bean = 0;

        // and return it
        return new Result(ret, null, new ResultPoint[] {
                finders.finders[res_x], finders.finders[res_y] },
                BarcodeFormat.UPC_E, 56135);

    }

    @Override
    public void reset() {
        // do nothing
    }

}
