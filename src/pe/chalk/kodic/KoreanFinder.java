/**
 * A simple dictionary for Korean, powered by National Institute of the Korean Language
 * Copyright (C) 2015  ChalkPE
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package pe.chalk.kodic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author ChalkPE <amato0617@gmail.com>
 * @since 2015-06-02
 */
public class KoreanFinder {
    private static final String URL = "http://stdweb2.korean.go.kr/search/List_dic.jsp";
    private static final String CHARSET = "UTF-8";

    enum SearchType {
        EQUALS("0"), STARTS_WITH("1"), ENDS_WITH("2"), CONTAINS("3");

        private String id;

        SearchType(String id){
            this.id = id;
        }

        @Override
        public String toString(){
            return this.id;
        }
    }

    enum SpCode {
        MYEONGSA("1");

        private String id;

        SpCode(String id){
            this.id = id;
        }

        @Override
        public String toString(){
            return this.id;
        }
    }

    private static String getParameters(SearchType type, String text, SpCode... spCodes) throws UnsupportedEncodingException {
        String param = URLEncoder.encode("PageRow",    CHARSET) + "=" + URLEncoder.encode("100000000",     CHARSET) + "&" +
                       URLEncoder.encode("Table",      CHARSET) + "=" + URLEncoder.encode("words",         CHARSET) + "|" + URLEncoder.encode("word", CHARSET) + "&" +
                       URLEncoder.encode("Gubun",      CHARSET) + "=" + URLEncoder.encode(type.toString(), CHARSET) + "&" +
                       URLEncoder.encode("SearchPart", CHARSET) + "=" + URLEncoder.encode("Simple",        CHARSET) + "&";

        if(spCodes.length > 0){
            param += URLEncoder.encode("SpCode", CHARSET) + "=" + String.join("&" + URLEncoder.encode("SpCode", CHARSET) + "=", Stream.of(spCodes).map(SpCode::toString).collect(Collectors.toList())) + "&";
        }

        return param + URLEncoder.encode("SearchText", CHARSET) + "=" + URLEncoder.encode(text, CHARSET);
    }

    private static String getHTML(String parameters) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
        connection.setDoOutput(true);
        connection.setRequestMethod("GET");

        try(BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), CHARSET))){
            writer.write(parameters);
        }

        String read;
        StringBuilder builder = new StringBuilder();

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), CHARSET))){
            while((read = reader.readLine()) != null){
                builder.append(read).append('\n');
            }
        }

        return builder.toString();
    }

    public static Collection<String> getAllNounStartsWith(String firstLetter, String... banned) throws IOException {
        Document document = Jsoup.parse(KoreanFinder.getHTML(KoreanFinder.getParameters(SearchType.STARTS_WITH, firstLetter, SpCode.MYEONGSA)));

        return new Elements(document.select("span#print_area p.exp").stream().filter(element -> {
            for(Element elem : element.select("> font[face=\"새굴림\"]")){
                if(Arrays.asList(banned).contains(elem.text())){
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList())).select("a[title] strong font").stream().map(Element::text).filter(str -> str.length() > 1).distinct().map(str -> str.replaceAll("-", "")).collect(Collectors.toList());
    }
}
