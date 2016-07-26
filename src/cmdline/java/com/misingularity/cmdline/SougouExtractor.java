/**
 * boilerpipe
 *
 * Copyright (c) 2009 Christian Kohlschütter
 *
 * The author licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.misingularity.cmdline;

import java.io.*;
import java.lang.RuntimeException;
import java.lang.StringBuilder;
import java.nio.charset.MalformedInputException;
import java.util.HashSet;

import com.cybozu.labs.langdetect.*;


import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.DefaultExtractor;
import de.l3s.boilerpipe.extractors.ExtractorBase;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Demonstrates how to use Boilerpipe to get the main content as plain text.
 * Note: In real-world cases, you'd probably want to download the file first using a fault-tolerant crawler.
 * <p/>
 * java -cp build/libs/imedecoder.jar:libs/boilerpipe-1.2.0.jar com.misingularity.cmdline.SougouExtractor
 *
 * @author Xiaoyun Wu
 */
public class SougouExtractor {

    // This class is used to read the underlying byte twice, once per line, so that we can figure
    // out the charset of the current doc, second time we read the content again with right decoding
    // so that we can deal with it.
    public static class PageReader {
        static HashSet<String> goodcodes = new HashSet<String>();
        static {
            goodcodes.add("utf-8");
            goodcodes.add("gbk");
            goodcodes.add("gb2312");
        };

        BufferedInputStream bin;
        int count = 0;

        public PageReader(InputStream in, int bufSize) {
            bin = new BufferedInputStream(in, bufSize);
        }

        public static String getEncoding(String html) {
            Document doc = Jsoup.parse(html);
            Elements eMETAs = doc.select("META");
            for (Element element : eMETAs) {
                String content = element.attr("content");
                if (content == null) continue;
                int index = content.indexOf("charset=");
                if (index != -1) return content.substring(index + 8);
            }
            return null;
        }

        public String readLine() throws IOException {
            StringBuilder sb = new StringBuilder();
            int c = bin.read();
            while (c != -1) {
                sb.append((char)c);
                count += 1;
                if (c == '\n') break;
                c = bin.read();
            }
            return sb.toString();
        }

        public String readPage(int readLimit) throws IOException {
            bin.mark(readLimit);
            count = 0;
            String line = readLine();
            StringBuilder sb = new StringBuilder();
            while (line != null && !line.startsWith("</doc>")) {
                sb.append(line);
                line = readLine();
            }
            if (count == 0) return null;
            String raw = sb.toString();
            String encoding = getEncoding(raw);
            if (encoding == null) return "";
            encoding = encoding.toLowerCase();

            if (!goodcodes.contains(encoding)) return "";
            byte[] str = new byte[count];
            try {
                bin.reset();
            } catch (IOException ioe) {
                return "";
            }
            bin.read(str);
            return new String(str, encoding);
        }
    }

    public static void main(final String[] args) throws Exception {
        //String path = System.getProperty("user.dir") + "/src/main/resources/profiles";
        ExtractorBase extractor = DefaultExtractor.getInstance();
/*
        try {
            DetectorFactory.loadProfile(path);
        } catch (LangDetectException e) {
            e.printStackTrace();
        }

        Detector detector = null;
*/
        FileInputStream in = new FileInputStream(args[0]);
        Writer rtr = new FileWriter(args[1]);
        PageReader pr = new PageReader(in, 16 * 1024 * 1024);
        String page;
        while ((page = pr.readPage(16 * 1024 * 1024)) != null) {
            if (page.length() == 0) continue;
            // System.out.println("\n************************************************************************\n");
            // System.out.println(page);
            BufferedReader br = new BufferedReader(new StringReader(page));
            String line = br.readLine();
            StringBuilder sb = new StringBuilder();
            String docid = null;
            String url = null;
            try {
                if (line.equals("<doc>")) {
                    line = br.readLine();
                } else {
                    throw new RuntimeException();
                }


                if (line.startsWith("<docno>")) {
                    docid = line;
                    line = br.readLine();
                } else {
                    throw new RuntimeException();

                }

                if (line.startsWith("<url>")) {
                    url = line;
                    line = br.readLine();
                } else {
                    throw new RuntimeException();
                }

                while (!line.startsWith("</doc>")) {
                    sb.append(line);
                    sb.append('\n');
                    line = br.readLine();
                }

                String html = sb.toString();
                String text = extractor.getText(html);
                //System.out.println("++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
                rtr.write(text);
                rtr.write("\n");
            } catch (NullPointerException npe) {
                // Keep silence.
            } catch (StackOverflowError error) {
                // keep silence.
            }
        }
        rtr.close();
    }
}




