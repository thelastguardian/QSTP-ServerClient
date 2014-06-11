/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package qstp;

import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author nhnt11
 */
public class SimpleClient {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        Socket sock = new Socket("localhost", 8888);
        Scanner s = new Scanner(System.in);
        OutputStreamWriter osw =
                new OutputStreamWriter(
                sock.getOutputStream());
        String line;
        while (!((line = s.nextLine()).equals(":quit"))) {
            osw.write(line + "\n");
            osw.flush();
        }
        s.close();
    }
}
