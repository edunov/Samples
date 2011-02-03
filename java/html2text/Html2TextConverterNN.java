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


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author "Sergey Edunov"
 * @version Nov 11, 2010
 */
public class Html2TextConverterNN extends Html2TextConverter {

    private EncogNN network = new EncogNN();

    protected boolean checkTreshold(int i, Line[] lines) {
        return network.score(createInput(lines[i], lines)) > 0.23;
    }

    private double[] createInput(Line l, Line[] lines) {
        double[] res = new double[7];
        res[0] = l.textLength;
        res[1] = density(l.markupLength, l.textLength);
        res[2] = l.prevTextLength;
        res[3] = density(l.prevMarkupLength, l.prevTextLength);
        res[4] = l.nextTextLength;
        res[5] = density(l.nextMarkupLength, l.nextTextLength);
        res[6] = (double) l.lineNum / lines.length;
        return res;
    }

    private double density(double markupLength, double textLength) {
        if (textLength < 1e-6) return 0;
        return markupLength / textLength;
    }
}
