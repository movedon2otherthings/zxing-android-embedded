package example.zxing;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.CompoundBarcodeView;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * This sample performs continuous scanning, displaying the barcode and source image whenever
 * a barcode is scanned.
 */
public class ContinuousCaptureActivity extends Activity {
    private static final String TAG = ContinuousCaptureActivity.class.getSimpleName();
    private CompoundBarcodeView barcodeView;


    public static class Base58check {
        public final static String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
        private static final BigInteger BASE = BigInteger.valueOf(58);
        /**
         */
        public static byte[] doubleDigest(byte[] input) {
            return doubleDigest(input, 0, input.length);
        }

        /**
         * Calculates the SHA-256 hash of the given byte range, and then hashes the resulting hash again.
         */
        public static byte[] doubleDigest(byte[] input, int offset, int length) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(input, offset, length);
                byte[] first = digest.digest();
                return digest.digest(first);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);  // Cannot happen.
            }
        }

        public static BigInteger decodeToBigInteger(String input) throws IllegalArgumentException {
            BigInteger bi = BigInteger.valueOf(0);
            // Work backwards through the string.
            for (int i = input.length() - 1; i >= 0; i--) {
                int alphaIndex = ALPHABET.indexOf(input.charAt(i));
                if (alphaIndex == -1) {
                    throw  new  IllegalArgumentException("Illegal character " + input.charAt(i) + " at " + i);
                }
                bi = bi.add(BigInteger.valueOf(alphaIndex).multiply(BASE.pow(input.length() - 1 - i)));
            }
            return bi;
        }

        public static byte[] decode(String input) throws IllegalArgumentException {
            byte[] bytes = decodeToBigInteger(input).toByteArray();
            // We may have got one more byte than we wanted, if the high bit of the next-to-last byte was not zero. This
            // is because BigIntegers are represented with twos-compliment notation, thus if the high bit of the last
            // byte happens to be 1 another 8 zero bits will be added to ensure the number parses as positive. Detect
            // that case here and chop it off.
            boolean stripSignByte = bytes.length > 1 && bytes[0] == 0 && bytes[1] < 0;
            // Count the leading zeros, if any.
            int leadingZeros = 0;
            for (int i = 0; input.charAt(i) == ALPHABET.charAt(0); i++) {
                leadingZeros++;
            }
            // Now cut/pad correctly. Java 6 has a convenience for this, but Android can't use it.
            byte[] tmp = new byte[bytes.length - (stripSignByte ? 1 : 0) + leadingZeros];
            System.arraycopy(bytes, stripSignByte ? 1 : 0, tmp, leadingZeros, tmp.length - leadingZeros);
            return tmp;
        }

        /**
         * Uses the checksum in the last 4 bytes of the decoded data to verify the rest are correct. The checksum is
         * removed from the returned data.
         *
         */
        public static byte[] decodeChecked(String input) throws IllegalArgumentException {
            byte[] tmp = decode(input);
            if (tmp.length < 4)
                throw new IllegalArgumentException("Input too short");
            byte[] checksum = new byte[4];
            System.arraycopy(tmp, tmp.length - 4, checksum, 0, 4);
            byte[] bytes = new byte[tmp.length - 4];
            System.arraycopy(tmp, 0, bytes, 0, tmp.length - 4);
            tmp = doubleDigest(bytes);
            byte[] hash = new byte[4];
            System.arraycopy(tmp, 0, hash, 0, 4);
            if (!Arrays.equals(hash, checksum))
                throw new IllegalArgumentException("Checksum does not validate");
            return bytes;
        }
    };

    private BarcodeCallback callback = new BarcodeCallback() {
        String correct = "";

        //private int buffered_scan_result[] = new int[1972];
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                String text = result.getText();
                String res = "";



                // fix crashes
  //              while (text.length() < 34) {
  //                  return;
   //             }

                char first = text.charAt(0);

                // skip non-coin
                if ((first != '1') &&(first != 'L') &&(first != 'D')) {
                   return;
                }




                // fix-non-coin-end
            //    if (text.charAt(33) != 'b') {
           //         return;
            //    }

                try {
                    Base58check.decodeChecked(text.trim());

                    correct = text;
                } catch (IllegalArgumentException e) {
                    res = text;

                };

                if (correct.length()>0) {
                    res = "CORRECT: " + correct;


                    // while we're here, we can aswell to activate the uri
                    String url = "";

                    if (first == '1') {
                        url = "bitc"+"oin:" + correct;
                    }
                    if (first == 'L') {
                        url = "litec"+"oin:" + correct;
                    }
                    if (first == 'D') {
                        url = "dogec"+"oin:" + correct;
                    }
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(url));
//                    sendBroadcast( i );
                    try {
                        startActivity(i);
                    } catch (android.content.ActivityNotFoundException e) {
                        // do nothing
                    }

                }

                barcodeView.setStatusText( res);

            }



            //Added preview of scanned barcode
            ImageView imageView = (ImageView) findViewById(R.id.barcodePreview);
            imageView.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.continuous_scan);

        barcodeView = (CompoundBarcodeView) findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(callback);
    }

    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeView.pause();
    }

    public void pause(View view) {
        barcodeView.pause();
    }

    public void resume(View view) {
        barcodeView.resume();
    }

    public void triggerScan(View view) {
        barcodeView.decodeSingle(callback);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}
