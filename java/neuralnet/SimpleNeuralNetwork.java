package neuralnet;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.data.basic.BasicNeuralData;
import org.encog.neural.data.basic.BasicNeuralDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.Train;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

import java.util.Arrays;
import java.util.List;

/**
 * @author "Sergey Edunov"
 * @version 2/21/11
 */
public class SimpleNeuralNetwork {

    public static void main(String[] args) {


        SimpleNeuralNetwork snn = new SimpleNeuralNetwork(2, 1, 4);
        snn.setBiasAndRange(new double[] {0, 0}, new double[]{1, 1});
        double[][] input = new double[][] {
                {1, 0},
                {0, 1},
                {1, 1},
                {0, 0},
        };
        double[][] ideal = new double[][]{
                {1},
                {1},
                {0},
                {0}
        };
        for(int n=0; n<10000; n++) {
            for(int i=0; i<input.length; i++) {
                snn.train(input[i], ideal[i], 0.5);
            }
        }




        NeuralDataSet trainingSet = new BasicNeuralDataSet(input, ideal);
        BasicNetwork network = new BasicNetwork();

        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, input[0].length));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, 4));
        network.addLayer(new BasicLayer(new ActivationSigmoid(), false, ideal[0].length));
        network.getStructure().finalizeStructure();
        network.reset();


        Train train = new Backpropagation(network, trainingSet);
//        Train train = new ResilientPropagation(network, trainingSet);
        int epoch = 1;

        do {

            train.iteration();
            if (epoch%100==0) System.out.println("Epoch #" + epoch + " Error:" + train.getError());
            epoch++;

        } while (epoch < 10000 && train.getError() > 0.001);


        double[] weights = network.getStructure().getFlat().getWeights();
        for(int i=0; i<input.length; i++) {
            System.out.println(Arrays.toString(network.compute(new BasicNeuralData(input[i])).getData()));
        }

        for(int i=0; i<input.length; i++) {
            System.out.println(Arrays.toString(snn.predict(input[i])));
        }

    }


    SimpleLayer[] layers;
    SimpleInputLayer input;
    SimpleOutpurLayer output;
    int inputs;

    public SimpleNeuralNetwork(int inputs, int outputs, int... layers) {
        this.inputs = inputs;
        this.layers = new SimpleLayer[layers.length + 2];
        input = new SimpleInputLayer(inputs);
        int p = 0;
        SimpleInnerLayer prev = input;
        this.layers[p++] = input;
        for (int cnt : layers) {
            SimpleInnerLayer next = new SimpleInnerLayer(prev, cnt);
            prev.setOutputNodes(next);
            next.randomize();
            prev = next;
            this.layers[p++] = prev;
        }
        output = new SimpleOutpurLayer(prev, outputs);
        prev.setOutputNodes(output);
        output.randomize();
        this.layers[p++] = output;
    }


    public void setBiasAndRange(double[] bias, double[] range) {
        SimpleInputLayer input = (SimpleInputLayer) layers[0];
        input.setBias(bias);
        input.setRange(range);
    }

    public void train(double[] input, double[] ideal, double learningRate){
        this.input.update(input, ideal, learningRate, 0);
    }

    public double[] predict(double[] input) {
        double[] res = new double[output.neurons()];
        for (int i = 0; i < res.length; i++) {
            res[i] = output.calculateValue(input, i);
        }
        return res;
    }

    public void setWeights(double[] weights) {
        int from = 0;
        for (int i = layers.length - 1; i > 0; i--) {
            int n = layers[i].neurons() * layers[i - 1].neurons();
            double[] layer = new double[n];
            System.arraycopy(weights, from, layer, 0, n);
            setWeights(i, layer);
            from += n;
        }
    }

    public void setWeights(int layer, double[] weights) {
        ((SimpleInnerLayer) layers[layer]).setWeights(weights);
    }


    public void reset() {
        for (SimpleLayer layer : layers) {
            layer.reset();
        }
    }


   static interface SimpleLayer {

        public int neurons();

        public double calculateValue(double[] values, int pos);

        public void update(double[] values,  double[] expected, double learningRate, int pos);

        public double error(double[] values, double[] expected, int pos);

        public void reset();

        public void randomize();
    }


    static class SimpleOutpurLayer extends SimpleInnerLayer {

        SimpleOutpurLayer(SimpleLayer next, int neurons) {
            super(next, neurons);
        }

        @Override
        public double error(double[] values, double[] expected, int pos) {
            double value = calculateValue(values, pos);
            return (expected[pos] - value) * value *(1-value);
        }


        @Override
        public void update(double[] values,  double[] expected, double learningRate, int pos) {
            double delta = learningRate * error(values, expected, pos);
            for(int i=0; i<inputNodes.neurons(); i++) {
                weights[i] += delta * inputNodes.calculateValue(values, i);
            }
        }

    }

    static class SimpleInnerLayer implements SimpleLayer {
        protected SimpleLayer inputNodes;
        protected SimpleInnerLayer outputNodes;
        protected double[] weights;
        protected int neurons;

        SimpleInnerLayer(SimpleLayer inputNodes, int neurons) {
            this.inputNodes = inputNodes;
            this.neurons = neurons;
        }

        public void setOutputNodes(SimpleInnerLayer outputNodes) {
            this.outputNodes = outputNodes;
        }

        public void setWeights(double[] weights) {
            this.weights = weights;
        }

        @Override
        public int neurons() {
            return neurons;
        }

        @Override
        public double calculateValue(double[] values, int pos) {
            double sum = 0;
            for (int i = 0; i < inputNodes.neurons(); i++) {
                sum += inputNodes.calculateValue(values, i) * weights[pos * inputNodes.neurons() + i];
            }
            if (sum < -45) return 0;
            if (sum > 45) return 1;
            double v = 1. / (1 + Math.exp(-sum));
            return v;
        }

        @Override
        public void update(double[] values,  double[] expected, double learningRate, int pos) {
            double delta = learningRate * error(values, expected, pos);
            for(int i=0; i<inputNodes.neurons(); i++) {
                weights[i] += delta * inputNodes.calculateValue(values, i);
            }
            for(int i=0; i<outputNodes.neurons(); i++) {
                outputNodes.update(values, expected, learningRate, i);
            }
        }

        @Override
        public double error(double[] values, double[] expected, int pos) {
            double value = calculateValue(values, pos);
            double err = 0;
            for(int i=0; i<outputNodes.neurons(); i++){
                err += outputNodes.error(values, expected, i) * outputNodes.weights[neurons()*i + pos];
            }
            return value * err;
        }


        public void reset() {
        }

        @Override
        public void randomize() {
            weights = new double[inputNodes.neurons()*neurons()];
            for(int i=0; i<weights.length; i++){
                weights[i] = Math.random();
            }
        }
    }

    static class SimpleInputLayer extends SimpleInnerLayer {

        private int inputs;
        private double[] bias;
        private double[] range;

        SimpleInputLayer(int inputs) {
            super(null, 0);
            this.inputs = inputs;
        }



        public void setBias(double[] bias) {
            this.bias = bias;
        }

        public void setRange(double[] range) {
            this.range = range;
        }

        @Override
        public int neurons() {
            return inputs;
        }

        public double calculateValue(double[] values, int pos) {
            return (values[pos] - bias[pos]) / range[pos];
        }


        @Override
        public double error(double[] values, double[] expected, int pos) {
            double error = 0;
            for(int i=0; i<outputNodes.neurons(); i++){
                error += outputNodes.error(values, expected, i);
            }
            return error;
        }

        public void reset() {
        }

        public void update(double[] values,  double[] expected, double learningRate, int pos){
            for(int i=0; i<outputNodes.neurons(); i++){
                outputNodes.update(values, expected, learningRate, i);
            }
        }

    }
}
