package e93.assembler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

public class IOUtils {

    public static String asString(String path) throws IOException {
        return readAllIntoString(asStream(path));
    }

    public static InputStream asStream(String path) {
        return IOUtils.class.getResourceAsStream(path);
    }

    public static String readAllIntoString(final InputStream in) throws IOException {
        StringWriter sw = new StringWriter();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));
             PrintWriter pw = new PrintWriter(sw)) {
            String line;
            while((line = reader.readLine()) != null) {
                pw.println(line);
            }
        }
        return sw.toString();
    }
}
