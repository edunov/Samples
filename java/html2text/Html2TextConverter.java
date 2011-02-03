/*
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


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author "Sergey Edunov"
 * @version Nov 11, 2010
 */
public class Html2TextConverter {

    public String convert(String html) throws IOException {
        List<String> slines = new ArrayList<String>();
        String[] shtml = html.split("\n");
        for (String s : shtml) {
            slines.add(s);
        }
        slines = cleanup(slines);
        Line[] lines = new Line[slines.size()];
        Line prev = new Line();
        double max = 0;
        int cnt = 0;

        for (int i = 0; i < lines.length; i++) {
            Line line = parse(slines.get(i));

            prev.nextMarkupLength = line.markupLength;
            prev.nextTextLength = line.textLength;

            line.prevMarkupLength = prev.markupLength;
            line.prevTextLength = prev.textLength;

            line.lineNum = cnt++;
            if (line.textLength > max){
                max = line.textLength;
            }

            prev = line;
            lines[i] = line;

        }
        normalize(lines, max);

        return serialize(lines);
    }

    private String serialize(Line[] lines) {
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            if (checkTreshold(i, lines)) {
                res.append(lines[i].text).append("\n");
            }
        }

        return res.toString();
    }

    protected boolean checkTreshold(int i, Line[] lines) {
        Line l = lines[i];
        return l.markupLength /l.textLength < 0.3;
    }

    private void normalize(Line[] lines, double max) {
        for(Line l : lines){
            l.markupLength/=max;
            l.textLength/=max;
            l.prevMarkupLength/=max;
            l.prevTextLength/=max;
            l.nextMarkupLength/=max;
            l.nextTextLength/=max;
        }
    }

    private Line parse(String sline) {
        Line res = new Line();
        res.text = sline;
        boolean isMarkup = false;
        int markupCnt = 0;
        for (int i = 0; i < sline.length(); i++) {
            char ch = sline.charAt(i);
            if (ch == '<') {
                isMarkup = true;
            }
            if (isMarkup) {
                markupCnt++;
            }
            if (ch == '>') {
                isMarkup = false;
            }
        }
        res.markupLength = markupCnt;
        res.textLength = sline.length();
        return res;
    }


    public List<String> cleanup(List<String> html) {
        StringBuilder text = new StringBuilder();
        for (String s : html) {
            text.append(s + "\n");
        }
        String stext = text.toString();
        stext = HtmlCleaner.stripComments(stext);
        stext = HtmlCleaner.stripScripts(stext);
        stext = HtmlCleaner.stripStyles(stext);
        List<String> res = new ArrayList<String>();
        String[] split = stext.split("\n");
        for (String s : split) {
            if (s.trim().length() > 0) {
                res.add(s.trim());
            }
        }
        return res;
    }



}
