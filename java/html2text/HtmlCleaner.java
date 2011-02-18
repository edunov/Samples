package html2text;/*
 * Copyright (c) 2011, Sergey Edunov. All Rights Reserved.
 *
 * This file is part of JQuant library.
 *
 * JQuant library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * JQuant is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with JQuant. If not, see <http://www.gnu.org/licenses/>.
 */


/**
 * @author "Sergey Edunov"
 * @version Nov 10, 2010
 */
public class HtmlCleaner {
    
    private static final String COMMENT_BEGIN = "<!--";
    private static final String COMMENT_END = "-->";
    private static final String STYLE_BEGIN = "<style";
    private static final String STYLE_END = "</style>";
    private static final String SCRIPT_BEGIN = "<script";
    private static final String SCRIPT_END = "</script>";

    public static String stripScripts(String stext) {
        return strip(stext, SCRIPT_BEGIN, SCRIPT_END);
    }

    public static String stripStyles(String stext) {
        return strip(stext, STYLE_BEGIN, STYLE_END);
    }

    public static String stripComments(String stext) {
        return strip(stext, COMMENT_BEGIN, COMMENT_END);
    }

    private static String strip(String stext, String beginTag, String endTag){
        StringBuilder noComments = new StringBuilder();
        int from = 0;
        String next = upTo(beginTag, stext, from);
        while (next.length() + from < stext.length()) {
            noComments.append(next);
            from += next.length();
            String skip = upTo(endTag, stext, from);
            from += skip.length() + endTag.length();
            next = upTo(beginTag, stext, from);
        }
        noComments.append(next);
        return noComments.toString();
    }



    private static String upTo(String word, String in, int from) {
        int i = in.indexOf(word, from);
        if (i < 0) return in.substring(from);
        return in.substring(from, i);
    }
}
