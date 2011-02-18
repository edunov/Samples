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


import org.encog.ConsoleStatusReportable;
import org.encog.engine.network.activation.ActivationTANH;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.data.csv.CSVNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.layers.Layer;
import org.encog.neural.networks.synapse.Synapse;
import org.encog.neural.networks.synapse.WeightedSynapse;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.neural.pattern.FeedForwardPattern;
import org.encog.neural.prune.PruneIncremental;
import org.encog.persist.EncogPersistedCollection;
import org.encog.persist.location.InputStreamPersistence;

import java.io.File;

/**
 * @author "Sergey Edunov"
 * @version Nov 15, 2010
 */
public class EncogNN {

    private BasicNetwork network;

    public EncogNN() {
        initialize();
    }

    private void initialize() {
        EncogPersistedCollection encog = new EncogPersistedCollection(
                new InputStreamPersistence(EncogNN.class.getResourceAsStream("config.ecg")));
        network = (BasicNetwork) encog.find("TEXT_NETWORK");
        for (Layer layer :  network.getStructure().getLayers()){
            layer.setActivationFunction(new ActivationTANH());
        }
        network.getStructure().flatten();
    }

    public double score(double[] in) {
        double[] res = new double[1];
        network.compute(in, res);
        return res[0];
    }


}
